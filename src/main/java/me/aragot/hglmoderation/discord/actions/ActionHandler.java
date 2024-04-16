package me.aragot.hglmoderation.discord.actions;

import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.admin.preset.PresetHandler;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.ResponseType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.Modal;

import javax.annotation.Nonnull;
import static me.aragot.hglmoderation.discord.actions.presets.PresetInteractions.*;

public class ActionHandler extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        String menuId = event.getSelectMenu().getId();
        //it's the /preset menu
        if (menuId.equalsIgnoreCase("preset-picker")) {
            Preset preset = PresetHandler.instance.getPresetByName(event.getSelectedOptions().get(0).getLabel());

            if (preset == null) {
                if (!event.getInteraction().getValues().get(0).equalsIgnoreCase("preset-add")) {
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
                    .addComponents(getPresetActionRows(preset))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        //This handles the preset scopes
        if (menuId.endsWith("-scopes")) {
            String presetName = menuId.substring(0, menuId.length() - 7);
            Preset preset = PresetHandler.instance.getPresetByName(presetName);
            if (preset == null) {
                event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find a preset with the name: " + presetName).build()).queue();
                return;
            }
            preset.setReasoningScope(getReasoningScopeForValues(presetName, event.getValues()));

            PresetHandler.savePresets();

            event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully changed reasoning scope for " + presetName).build()).queue();
        } else if (menuId.endsWith("-type")) {
            String presetName = menuId.substring(0, menuId.length() - 5);
            Preset preset = PresetHandler.instance.getPresetByName(presetName);
            if (preset == null) {
                event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find a preset with the name: " + presetName).build()).queue();
                return;
            }
            preset.setPunishmentsTypes(getPunishmentTypesForValues(presetName, event.getValues()));

            PresetHandler.savePresets();

            event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.SUCCESS, "Successfully changed Punishment Type for " + presetName).build()).queue();
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        MessageEmbed eb = HGLBot.getEmbedTemplate(ResponseType.ERROR, "This is not supposed to be displayed. Please report this bug. EndPoint: ModalInteraction").build();
        switch (event.getModalId()) {
            case "preset-create":
                eb = createPresetFromModal(event).build();
                if (eb.getTitle().equalsIgnoreCase("Success!")) {
                    String presetName = event.getValue("preset-name").getAsString();
                    event.getChannel().sendMessageEmbeds(eb).queue();
                    Preset preset = PresetHandler.instance.getPresetByName(presetName);
                    event.replyEmbeds(getPresetEmbed(preset).build())
                            .addComponents(getPresetActionRows(preset))
                            .setEphemeral(true)
                            .queue();
                    return;
                }
                break;

            case "preset-edit":
                eb = editPresetFromModal(event).build();
                if (eb.getTitle().equalsIgnoreCase("Success!")) {
                    String presetName = event.getValue("preset-name").getAsString();
                    event.getChannel().sendMessageEmbeds(eb).queue();
                    Preset preset = PresetHandler.instance.getPresetByName(presetName);
                    event.replyEmbeds(getPresetEmbed(preset).build())
                            .addComponents(getPresetActionRows(preset))
                            .setEphemeral(true)
                            .queue();
                    return;
                }
                break;

            case "preset-duration":
                eb = setDurationFromModal(event).build();
                if (eb.getTitle().equalsIgnoreCase("Success!")) {
                    String presetName = event.getValue("preset-name").getAsString();
                    event.getChannel().sendMessageEmbeds(eb).queue();
                    Preset preset = PresetHandler.instance.getPresetByName(presetName);
                    event.replyEmbeds(getPresetEmbed(preset).build())
                            .addComponents(getPresetActionRows(preset))
                            .setEphemeral(true)
                            .queue();
                    return;
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

        event.replyEmbeds(eb).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Preset preset;
        Modal modal;
        switch (event.getButton().getId()) {
            case "preset-duration":
                preset = PresetHandler.instance.getPresetByName(event.getMessage().getEmbeds().get(0).getTitle());

                if (preset == null) {
                    event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find this preset. Please refresh the PresetGUI").build())
                            .setEphemeral(true).queue();
                    return;
                }

                modal = getDurationModal(preset);

                event.replyModal(modal).queue();

                break;
            case "preset-edit":
                preset = PresetHandler.instance.getPresetByName(event.getMessage().getEmbeds().get(0).getTitle());

                if (preset == null) {
                    event.replyEmbeds(HGLBot.getEmbedTemplate(ResponseType.ERROR, "Cannot find this preset. Please refresh the PresetGUI").build())
                            .setEphemeral(true).queue();
                    return;
                }

                modal = getModalForPreset(preset);

                event.replyModal(modal).queue();
                break;

            case "preset-delete":
                preset = PresetHandler.instance.getPresetByName(event.getMessage().getEmbeds().get(0).getTitle());
                if (preset == null) {
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
                if (preset == null) {
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
}
