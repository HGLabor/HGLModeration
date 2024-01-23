package me.aragot.hglmoderation.database;

import com.google.gson.Gson;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.data.reports.Report;
import me.aragot.hglmoderation.data.reports.ReportState;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModerationDB {

    private static final String dbPrefix = "hglmod_";
    private MongoClient mongoClient;
    private MongoDatabase mongoDB;
    private MongoCollection reportCollection;
    private MongoCollection punishmentCollection;

    public ModerationDB(String authURI){
        this.mongoClient = MongoClients.create(authURI);
        this.mongoDB = this.mongoClient.getDatabase("admin");
        ArrayList<String> collectionNames = this.mongoDB.listCollectionNames().into(new ArrayList<>());

        if(!collectionNames.contains(dbPrefix + "reports")) this.mongoDB.createCollection(dbPrefix + "reports");
        if(!collectionNames.contains(dbPrefix + "punishments")) this.mongoDB.createCollection(dbPrefix + "punishments");

        this.reportCollection = this.mongoDB.getCollection(dbPrefix + "reports");
        this.punishmentCollection = this.mongoDB.getCollection(dbPrefix + "punishments");
    }



    public static ArrayList<Report> getOpenReports(ReportState state){

        ModerationDB db = new ModerationDB(Config.instance.getDbConnectionString());

        MongoCursor<Document> cursor =  db.getReportCollection().aggregate(
                List.of(Aggregates.match(Filters.eq("state", state.toString())))
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

    private void closeConnection(){
        this.mongoClient.close();
    }

    private MongoCollection getReportCollection(){
        return this.reportCollection;
    }
}
