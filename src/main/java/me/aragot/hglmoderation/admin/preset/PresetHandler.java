package me.aragot.hglmoderation.admin.preset;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.data.Reasoning;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PresetHandler {

    private final ArrayList<Preset> presetList = new ArrayList<>();

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
        } catch (IOException x) {
            HGLModeration.instance.getLogger().error(x.getMessage());
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
            } catch (IOException x) {
                HGLModeration.instance.getLogger().error(x.getMessage());
            }
        }

        try {

            FileWriter fw = new FileWriter(presetFile);
            fw.write(gson.toJson(instance));
            fw.close();
        } catch (IOException x) {
            HGLModeration.instance.getLogger().error(x.getMessage());
        }

    }

    public ArrayList<Preset> getPresetList(){
        return this.presetList;
    }

    public void addPreset(Preset preset){
        this.presetList.add(preset);
    }

    public void removePreset(Preset preset){
        this.presetList.remove(preset);
    }

    public boolean containsPreset(String presetName){
        for(Preset preset : presetList){
            if(preset.getName().equalsIgnoreCase(presetName))
                return true;
        }
        return false;
    }

    public Preset getPresetByName(String presetName){
        for(Preset preset : presetList){
            if(preset.getName().equalsIgnoreCase(presetName))
                return preset;
        }
        return null;
    }

    public Preset getPresetForScore(Reasoning scope, int score){
        for(Preset preset : this.presetList)
            if(preset.isInScope(scope) && preset.isInRange(score)) return preset;

        return null;
    }
}
