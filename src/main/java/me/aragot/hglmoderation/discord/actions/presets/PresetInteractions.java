package me.aragot.hglmoderation.discord.actions.presets;

import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.admin.preset.PresetHandler;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.data.punishments.PunishmentType;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.tools.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.ArrayList;
import java.util.List;

public class PresetInteractions {
    public static EmbedBuilder editPresetFromModal(ModalInteractionEvent event){
        String presetName = event.getValue("preset-name").getAsString();
        Preset preset = PresetHandler.instance.getPresetByName(presetName);
        if(preset == null)
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find this preset. Try refreshing your PresetGUI");

        String presetDescription = event.getValue("preset-description").getAsString();
        int start;
        int end;
        int weight;


        try {
            start = Integer.parseInt(event.getValue("preset-start").getAsString());
            end = Integer.parseInt(event.getValue("preset-end").getAsString());
            weight = Integer.parseInt(event.getValue("preset-weight").getAsString());
        } catch (NumberFormatException x) {
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "Your Preset Start, End and Weight must be whole integers.");
        }


        if(start > end && end != -1)
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "Preset Range start value cannot be greater than the end value.");

        preset.setDescription(presetDescription);
        preset.setStart(start);
        preset.setEnd(end);
        preset.setWeight(weight);

        PresetHandler.savePresets();

        return HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully edited Preset.");
    }

    public static EmbedBuilder setDurationFromModal(ModalInteractionEvent event){
        String presetName = event.getValue("preset-name").getAsString();
        Preset preset = PresetHandler.instance.getPresetByName(presetName);
        if(preset == null)
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find this preset. Try refreshing your PresetGUI");

        int days;
        int hours;
        int minutes;


        try {
            days = Integer.parseInt(event.getValue("preset-days").getAsString());
            hours = Integer.parseInt(event.getValue("preset-hours").getAsString());
            minutes = Integer.parseInt(event.getValue("preset-minutes").getAsString());
        } catch (NumberFormatException x) {
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "Your duration's days, hours and minutes must be whole integers.");
        }


        if(days < 0 || hours < 0 || minutes < 0){
            preset.setDuration(-1);
        } else {
            preset.setDuration(convertDurationToSeconds(days, hours, minutes));
        }

        PresetHandler.savePresets();

        return HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully set the duration for the Preset.");
    }

    public static EmbedBuilder createPresetFromModal(ModalInteractionEvent event){
        String presetName = event.getValue("preset-name").getAsString();
        if(presetName.contains(" "))
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "Your Preset Name cannot contain spaces!");

        if(PresetHandler.instance.containsPreset(presetName))
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "There is already a preset named like that. Please choose a unique name.");

        String presetDescription = event.getValue("preset-description").getAsString();
        int start;
        int end;
        int weight;


        try {
            start = Integer.parseInt(event.getValue("preset-start").getAsString());
            end = Integer.parseInt(event.getValue("preset-end").getAsString());
            weight = Integer.parseInt(event.getValue("preset-weight").getAsString());
        } catch (NumberFormatException x) {
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "Your Preset Start, End and Weight must be whole integers.");
        }


        if(start > end && end != -1)
            return HGLBot.getEmbedTemplate(ResponseType.ERROR, "Preset Range start value cannot be greater than the end value.");

        Preset preset = new Preset(presetName, presetDescription, start, end, weight);
        PresetHandler.instance.addPreset(preset);
        PresetHandler.savePresets();

        return HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully created new Preset.");
    }

    public static EmbedBuilder getPresetEmbed(Preset preset){
        EmbedBuilder eb = HGLBot.getEmbedTemplate(ResponseType.DEFAULT);
        String endsAt = preset.getEnd() == -1 ? "Infinity" : String.valueOf(preset.getEnd());
        eb.setTitle(preset.getName());
        eb.setDescription(
                "Description:\n" +
                        preset.getDescription() + "\n\n" +
                        "Punishment Range: " + preset.getStart() + " -> " + endsAt + "\n" +
                        "Preset Weight: " + preset.getWeight() + "\n" +
                        "Punishment Duration: " + preset.getDurationAsString()
        );
        return eb;
    }

    public static StringSelectMenu.Builder getScopeMenuForPreset(Preset preset){
        StringSelectMenu.Builder reasonScope = StringSelectMenu.create(preset.getName().toLowerCase() + "-scopes");
        reasonScope.setPlaceholder("Select this presets scope");
        reasonScope.setMaxValues(Reasoning.values().length);
        for(Reasoning reason : Reasoning.values()){
            reasonScope.addOption(StringUtils.capitalize(reason.name().toLowerCase()),
                    preset.getName().toLowerCase() + "-" + reason.name().toLowerCase(),
                    Emoji.fromUnicode("\uD83E\uDEAC"));
        }

        ArrayList<String> reasoningIds = new ArrayList<>();
        for(Reasoning reason : preset.getReasoningScope())
            reasoningIds.add(preset.getName().toLowerCase() + "-" + reason.name().toLowerCase());

        reasonScope.setDefaultValues(reasoningIds);
        return reasonScope;
    }

    public static StringSelectMenu.Builder getPunishmentTypeMenuForPreset(Preset preset){
        StringSelectMenu.Builder typeMenu = StringSelectMenu.create(preset.getName().toLowerCase() + "-type");
        typeMenu.setPlaceholder("Select this presets punishments");
        typeMenu.setMaxValues(PunishmentType.values().length);
        for(PunishmentType type : PunishmentType.values()){
            typeMenu.addOption(StringUtils.capitalize(type.name().toLowerCase()),
                    preset.getName().toLowerCase() + "-" + type.name().toLowerCase(),
                    Emoji.fromUnicode("\uD83D\uDEAB"));
        }

        ArrayList<String> typeIds = new ArrayList<>();
        for(PunishmentType type : preset.getPunishmentsTypes())
            typeIds.add(preset.getName().toLowerCase() + "-" + type.name().toLowerCase());

        typeMenu.setDefaultValues(typeIds);
        return typeMenu;
    }

    public static ArrayList<Reasoning> getReasoningScopeForValues(String presetName, List<String> values){
        ArrayList<Reasoning> reasoningScope = new ArrayList<>();
        for(String value : values){
            String reasoning = value.replace(presetName + "-", "").toUpperCase();
            reasoningScope.add(Reasoning.valueOf(reasoning));
        }
        return reasoningScope;
    }

    public static ArrayList<PunishmentType> getPunishmentTypesForValues(String presetName, List<String> values){
        ArrayList<PunishmentType> types = new ArrayList<>();
        for(String value : values){
            String type = value.replace(presetName + "-", "").toUpperCase();
            types.add(PunishmentType.valueOf(type));
        }
        return types;
    }

    public static Modal getModal(){
        TextInput presetNameInput = TextInput.create("preset-name", "Preset Name", TextInputStyle.SHORT)
                .setPlaceholder("Name of Preset (No Spaces allowed!)")
                .setMinLength(1)
                .setMaxLength(25)
                .build();

        TextInput startInput = TextInput.create("preset-start", "Preset Start", TextInputStyle.SHORT)
                .setPlaceholder("Preset Start from Number")
                .setMinLength(1)
                .setMaxLength(5)
                .build();

        TextInput endInput = TextInput.create("preset-end", "Preset End", TextInputStyle.SHORT)
                .setPlaceholder("Preset Ends at Number (-1 == infinity)")
                .setMinLength(1)
                .setMaxLength(5)
                .build();

        TextInput weight = TextInput.create("preset-weight", "Preset Weight", TextInputStyle.SHORT)
                .setPlaceholder("Weight adds is added to the Players punishment score")
                .setMinLength(1)
                .setMaxLength(5)
                .build();

        TextInput presetDescriptionInput = TextInput.create("preset-description", "Preset Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Preset description such as \"Lowest Punishment Preset for Chat-Based Punishments\"")
                .setMinLength(10)
                .setMaxLength(150)
                .build();

        return Modal.create("preset-create", "Create new Preset")
                .addComponents(ActionRow.of(presetNameInput), ActionRow.of(startInput), ActionRow.of(endInput),ActionRow.of(weight), ActionRow.of(presetDescriptionInput))
                .build();
    }

    public static Modal getModalForPreset(Preset preset){
        TextInput presetNameInput = TextInput.create("preset-name", "Preset Name", TextInputStyle.SHORT)
                .setPlaceholder("Name of Preset (No Spaces allowed!)")
                .setValue(preset.getName())
                .setMinLength(1)
                .setMaxLength(25)
                .build();

        TextInput startInput = TextInput.create("preset-start", "Preset Start", TextInputStyle.SHORT)
                .setPlaceholder("Preset Start from Number")
                .setValue(String.valueOf(preset.getStart()))
                .setMinLength(1)
                .setMaxLength(5)
                .build();

        TextInput endInput = TextInput.create("preset-end", "Preset End", TextInputStyle.SHORT)
                .setPlaceholder("Preset Ends at Number (-1 == infinity)")
                .setValue(String.valueOf(preset.getEnd()))
                .setMinLength(1)
                .setMaxLength(5)
                .build();

        TextInput weight = TextInput.create("preset-weight", "Preset Weight", TextInputStyle.SHORT)
                .setPlaceholder("Weight adds is added to the Players punishment score")
                .setValue(String.valueOf(preset.getWeight()))
                .setMinLength(1)
                .setMaxLength(5)
                .build();

        TextInput presetDescriptionInput = TextInput.create("preset-description", "Preset Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Preset description such as \"Lowest Punishment Preset for Chat-Based Punishments\"")
                .setValue(preset.getDescription())
                .setMinLength(10)
                .setMaxLength(150)
                .build();

        return Modal.create("preset-edit", "Edit Preset")
                .addComponents(ActionRow.of(presetNameInput), ActionRow.of(startInput), ActionRow.of(endInput),ActionRow.of(weight), ActionRow.of(presetDescriptionInput))
                .build();
    }

    public static Modal getDurationModal(Preset preset){
        TextInput presetNameInput = TextInput.create("preset-name", "Preset Name (DO NOT EDIT!)", TextInputStyle.SHORT)
                .setPlaceholder("Name of Preset (No Spaces allowed!)")
                .setValue(preset.getName())
                .setMinLength(1)
                .setMaxLength(25)
                .build();

        TextInput daysInput = TextInput.create("preset-days", "Duration in Days", TextInputStyle.SHORT)
                .setPlaceholder("Number of Days (-1 == infinity)")
                .setValue(String.valueOf(preset.getDays()))
                .setRequired(false)
                .setMinLength(1)
                .setMaxLength(5)
                .build();

        TextInput hoursInput = TextInput.create("preset-hours", "Duration in Hours", TextInputStyle.SHORT)
                .setPlaceholder("Number of Hours (-1 == infinity)")
                .setValue(String.valueOf(preset.getHours()))
                .setRequired(false)
                .setMinLength(1)
                .setMaxLength(5)
                .build();

        TextInput minutesInput = TextInput.create("preset-minutes", "Duration in Minutes", TextInputStyle.SHORT)
                .setPlaceholder("Number of Minutes (-1 == infinity)")
                .setValue(String.valueOf(preset.getMinutes()))
                .setRequired(false)
                .setMinLength(1)
                .setMaxLength(5)
                .build();


        return Modal.create("preset-duration", "Set Duration")
                .addComponents(ActionRow.of(presetNameInput), ActionRow.of(daysInput), ActionRow.of(hoursInput),ActionRow.of(minutesInput))
                .build();
    }

    public static ArrayList<ActionRow> getPresetActionRows(Preset preset){
        ArrayList<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(getScopeMenuForPreset(preset).build()));
        actionRows.add(ActionRow.of(getPunishmentTypeMenuForPreset(preset).build()));
        actionRows.add(ActionRow.of(
                Button.primary("preset-edit", "Edit Preset"),
                Button.secondary("preset-duration", "Set Duration"),
                Button.danger("preset-delete", "Delete Preset")));

        return actionRows;
    }

    public static long convertDurationToSeconds(int days, int hours, int minutes){
        long totalSeconds = 0;

        totalSeconds += ((long) days * 24 * 60 * 60);
        totalSeconds += ((long) hours * 60 * 60);
        totalSeconds += ((long) minutes * 60);

        return totalSeconds;
    }
}
