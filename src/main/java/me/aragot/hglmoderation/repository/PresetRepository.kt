package me.aragot.hglmoderation.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOneModel
import com.mongodb.client.model.ReplaceOptions
import me.aragot.hglmoderation.admin.preset.Preset

class PresetRepository : Repository() {
    fun getAllPresets(): List<Preset> {
        return this.database.presetCollection.find().toList()
    }

    fun insertAll(presets: List<Preset>): Boolean {
        return this.database.presetCollection.insertMany(presets).wasAcknowledged()
    }

    fun saveAll(presets: List<Preset>): Boolean {
        if (presets.isEmpty()) return false

        val updates = presets.map { preset ->
            ReplaceOneModel(
                Filters.eq("_id", preset.id),
                preset,
                ReplaceOptions().upsert(true)
            )
        }

        val result = database.presetCollection.bulkWrite(updates)
        return result.wasAcknowledged()
    }
}