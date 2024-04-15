package me.aragot.hglmoderation.admin.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import me.aragot.hglmoderation.HGLModeration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {

    public static final File dir = new File("./HGLModeration");
    private String dbConnectionString = "";
    private String discordBotToken = "";
    private String reportChannelId = "";
    private String punishmentChannelId = "";
    private String reportPingroleId = "";

    public static Config instance;

    public static final String discordLink = "discord.hglabor.gg";

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
        } catch (IOException x) {
            HGLModeration.instance.getLogger().error(x.getMessage());
        }

        if(instance == null) instance = new Config();
    }

    public static void saveConfig(){
        Gson gson = new Gson();

        File configFile = new File(dir.getPath(), "config.json");
        if(!configFile.exists()){
            try {
                configFile.createNewFile();
            } catch (IOException x) {
                HGLModeration.instance.getLogger().error(x.getMessage());
            }
        }

        try {

            FileWriter fw = new FileWriter(configFile);
            fw.write(gson.toJson(instance));
            fw.close();
        } catch (IOException x) {
            HGLModeration.instance.getLogger().error(x.getMessage());
        }

    }

    public String getDiscordBotToken() {
        return discordBotToken;
    }

    public String getDbConnectionString() {
        return dbConnectionString;
    }

    public String getReportChannelId() {
        return reportChannelId;
    }

    public void setReportChannelId(String reportChannelId) {
        this.reportChannelId = reportChannelId;
        saveConfig();
    }

    public String getReportPingroleId() {
        return reportPingroleId;
    }

    public void setReportPingroleId(String reportPingroleId) {
        this.reportPingroleId = reportPingroleId;
        saveConfig();
    }

    public String getPunishmentChannelId() {
        return punishmentChannelId;
    }

    public void setPunishmentChannelId(String punishmentChannelId) {
        this.punishmentChannelId = punishmentChannelId;
    }
}
