package com.upipe.app.database.stream

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.upipe.app.database.stream.model.StreamEntity
import com.upipe.app.database.stream.model.StreamStateEntity

data class StreamWithState(
    @Embedded
    val stream: StreamEntity,

    @ColumnInfo(name = StreamStateEntity.STREAM_PROGRESS_MILLIS)
    val stateProgressMillis: Long?
)
