package me.aragot.hglmoderation.service.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.entity.PlayerData;
import me.aragot.hglmoderation.entity.punishments.Punishment;
import me.aragot.hglmoderation.entity.reports.Report;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.*;

public class ModerationDB {

    private static final String dbPrefix = "hglmod_";
    private final MongoClient mongoClient;
    public final MongoCollection<Report> reportCollection;
    public final MongoCollection<Punishment> punishmentCollection;
    public final MongoCollection<PlayerData> playerDataCollection;
    public final MongoCollection<Preset> presetCollection;

    public ModerationDB(String authURI) {
        ConnectionString connectionString = new ConnectionString(authURI);

        // Default codec
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
                PojoCodecProvider
                        .builder()
                        .automatic(true)
                        .build()
        );

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                pojoCodecRegistry
        );

        MongoClientSettings clientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(codecRegistry)
                .build();

        this.mongoClient = MongoClients.create(clientSettings);
        MongoDatabase mongoDB = this.mongoClient.getDatabase("hglabor");
        ArrayList<String> collectionNames = mongoDB.listCollectionNames().into(new ArrayList<>());

        if (!collectionNames.contains(dbPrefix + "reports")) mongoDB.createCollection(dbPrefix + "reports");
        if (!collectionNames.contains(dbPrefix + "punishments")) mongoDB.createCollection(dbPrefix + "punishments");
        if (!collectionNames.contains(dbPrefix + "playerdata")) mongoDB.createCollection(dbPrefix + "playerdata");
        if (!collectionNames.contains(dbPrefix + "presets")) mongoDB.createCollection(dbPrefix + "presets");

        this.reportCollection = mongoDB.getCollection(dbPrefix + "reports", Report.class);
        this.punishmentCollection = mongoDB.getCollection(dbPrefix + "punishments", Punishment.class);
        this.playerDataCollection = mongoDB.getCollection(dbPrefix + "playerdata", PlayerData.class);
        this.presetCollection = mongoDB.getCollection(dbPrefix + "presets", Preset.class);
    }

    public void closeConnection(){
        this.mongoClient.close();
    }
}
