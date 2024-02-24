package me.aragot.hglmoderation.database;

import com.google.gson.Gson;
import com.mongodb.MongoException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.punishments.Punishment;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.data.reports.ReportState;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

public class ModerationDB {

    /*
        To Change:
            - Gson.fromJson(Document.toJson, Class.Class); -> Maybe change, not very efficient, could retrieve Data doing: Document.get(key);
            - Recode needed, poorly used db so far.
     */

    private static final String dbPrefix = "hglmod_";
    private final MongoClient mongoClient;
    private MongoDatabase mongoDB;
    private final MongoCollection<Document> reportCollection;
    private final MongoCollection<Document> punishmentCollection;
    private final MongoCollection<Document> playerDataCollection;

    public ModerationDB(String authURI){
        this.mongoClient = MongoClients.create(authURI);
        this.mongoDB = this.mongoClient.getDatabase("Moderation");
        ArrayList<String> collectionNames = this.mongoDB.listCollectionNames().into(new ArrayList<>());

        if(!collectionNames.contains(dbPrefix + "reports")) this.mongoDB.createCollection(dbPrefix + "reports");
        if(!collectionNames.contains(dbPrefix + "punishments")) this.mongoDB.createCollection(dbPrefix + "punishments");
        if(!collectionNames.contains(dbPrefix + "playerdata")) this.mongoDB.createCollection(dbPrefix + "playerdata");

        this.reportCollection = this.mongoDB.getCollection(dbPrefix + "reports");
        this.punishmentCollection = this.mongoDB.getCollection(dbPrefix + "punishments");
        this.playerDataCollection = this.mongoDB.getCollection(dbPrefix + "playerdata");
    }

    public ArrayList<Report> getReportsByState(ReportState state){


        MongoCursor<Document> cursor =  this.reportCollection.aggregate(
                List.of(Aggregates.match(Filters.eq("state", state.name())))
        ).iterator();

        ArrayList<Report> reportList = new ArrayList<>();
        Gson gson = new Gson();
        while(cursor.hasNext()){
            Document document = cursor.next();
            reportList.add(gson.fromJson(document.toJson(), Report.class));
        }
        return reportList;
    }

    public Report getReportById(String reportId){
        Document report = this.reportCollection.find(Filters.eq("_id", reportId)).first();
        if(report == null) return null;
        Gson gson = new Gson();
        return gson.fromJson(report.toJson(), Report.class);
    }

    public boolean pushReport(Report report){

        try {
            Gson gson = new Gson();
            this.reportCollection.insertOne(Document.parse(gson.toJson(report)));
            return true;

        } catch (MongoException x) {
            return false;
        }

    }

    public boolean updateReport(Report report){

        UpdateResult res = this.reportCollection.updateOne(
                Filters.eq("_id", report.getReportId()),
                Updates.set("state", report.getState())
        );

        return res.wasAcknowledged();
    }

    public void closeConnection(){
        this.mongoClient.close();
    }


    public Punishment getPunishmentById(String punishmentId){
        Document punishment = this.punishmentCollection.find(Filters.eq("_id", punishmentId)).first();
        if(punishment == null) return null;
        Gson gson = new Gson();
        return gson.fromJson(punishment.toJson(), Punishment.class);
    }

    public PlayerData getPlayerDataById(String playerId){
        Document playerData = this.playerDataCollection.find(Filters.eq("_id", playerId)).first();
        if(playerData == null) return null;
        Gson gson = new Gson();
        return gson.fromJson(playerData.toJson(), PlayerData.class);
    }

    public boolean pushPlayerData(PlayerData data){

        try {
            Gson gson = new Gson();
            this.playerDataCollection.insertOne(Document.parse(gson.toJson(data)));
            return true;

        } catch (MongoException x) {
            return false;
        }

    }

    public boolean updatePlayerData(PlayerData data){
        Gson gson = new Gson();
        return this.playerDataCollection.replaceOne(
                new Document("_id", data.getPlayerId()),
                Document.parse(gson.toJson(data)))
                .wasAcknowledged();
    }

    public boolean pushPunishment(Punishment punishment){

        try {
            Gson gson = new Gson();
            this.punishmentCollection.insertOne(Document.parse(gson.toJson(punishment)));
            return true;

        } catch (MongoException x) {
            return false;
        }

    }
}
