package me.aragot.hglmoderation.service.database.codecs;

import me.aragot.hglmoderation.entity.Reasoning;
import me.aragot.hglmoderation.entity.reports.Priority;
import me.aragot.hglmoderation.entity.reports.Report;
import me.aragot.hglmoderation.entity.reports.ReportState;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import java.util.ArrayList;

public class ReportCodec implements Codec<Report> {

    @Override
    public void encode(final BsonWriter writer, final Report report, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString("_id", report.getId());
        writer.writeString("reportedUUID", report.getReportedUUID());
        writer.writeString("reporterUUID", report.getReporterUUID());
        writer.writeInt64("submittedAt", report.getSubmittedAt());
        writer.writeString("reasoning", report.getReasoning().name());
        writer.writeString("state", report.getState().name());
        writer.writeString("priority", report.getPriority().name());
        writer.writeString("reviewedBy", report.getReviewedBy());
        writer.writeString("punishmentId", report.getPunishmentId());
        writer.writeString("discordLog", report.getDiscordLog());
        writer.writeStartArray("reportedUserMessages");
        for (String message : report.getReportedUserMessages()) {
            writer.writeString(message);
        }
        writer.writeEndArray();
        writer.writeEndDocument();
    }

    @Override
    public Report decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartDocument();
        String id = reader.readString("_id");
        String reportedUUID = reader.readString("reportedUUID");
        String reporterUUID = reader.readString("reporterUUID");
        long submittedAt = reader.readInt64("submittedAt");
        Reasoning reasoning = Reasoning.valueOf(reader.readString("reasoning"));
        ReportState state = ReportState.valueOf(reader.readString("state"));
        Priority priority = Priority.valueOf(reader.readString("priority"));
        String reviewedBy = reader.readString("reviewedBy");
        String punishmentId = reader.readString("punishmentId");
        String discordLog = reader.readString("discordLog");
        ArrayList<String> reportedUserMessages = new ArrayList<>();
        reader.readStartArray();
        while (reader.readBsonType() != org.bson.BsonType.END_OF_DOCUMENT) {
            reportedUserMessages.add(reader.readString());
        }
        reader.readEndArray();
        reader.readEndDocument();
        return new Report(id, reportedUUID, reporterUUID, submittedAt, reasoning, state, priority, reviewedBy, punishmentId, discordLog, reportedUserMessages);
    }

    @Override
    public Class<Report> getEncoderClass() {
        return Report.class;
    }

}