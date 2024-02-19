package me.aragot.hglmoderation.discord.actions;

import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.admin.preset.PresetHandler;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.tools.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

// To Do:
// Add Duration button handling
// Add StringSelectMenu with 1 Option Max for Punishment Type
public class ActionHandler extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        String menuId = event.getSelectMenu().getId();
        // its the /preset menu
        if(menuId.equalsIgnoreCase("preset-picker")){
            Preset preset = PresetHandler.instance.getPresetByName(event.getSelectedOptions().get(0).getLabel());

            if(preset == null){
                if(!event.getInteraction().getValues().get(0).equalsIgnoreCase("preset-add")){
                    event.replyEmbeds(
                            HGLBot.getEmbedTemplate(ResponseType.ERROR, "Couldn't find this preset, please try reopening your PresetGUI. Picked Label: " + event.getSelectedOptions().get(0).getLabel()).build()
                    ).setEphemeral(true).queue();
                    return;
                }

                Modal modal = getModal();

                event.replyModal(modal).queue();
                return;
            }

            event.replyEmbeds(getPresetEmbed(preset).build())
                    .addActionRow(getSelectMenuForPreset(preset).build())
                    .addComponents(ActionRow.of(
                            Button.primary("preset-edit", "Edit Preset"),
                            Button.secondary("preset-duration", "Edit Duration"),
                            Button.danger("preset-delete", "Delete Preset")))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        //This handles the preset scopes
        if(menuId.endsWith("-scopes")){
            String presetName = menuId.substring(0, menuId.length() - 7);
            Preset preset = PresetHandler.instance.getPresetByName(presetName);
            if(preset == null){
                event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find a preset with the name: " + presetName).build()).queue();
                return;
            }
            preset.setReasoningScope(getReasoningScopeForValues(presetName, event.getValues()));

            PresetHandler.savePresets();

            event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully changed reasoning scope for " + presetName).build()).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event){
        MessageEmbed eb;
        switch(event.getModalId()){
            case "preset-create":
                eb = createPresetFromModal(event).build();
                if(eb.getTitle().equalsIgnoreCase("Success!")){
                    String presetName = event.getValue("preset-name").getAsString();
                    event.getChannel().sendMessageEmbeds(eb).queue();
                    Preset preset = PresetHandler.instance.getPresetByName(presetName);
                    event.replyEmbeds(getPresetEmbed(preset).build())
                            .addActionRow(getSelectMenuForPreset(preset).build())
                            .addComponents(ActionRow.of(
                                    Button.primary("preset-edit", "Edit Preset"),
                                    Button.secondary("preset-duration", "Edit Duration"),
                                    Button.danger("preset-delete", "Delete Preset"))
                            )
                            .setEphemeral(true)
                            .queue();
                } else {
                    event.replyEmbeds(eb).queue();
                }
                break;
            case "preset-edit":
                eb = editPresetFromModal(event).build();
                if(eb.getTitle().equalsIgnoreCase("Success!")){
                    String presetName = event.getValue("preset-name").getAsString();
                    event.getChannel().sendMessageEmbeds(eb).queue();
                    Preset preset = PresetHandler.instance.getPresetByName(presetName);
                    event.replyEmbeds(getPresetEmbed(preset).build())
                            .addActionRow(getSelectMenuForPreset(preset).build())
                            .addComponents(ActionRow.of(
                                    Button.primary("preset-edit", "Edit Preset"),
                                    Button.danger("preset-delete", "Delete Preset"))
                            )
                            .setEphemeral(true)
                            .queue();
                } else {
                    event.replyEmbeds(eb).queue();
                }
                break;
            default:
                event.replyEmbeds(
                        HGLBot.getEmbedTemplate(
                                ResponseType.ERROR,
                                "Hey I don't remember that Modal. That's weird. Please report this if you expected a different reply."
                                ).build()).queue();
                break;
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event){
        Preset preset;
        Modal modal;
        switch(event.getButton().getId()){
            case "preset-duration":
                preset = PresetHandler.instance.getPresetByName(event.getMessage().getEmbeds().get(0).getTitle());

                if(preset == null){
                    event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find this preset. Please refresh the PresetGUI").build())
                            .setEphemeral(true).queue();
                    return;
                }

                modal = getDurationModal(preset);

                event.replyModal(modal).queue();

                break;
            case "preset-edit":
                preset = PresetHandler.instance.getPresetByName(event.getMessage().getEmbeds().get(0).getTitle());

                if(preset == null){
                    event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find this preset. Please refresh the PresetGUI").build())
                            .setEphemeral(true).queue();
                    return;
                }

                modal = getModalForPreset(preset);

                event.replyModal(modal).queue();
                break;

            case "preset-delete":
                preset = PresetHandler.instance.getPresetByName(event.getMessage().getEmbeds().get(0).getTitle());
                if(preset == null){
                    event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find this preset. Please refresh the PresetGUI").build())
                            .setEphemeral(true).queue();
                    return;
                }

                event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.DEFAULT, "Are you sure you want to delete the preset '" + preset.getName() + "'?").build())
                        .addComponents(ActionRow.of(
                                Button.success("preset-keep", "Keep Preset"),
                                Button.danger("preset-delete-final", "I am sure, I know what I'm doing.")))
                        .setEphemeral(true).queue();
                break;
            case "preset-keep":
                event.getMessage().delete().queue();
                break;

            case "preset-delete-final":
                String presetName = event.getMessage().getEmbeds().get(0).getDescription().split("'")[1];
                preset = PresetHandler.instance.getPresetByName(presetName);
                if(preset == null){
                    event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find this preset. Please refresh the PresetGUI").build())
                            .setEphemeral(true).queue();
                    return;
                }

                PresetHandler.instance.removePreset(preset);
                PresetHandler.savePresets();
                event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully removed the preset.").build())
                        .setEphemeral(true).queue();
                break;

            default:
                event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Couldn't find the button with the ID: " + event.getButton().getId()).build())
                        .setEphemeral(true).queue();
                break;
        }
    }

    public EmbedBuilder editPresetFromModal(ModalInteractionEvent event){
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

    public EmbedBuilder createPresetFromModal(ModalInteractionEvent event){
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

    public EmbedBuilder getPresetEmbed(Preset preset){
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

    public StringSelectMenu.Builder getSelectMenuForPreset(Preset preset){
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

    public ArrayList<Reasoning> getReasoningScopeForValues(String presetName, List<String> values){
        ArrayList<Reasoning> reasoningScope = new ArrayList<>();
        for(String value : values){
            String reasoning = value.replace(presetName + "-", "").toUpperCase();
            reasoningScope.add(Reasoning.valueOf(reasoning));
        }
        return reasoningScope;
    }

    private Modal getModal(){
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

    private Modal getModalForPreset(Preset preset){
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

    //Needs to be implemented
    private Modal getDurationModal(Preset preset){
        return null;
    }
}
