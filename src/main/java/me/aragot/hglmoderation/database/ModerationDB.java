package me.aragot.hglmoderation.database;

import com.google.gson.Gson;
import com.mongodb.MongoException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
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
    private MongoClient mongoClient;
    private MongoDatabase mongoDB;
    private MongoCollection<Document> reportCollection;
    private MongoCollection<Document> punishmentCollection;
    private MongoCollection<Document> playerDataCollection;

    public ModerationDB(String authURI){
        this.mongoClient = MongoClients.create(authURI);
        this.mongoDB = this.mongoClient.getDatabase("moderation");
        ArrayList<String> collectionNames = this.mongoDB.listCollectionNames().into(new ArrayList<>());

        if(!collectionNames.contains(dbPrefix + "reports")) this.mongoDB.createCollection(dbPrefix + "reports");
        if(!collectionNames.contains(dbPrefix + "punishments")) this.mongoDB.createCollection(dbPrefix + "punishments");
        if(!collectionNames.contains(dbPrefix + "playerdata")) this.mongoDB.createCollection(dbPrefix + "playerdata");

        this.reportCollection = this.mongoDB.getCollection(dbPrefix + "reports");
        this.punishmentCollection = this.mongoDB.getCollection(dbPrefix + "punishments");
        this.playerDataCollection = this.mongoDB.getCollection(dbPrefix + "playerdata");
    }

    public static ArrayList<Report> getReportsByState(ReportState state){

        ModerationDB db = new ModerationDB(Config.instance.getDbConnectionString());

        MongoCursor<Document> cursor =  db.getReportCollection().aggregate(
                List.of(Aggregates.match(Filters.eq("state", state.name())))
        ).iterator();

        ArrayList<Report> reportList = new ArrayList<>();
        Gson gson = new Gson();
        while(cursor.hasNext()){
            Document document = cursor.next();
            reportList.add(gson.fromJson(document.toJson(), Report.class));
        }
        db.closeConnection();
        return reportList;
    }

    //<Date, Report> Because ObjectId is represented by the date
    public ArrayList<Report> getAllReports(){

        MongoCursor<Document> cursor = this.reportCollection.find().iterator();

        ArrayList<Report> reportList = new ArrayList<>();

        Gson gson = new Gson();
        while(cursor.hasNext()){
            Document document = cursor.next();

            reportList.add(gson.fromJson(document.toJson(), Report.class));
        }
        return reportList;
    }

    public boolean pushReports(ArrayList<Report> reports){

        List<Document> reportList = new ArrayList<>();

        try {

            Gson gson = new Gson();
            for(Report report : reports)
                reportList.add(Document.parse(gson.toJson(report)));
            this.reportCollection.insertMany(reportList);
            return true;

        } catch (MongoException x) {
            return false;
        }

    }

    public boolean updateReports(ArrayList<Report> reportList){
        List<WriteModel<Document>> operations = new ArrayList<>();
        for(Report report : reportList){
            Document query = new Document("reportId", report.getReportId());
            Document change = new Document("$set",
                    new Document("state", report.getState())
                    .append("punishmentId", report.getPunishmentId())
            );
            operations.add(new UpdateOneModel<>(query, change));
        }

        if(operations.isEmpty()) return true;

        BulkWriteResult result = this.reportCollection.bulkWrite(operations);

        return result.wasAcknowledged();
    }


    public boolean updatePlayerData(HashMap<Date, Report> reportMap){
        List<WriteModel<Document>> operations = new ArrayList<>();
        for(Map.Entry<Date, Report> reportEntry : reportMap.entrySet()){
            Document query = new Document("_id", new ObjectId(reportEntry.getKey()));
            Document change = new Document("$set", new Document("state", reportEntry.getValue().getState()));
            operations.add(new UpdateOneModel<>(query, change));
        }

        if(operations.isEmpty()) return true;

        BulkWriteResult result = this.reportCollection.bulkWrite(operations);

        return result.wasAcknowledged();
    }

    public void closeConnection(){
        this.mongoClient.close();
    }

    private MongoCollection<Document> getReportCollection(){
        return this.reportCollection;
    }

    private MongoCollection<Document> getPunishmentCollection(){
        return this.punishmentCollection;
    }

    private MongoCollection<Document> getPlayerDataCollection(){
        return this.playerDataCollection;
    }

    //Load and initzializes all Data. Less connection -> less console spam -> more efficient connections
    public void loadData(){

        MongoCursor<Document> cursor = this.reportCollection.find().iterator();

        ArrayList<Report> reportList = new ArrayList<>();
        Gson gson = new Gson();
        while(cursor.hasNext()){
            Document document = cursor.next();
            reportList.add(gson.fromJson(document.toJson(), Report.class));
        }
        Report.reportLog = reportList;


        cursor = this.playerDataCollection.find().iterator();

        ArrayList<PlayerData> dataList = new ArrayList<>();
        while(cursor.hasNext()){
            Document document = cursor.next();
            dataList.add(gson.fromJson(document.toJson(), PlayerData.class));
        }
        PlayerData.dataList = dataList;


        cursor = this.punishmentCollection.find().iterator();

        ArrayList<Punishment> punishments = new ArrayList<>();
        while(cursor.hasNext()){
            Document document = cursor.next();
            punishments.add(gson.fromJson(document.toJson(), Punishment.class));
        }
        Punishment.punishments = punishments;

    }

}
