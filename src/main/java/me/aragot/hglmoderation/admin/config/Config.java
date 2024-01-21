package me.aragot.hglmoderation.admin.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    private static final File dir = new File("./HGLModeration");
    private String dbConnectionString = "";
    private String discordBotToken = "";
    private String reportChannelId = "";

    public static Config instance;

    public static void loadConfig(){
        if(!dir.exists()){
            dir.mkdir();
        }

        File configFile = new File(dir.getPath(), "config.json");
        if(!configFile.exists()){

            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            instance = new Config();

            return;
        }
        Gson gson = new Gson();

        try {
            JsonReader reader = new JsonReader(new FileReader(configFile));
            instance = gson.fromJson(reader, Config.class);
            reader.close();
        } catch (Exception x) {
            x.printStackTrace();
        }

        if(instance == null) instance = new Config();
    }

    public static void saveConfig(){
        Gson gson = new Gson();

        File configFile = new File(dir.getPath(), "config.json");
        if(!configFile.exists()){
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {

            FileWriter fw = new FileWriter(configFile);
            fw.write(gson.toJson(instance));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getDiscordBotToken() {
        return discordBotToken;
    }

    public String getDbConnectionString() {
        return dbConnectionString;
    }
}