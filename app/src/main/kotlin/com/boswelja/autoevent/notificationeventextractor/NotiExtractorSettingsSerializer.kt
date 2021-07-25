package com.boswelja.autoevent.notificationeventextractor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream

val Context.notiExtractorSettingsStore: DataStore<NotiExtractorSettings> by dataStore(
    fileName = "notiextractorsettings.pb",
    serializer = NotiExtractorSettingsSerializer
)

object NotiExtractorSettingsSerializer : Serializer<NotiExtractorSettings> {
    override val defaultValue: NotiExtractorSettings = NotiExtractorSettings(
        blocklist = emptyList()
    )

    override suspend fun readFrom(input: InputStream): NotiExtractorSettings {
        return NotiExtractorSettings.ADAPTER.decode(input)
    }

    override suspend fun writeTo(t: NotiExtractorSettings, output: OutputStream) {
        NotiExtractorSettings.ADAPTER.encode(output, t)
    }
}
