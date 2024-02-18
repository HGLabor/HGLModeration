package me.aragot.hglmoderation.admin.preset;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import me.aragot.hglmoderation.admin.config.Config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PresetHandler {

    private ArrayList<Preset> presetList = new ArrayList<>();

    public static PresetHandler instance;

    public static void loadPresets(){
        File dir = Config.dir;
        if(!dir.exists()){
            dir.mkdir();
        }

        File presetFile = new File(dir.getPath(), "presets.json");
        if(!presetFile.exists()){

            try {
                presetFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            instance = new PresetHandler();

            return;
        }
        Gson gson = new Gson();

        try {
            JsonReader reader = new JsonReader(new FileReader(presetFile));
            instance = gson.fromJson(reader, PresetHandler.class);
            reader.close();
        } catch (Exception x) {
            x.printStackTrace();
        }

        if(instance == null) instance = new PresetHandler();
    }

    public static void savePresets(){
        File dir = Config.dir;
        Gson gson = new Gson();

        File presetFile = new File(dir.getPath(), "presets.json");
        if(!presetFile.exists()){
            try {
                presetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {

            FileWriter fw = new FileWriter(presetFile);
            fw.write(gson.toJson(instance));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Preset> getPresetList(){
        return this.presetList;
    }

    public void addPreset(Preset preset){
        this.presetList.add(preset);
    }

    public boolean containsPreset(String presetName){
        for(Preset preset : presetList){
            if(preset.getPresetName().equalsIgnoreCase(presetName))
                return true;
        }
        return false;
    }

    public Preset getPresetByName(String presetName){
        for(Preset preset : presetList){
            if(preset.getPresetName().equalsIgnoreCase(presetName))
                return preset;
        }
        return null;
    }
}
