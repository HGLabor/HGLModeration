package me.aragot.hglmoderation.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.punishments.Punishment;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.data.reports.ReportState;
import me.aragot.hglmoderation.database.codecs.PlayerDataCodec;
import me.aragot.hglmoderation.database.codecs.PunishmentCodec;
import me.aragot.hglmoderation.database.codecs.ReportCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.time.Instant;
import java.util.*;

public class ModerationDB {

    private static final String dbPrefix = "hglmod_";
    private final MongoClient mongoClient;
    private final MongoCollection<Report> reportCollection;
    private final MongoCollection<Punishment> punishmentCollection;
    private final MongoCollection<PlayerData> playerDataCollection;

    public ModerationDB(String authURI){

        ConnectionString connectionString = new ConnectionString(authURI);
        CodecRegistry reportRegistry = CodecRegistries.fromCodecs(new ReportCodec());
        CodecRegistry punishmentRegistry = CodecRegistries.fromCodecs(new PunishmentCodec());
        CodecRegistry playerDataRegistry = CodecRegistries.fromCodecs(new PlayerDataCodec());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), reportRegistry, punishmentRegistry, playerDataRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(codecRegistry)
                .build();

        this.mongoClient = MongoClients.create(clientSettings);
        MongoDatabase mongoDB = this.mongoClient.getDatabase("hglabor");
        ArrayList<String> collectionNames = mongoDB.listCollectionNames().into(new ArrayList<>());

        if(!collectionNames.contains(dbPrefix + "reports")) mongoDB.createCollection(dbPrefix + "reports");
        if(!collectionNames.contains(dbPrefix + "punishments")) mongoDB.createCollection(dbPrefix + "punishments");
        if(!collectionNames.contains(dbPrefix + "playerdata")) mongoDB.createCollection(dbPrefix + "playerdata");

        this.reportCollection = mongoDB.getCollection(dbPrefix + "reports", Report.class);
        this.punishmentCollection = mongoDB.getCollection(dbPrefix + "punishments", Punishment.class);
        this.playerDataCollection = mongoDB.getCollection(dbPrefix + "playerdata", PlayerData.class);
    }

    public ArrayList<Report> getUnfinishedReports(){
        MongoCursor<Report> cursor =  this.reportCollection.aggregate(
                List.of(Aggregates.match(Filters.or(Filters.eq("state", ReportState.OPEN), Filters.eq("state", ReportState.UNDER_REVIEW))))
        ).iterator();

        ArrayList<Report> reportList = new ArrayList<>();
        while(cursor.hasNext())
            reportList.add(cursor.next());
        return reportList;
    }

    public Report getReportById(String reportId){
        return this.reportCollection.find(Filters.eq("_id", reportId)).first();
    }

    public boolean pushReport(Report report){
        try {
            this.reportCollection.insertOne(report);
            return true;

        } catch (MongoException x) {
            return false;
        }
    }

    public boolean updateReportsBasedOn(Report report){

        return this.reportCollection.updateMany(
                Filters.and(Filters.eq("reportedUUID", report.getReportedUUID()),
                        Filters.eq("reasoning", report.getReasoning()),
                        Filters.or(Filters.eq("state", ReportState.UNDER_REVIEW), Filters.eq("state", ReportState.OPEN))
                ),
                Updates.combine(Updates.set("state", report.getState()),
                        Updates.set("reviewedBy", report.getReviewedBy()),
                        Updates.set("punishmentId", report.getPunishmentId())
                )
        ).wasAcknowledged();
    }

    public void closeConnection(){
        this.mongoClient.close();
    }


    public Punishment getPunishmentById(String punishmentId){
        return this.punishmentCollection.find(Filters.eq("_id", punishmentId)).first();
    }

    public PlayerData getPlayerDataById(String playerId){
        return this.playerDataCollection.find(Filters.eq("_id", playerId)).first();
    }

    public boolean pushPlayerData(PlayerData data){

        try {
            this.playerDataCollection.insertOne(data);
            return true;

        } catch (MongoException x) {
            return false;
        }

    }

    public boolean updatePlayerData(PlayerData data){
        return this.playerDataCollection.replaceOne(
                Filters.eq("_id", data.getPlayerId()),
                data)
                .wasAcknowledged();
    }

    public boolean pushPunishment(Punishment punishment){

        try {
            this.punishmentCollection.insertOne(punishment);
            return true;

        } catch (MongoException x) {
            return false;
        }

    }

    public ArrayList<Punishment> getPunishmentsForPlayer(String uuid, String host){
        return this.punishmentCollection.find(Filters.or(Filters.eq("issuedTo", uuid), Filters.eq("issuedTo", host))).sort(Sorts.descending("issuedAt")).into(new ArrayList<>());
    }

    public ArrayList<Report> getReportsForPlayerExcept(String playerId, String reportId){
        return this.reportCollection.find(Filters.and(Filters.eq("reportedUUID", playerId), Filters.ne("_id", reportId), Filters.ne("state", ReportState.DONE.name()))).into(new ArrayList<>());
    }

    public ArrayList<Report> getReportsByPlayer(String playerId){
        return this.reportCollection.find(Filters.and(Filters.eq("reportedUUID", playerId), Filters.ne("state", ReportState.DONE.name()))).into(new ArrayList<>());
    }

    public boolean updatePunishment(Punishment punishment){
        return this.punishmentCollection.replaceOne(
                        Filters.eq("_id", punishment.getId()),
                        punishment)
                .wasAcknowledged();
    }

    public ArrayList<Punishment> getActivePunishments(String uuid, String host){
        return this.punishmentCollection.find(Filters.and(
                Filters.or(
                        Filters.eq("endsAt", -1),
                        Filters.gt("endsAt", Instant.now().getEpochSecond())),
                Filters.or(
                        Filters.eq("issuedTo", uuid),
                        Filters.eq("issuedTo", host)))).into(new ArrayList<>());
    }
}
