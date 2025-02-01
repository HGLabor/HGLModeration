package me.aragot.hglmoderation.repository

import com.mongodb.MongoException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import me.aragot.hglmoderation.entity.punishments.Punishment
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class PunishmentRepository : Repository() {
    fun flushData(punishment: Punishment): Boolean {
        try {
            this.database.punishmentCollection.insertOne(punishment)
            return true
        } catch (x: MongoException) {
            return false
        }
    }

    fun updateData(punishment: Punishment): Boolean {
        return this.database.punishmentCollection.replaceOne(
            Filters.eq("_id", punishment.id),
            punishment
        ).wasAcknowledged()
    }

    fun getPunishmentById(id: String): Punishment? {
        return this.database.punishmentCollection.find(Filters.eq("_id", id)).first()
    }

    fun getActivePunishmentsFor(uuid: UUID, hostAddress: String): ArrayList<Punishment> {
        return this.database.punishmentCollection.find(
            Filters.and(
                Filters.or(
                    Filters.eq("endsAt", -1),
                    Filters.gt("endsAt", Instant.now().epochSecond)
                ),
                Filters.or(
                    Filters.eq("issuedTo", uuid.toString()),
                    Filters.eq("issuedTo", hostAddress)
                )
            )
        ).into(ArrayList())
    }

    fun getPunishmentsFor(uuid: UUID, hostAddress: String): ArrayList<Punishment> {
        return this.database.punishmentCollection.find(
            Filters.or(
                Filters.eq("issuedTo", uuid.toString()),
                Filters.eq("issuedTo", hostAddress)
            )
        ).sort(Sorts.descending("issuedAt")).into(java.util.ArrayList<Punishment>())
    }
}