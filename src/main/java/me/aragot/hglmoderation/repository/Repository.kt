package me.aragot.hglmoderation.repository

import me.aragot.hglmoderation.HGLModeration
import me.aragot.hglmoderation.service.database.ModerationDB

open class Repository(db: ModerationDB = HGLModeration.instance.database) {
    protected val database = db
}