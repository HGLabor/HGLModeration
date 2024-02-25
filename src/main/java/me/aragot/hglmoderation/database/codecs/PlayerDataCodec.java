package me.aragot.hglmoderation.database.codecs;

import me.aragot.hglmoderation.data.Notification;
import me.aragot.hglmoderation.data.PlayerData;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;

public class PlayerDataCodec implements Codec<PlayerData> {

    @Override
    public void encode(final BsonWriter writer, final PlayerData playerData, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("_id", playerData.getPlayerId());
        writer.writeInt32("reportScore", playerData.getReportScore());
        writer.writeInt32("punishmentScore", playerData.getPunishmentScore());
        writer.writeString("discordId", playerData.getDiscordId());
        writer.writeStartArray("notifications");
        for (Notification notification : playerData.getNotifications()) {
            writer.writeString(notification.name());
        }
        writer.writeEndArray();
        writer.writeStartArray("punishments");
        for (String punishment : playerData.getPunishments()) {
            writer.writeString(punishment);
        }
        writer.writeEndArray();
        writer.writeEndDocument();
    }

    @Override
    public PlayerData decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartDocument();
        String playerId = reader.readString("_id");
        int reportScore = reader.readInt32("reportScore");
        int punishmentScore = reader.readInt32("punishmentScore");
        String discordId = reader.readString("discordId");
        ArrayList<Notification> notifications = new ArrayList<>();
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            notifications.add(Notification.valueOf(reader.readString()));
        }
        reader.readEndArray();
        ArrayList<String> punishments = new ArrayList<>();
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            punishments.add(reader.readString());
        }
        reader.readEndArray();
        reader.readEndDocument();

        return new PlayerData(playerId, reportScore, punishmentScore, discordId, notifications, punishments);
    }

    @Override
    public Class<PlayerData> getEncoderClass() {
        return PlayerData.class;
    }
}