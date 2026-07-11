/*
 * SPDX-FileCopyrightText: 2017-2024 NewPipe contributors <https://newpipe.net>
 * SPDX-FileCopyrightText: 2025 NewPipe e.V. <https://newpipe-ev.de>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.upipe.app.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.upipe.app.database.feed.dao.FeedDAO
import com.upipe.app.database.feed.dao.FeedGroupDAO
import com.upipe.app.database.feed.model.FeedEntity
import com.upipe.app.database.feed.model.FeedGroupEntity
import com.upipe.app.database.feed.model.FeedGroupSubscriptionEntity
import com.upipe.app.database.feed.model.FeedLastUpdatedEntity
import com.upipe.app.database.history.dao.SearchHistoryDAO
import com.upipe.app.database.history.dao.StreamHistoryDAO
import com.upipe.app.database.history.model.SearchHistoryEntry
import com.upipe.app.database.history.model.StreamHistoryEntity
import com.upipe.app.database.playlist.dao.PlaylistDAO
import com.upipe.app.database.playlist.dao.PlaylistRemoteDAO
import com.upipe.app.database.playlist.dao.PlaylistStreamDAO
import com.upipe.app.database.playlist.model.PlaylistEntity
import com.upipe.app.database.playlist.model.PlaylistRemoteEntity
import com.upipe.app.database.playlist.model.PlaylistStreamEntity
import com.upipe.app.database.stream.dao.StreamDAO
import com.upipe.app.database.stream.dao.StreamStateDAO
import com.upipe.app.database.stream.model.StreamEntity
import com.upipe.app.database.stream.model.StreamStateEntity
import com.upipe.app.database.subscription.SubscriptionDAO
import com.upipe.app.database.subscription.SubscriptionEntity

@TypeConverters(Converters::class)
@Database(
    version = Migrations.DB_VER_9,
    entities = [
        SubscriptionEntity::class,
        SearchHistoryEntry::class,
        StreamEntity::class,
        StreamHistoryEntity::class,
        StreamStateEntity::class,
        PlaylistEntity::class,
        PlaylistStreamEntity::class,
        PlaylistRemoteEntity::class,
        FeedEntity::class,
        FeedGroupEntity::class,
        FeedGroupSubscriptionEntity::class,
        FeedLastUpdatedEntity::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedDAO(): FeedDAO
    abstract fun feedGroupDAO(): FeedGroupDAO
    abstract fun playlistDAO(): PlaylistDAO
    abstract fun playlistRemoteDAO(): PlaylistRemoteDAO
    abstract fun playlistStreamDAO(): PlaylistStreamDAO
    abstract fun searchHistoryDAO(): SearchHistoryDAO
    abstract fun streamDAO(): StreamDAO
    abstract fun streamHistoryDAO(): StreamHistoryDAO
    abstract fun streamStateDAO(): StreamStateDAO
    abstract fun subscriptionDAO(): SubscriptionDAO

    companion object {
        const val DATABASE_NAME: String = "newpipe.db"
    }
}
