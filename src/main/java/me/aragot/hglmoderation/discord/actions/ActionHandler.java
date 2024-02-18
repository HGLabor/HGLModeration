package me.aragot.hglmoderation.discord.actions;

import me.aragot.hglmoderation.admin.preset.Preset;
import me.aragot.hglmoderation.admin.preset.PresetHandler;
import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.discord.HGLBot;
import me.aragot.hglmoderation.response.ResponseType;
import me.aragot.hglmoderation.tools.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import javax.annotation.Nonnull;

//Modal Handling missing
public class ActionHandler extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {

        if(event.getSelectMenu().getId().equalsIgnoreCase("preset-picker")){
            Preset preset = PresetHandler.instance.getPresetByName(event.getId());

            if(preset == null){
                if(!event.getInteraction().getValues().get(0).equalsIgnoreCase("preset-add")){
                    event.replyEmbeds(
                            HGLBot.getEmbedTemplate(ResponseType.ERROR, "Couldn't find this preset, please try reopening your PresetGUI").build()
                    ).setEphemeral(true).queue();
                    return;
                }

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
                        .setPlaceholder("Preset Ends at Number")
                        .setMinLength(1)
                        .setMaxLength(5)
                        .build();

                TextInput presetDescriptionInput = TextInput.create("preset-description", "Preset Description", TextInputStyle.PARAGRAPH)
                        .setPlaceholder("Preset description such as \"Lowest Punishment Preset for Chat-Based Punishments\"")
                        .setMinLength(10)
                        .setMaxLength(150)
                        .build();

                Modal modal = Modal.create("preset-create", "Create new Preset")
                        .addComponents(ActionRow.of(presetNameInput), ActionRow.of(startInput), ActionRow.of(endInput), ActionRow.of(presetDescriptionInput))
                        .build();

                event.replyModal(modal).queue();

            }

            StringSelectMenu.Builder reasonScope = StringSelectMenu.create(preset.getPresetName().toLowerCase() + "-scopes");

            for(Reasoning reason : preset.getReasoningScope()){
                reasonScope.addOption(StringUtils.capitalize(reason.name().toLowerCase()),
                        preset.getPresetName().toLowerCase() + "-" + StringUtils.capitalize(reason.name().toLowerCase()),
                        Emoji.fromUnicode("\uD83E\uDEAC"));
            }

            event.replyEmbeds(getPresetEmbed(preset).build()).addActionRow(reasonScope.build()).queue();
            return;
        }

    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event){
        switch(event.getModalId()){
            case "preset-create":
                event.replyEmbeds(createPresetFromModal(event).build()).queue();
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

    public EmbedBuilder createPresetFromModal(ModalInteractionEvent event){
        EmbedBuilder eb = new EmbedBuilder();

        return eb;
    }


    public EmbedBuilder getPresetEmbed(Preset preset){
        EmbedBuilder eb = HGLBot.getEmbedTemplate(ResponseType.DEFAULT);
        String endsAt = preset.getEnd() == -1 ? "Infinity" : String.valueOf(preset.getEnd());
        eb.setTitle(preset.getPresetName());
        eb.setDescription(
                        "Description:\n" +
                        preset.getPresetDescription() + "\n\n" +
                        "Starts at Punishment Score > " + preset.getStart() + "\n" +
                        "Ends at Punishment Score < " + endsAt);
        return eb;
    }

}
