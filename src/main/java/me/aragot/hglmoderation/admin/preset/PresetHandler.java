package me.aragot.hglmoderation.admin.preset;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import me.aragot.hglmoderation.HGLModeration;
import me.aragot.hglmoderation.admin.config.Config;
import me.aragot.hglmoderation.entity.Reasoning;
import me.aragot.hglmoderation.repository.PresetRepository;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PresetHandler {

    private final PresetRepository repository = new PresetRepository();

    private ArrayList<Preset> presetList = new ArrayList<>();

    public static PresetHandler instance;

    public PresetHandler() {
        instance = this;
    }

    public static void loadPresets() {
        File dir = Config.dir;

        File presetFile = new File(dir.getPath(), "presets.json");
        if (!presetFile.exists()) {
            instance = new PresetHandler();
            instance.loadPresetsFromDatabase();
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

        if (instance == null) instance = new PresetHandler();

        try {
            if (!presetFile.delete()) {
                HGLModeration.instance.getLogger().error("Couldn't delete old preset file, cancelling migration");
            }

            HGLModeration.instance.getLogger().info("Successfully deleted deprecated Preset file");
            PresetRepository presetRepository = new PresetRepository();

            for (Preset preset : instance.presetList) {
                if (preset.getId() == null) {
                    preset.setId(UUID.randomUUID());
                }
            }

            if (presetRepository.insertAll(instance.presetList)) {
                HGLModeration.instance.getLogger().info("Successfully migrated all presets to the database");
                return;
            }

            HGLModeration.instance.getLogger().error("Couldn't migrate the presets from the files to the database");
        } catch (SecurityException ignored) {
            HGLModeration.instance.getLogger().error("Couldn't delete old Preset file");
        }
    }

    public void loadPresetsFromDatabase() {
        this.presetList = new ArrayList<>(this.repository.getAllPresets());
    }

    public static void savePresets() {
        instance.repository.saveAll(instance.presetList);
    }

    public List<Preset> getPresetList() {
        return this.presetList;
    }

    public void addPreset(Preset preset) {
        this.presetList.add(preset);
    }

    public void removePreset(Preset preset) {
        this.presetList.remove(preset);
    }

    public boolean containsPreset(String presetName) {
        for (Preset preset : presetList) {
            if (preset.getName().equalsIgnoreCase(presetName))
                return true;
        }
        return false;
    }

    public Preset getPresetByName(String presetName) {
        for (Preset preset : presetList) {
            if (preset.getName().equalsIgnoreCase(presetName))
                return preset;
        }
        return null;
    }

    public Preset getPresetForScore(Reasoning scope, int score) {
        for (Preset preset : this.presetList)
            if (preset.isInScope(scope) && preset.isInRange(score))
                return preset;

        return null;
    }
}
