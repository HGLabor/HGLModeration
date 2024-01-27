package me.aragot.hglmoderation.database;

import com.google.gson.Gson;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.data.PlayerData;
import me.aragot.hglmoderation.data.punishments.Punishment;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.data.reports.ReportState;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ModerationDB {

    /*
        To Change:
            - Gson.fromJson(Document.toJson, Class.Class); -> Maybe change, not very efficient, could retrieve Data doing: Document.get(key);
     */

    private static final String dbPrefix = "hglmod_";
    private MongoClient mongoClient;
    private MongoDatabase mongoDB;
    private MongoCollection reportCollection;
    private MongoCollection punishmentCollection;
    private MongoCollection playerDataCollection;

    public ModerationDB(String authURI){
        this.mongoClient = MongoClients.create(authURI);
        this.mongoDB = this.mongoClient.getDatabase("moderation");
        ArrayList<String> collectionNames = this.mongoDB.listCollectionNames().into(new ArrayList<>());

        if(!collectionNames.contains(dbPrefix + "reports")) this.mongoDB.createCollection(dbPrefix + "reports");
        if(!collectionNames.contains(dbPrefix + "punishments")) this.mongoDB.createCollection(dbPrefix + "punishments");

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

    public static ArrayList<Report> getAllReports(){

        ModerationDB db = new ModerationDB(Config.instance.getDbConnectionString());

        MongoCursor<Document> cursor = db.getReportCollection().find().iterator();

        ArrayList<Report> reportList = new ArrayList<>();
        Gson gson = new Gson();
        while(cursor.hasNext()){
            Document document = cursor.next();
            reportList.add(gson.fromJson(document.toJson(), Report.class));
        }
        db.closeConnection();
        return reportList;
    }

    public static boolean pushReports(ArrayList<Report> reports){

        ModerationDB db = new ModerationDB(Config.instance.getDbConnectionString());

        List<Document> reportList = new ArrayList<>();

        try {

            Gson gson = new Gson();
            for(Report report : reports)
                reportList.add(Document.parse(gson.toJson(report)));
            db.reportCollection.insertMany(reportList);
            db.closeConnection();
            return true;

        } catch (Exception x) {
            db.closeConnection();
            return false;
        }

    }

    private void closeConnection(){
        this.mongoClient.close();
    }

    private MongoCollection getReportCollection(){
        return this.reportCollection;
    }

    private MongoCollection getPunishmentCollection(){
        return this.punishmentCollection;
    }

    private MongoCollection getPlayerDataCollection(){
        return this.playerDataCollection;
    }

    //Load and initzializes all Data. Less connection -> less console spam -> more efficient connections
    public static void loadData(){
        ModerationDB db = new ModerationDB(Config.instance.getDbConnectionString());

        MongoCursor<Document> cursor = db.getReportCollection().find().iterator();

        ArrayList<Report> reportList = new ArrayList<>();
        Gson gson = new Gson();
        while(cursor.hasNext()){
            Document document = cursor.next();
            reportList.add(gson.fromJson(document.toJson(), Report.class));
        }
        Report.reportLog = reportList;


        cursor = db.getPlayerDataCollection().find().iterator();

        ArrayList<PlayerData> dataList = new ArrayList<>();
        while(cursor.hasNext()){
            Document document = cursor.next();
            dataList.add(gson.fromJson(document.toJson(), PlayerData.class));
        }
        PlayerData.dataList = dataList;


        cursor = db.getPunishmentCollection().find().iterator();

        ArrayList<Punishment> punishments = new ArrayList<>();
        while(cursor.hasNext()){
            Document document = cursor.next();
            punishments.add(gson.fromJson(document.toJson(), Punishment.class));
        }
        Punishment.punishments = punishments;

        db.closeConnection();

    }

}
