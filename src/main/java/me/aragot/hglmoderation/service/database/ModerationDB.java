package me.aragot.hglmoderation.service.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.entity.reports.Report;
import me.aragot.hglmoderation.service.database.codecs.PlayerDataCodec;
import me.aragot.hglmoderation.service.database.codecs.PunishmentCodec;
import me.aragot.hglmoderation.service.database.codecs.ReportCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.*;

public class ModerationDB {

    private static final String dbPrefix = "hglmod_";
    private final MongoClient mongoClient;
    public final MongoCollection<Report> reportCollection;
    public final MongoCollection<Punishment> punishmentCollection;
    public final MongoCollection<PlayerData> playerDataCollection;

    public ModerationDB(String authURI) {
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
        MongoDatabase mongoDB = this.mongoClient.getDatabase("Moderation");
        ArrayList<String> collectionNames = mongoDB.listCollectionNames().into(new ArrayList<>());

        if (!collectionNames.contains(dbPrefix + "reports")) mongoDB.createCollection(dbPrefix + "reports");
        if (!collectionNames.contains(dbPrefix + "punishments")) mongoDB.createCollection(dbPrefix + "punishments");
        if (!collectionNames.contains(dbPrefix + "playerdata")) mongoDB.createCollection(dbPrefix + "playerdata");

        this.reportCollection = mongoDB.getCollection(dbPrefix + "reports", Report.class);
        this.punishmentCollection = mongoDB.getCollection(dbPrefix + "punishments", Punishment.class);
        this.playerDataCollection = mongoDB.getCollection(dbPrefix + "playerdata", PlayerData.class);
    }

    public void closeConnection(){
        this.mongoClient.close();
    }
}
