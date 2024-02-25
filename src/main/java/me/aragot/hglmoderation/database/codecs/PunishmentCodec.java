package me.aragot.hglmoderation.database.codecs;

import me.aragot.hglmoderation.data.Reasoning;
import me.aragot.hglmoderation.data.punishments.Punishment;
import me.aragot.hglmoderation.data.punishments.PunishmentType;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.ArrayList;

public class PunishmentCodec implements Codec<Punishment> {

    @Override
    public void encode(final BsonWriter writer, final Punishment punishment, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("_id", punishment.getId());
        writer.writeInt64("issuedAt", punishment.getIssuedAtTimestamp());
        writer.writeString("issuedTo", punishment.getPunishedUUID());
        writer.writeString("issuedBy", punishment.getIssuerUUID());
        writer.writeInt64("endsAt", punishment.getEndsAtTimestamp());
        writer.writeString("reason", punishment.getReasoning().name());
        writer.writeString("note", punishment.getNote());
        writer.writeStartArray("types");
        for (PunishmentType type : punishment.getTypes()) {
            writer.writeString(type.name());
        }
        writer.writeEndArray();
        writer.writeEndDocument();
    }

    @Override
    public Punishment decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartDocument();
        String id = reader.readString("_id");
        long issuedAt = reader.readInt64("issuedAt");
        String issuedTo = reader.readString("issuedTo");
        String issuedBy = reader.readString("issuedBy");

        long endsAt = reader.readInt64("endsAt");
        Reasoning reason = Reasoning.valueOf(reader.readString("reason"));
        String note = reader.readString("note");

        ArrayList<PunishmentType> types = new ArrayList<>();

        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            types.add(PunishmentType.valueOf(reader.readString()));
        }
        reader.readEndArray();


        reader.readEndDocument();
        return new Punishment(id, issuedAt, issuedTo, issuedBy, types, endsAt, reason, note);
    }

    @Override
    public Class<Punishment> getEncoderClass() {
        return Punishment.class;
    }
}