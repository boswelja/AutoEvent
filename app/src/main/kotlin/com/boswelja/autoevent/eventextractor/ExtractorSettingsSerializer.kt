package com.boswelja.autoevent.eventextractor

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream

val Context.extractorSettingsDataStore: DataStore<ExtractorSettings> by dataStore(
    fileName = "extractorsettings.pb",
    serializer = ExtractorSettingsSerializer
)

object ExtractorSettingsSerializer : Serializer<ExtractorSettings> {

    override val defaultValue: ExtractorSettings = ExtractorSettings(
        language = ExtractorSettings.ExtractorLanguage.DETECT,
        extractLocation = true,
        extractEmails = true,
        ignoreAllDayEvents = false
    )

    override suspend fun readFrom(input: InputStream): ExtractorSettings {
        return ExtractorSettings.ADAPTER.decode(input)
    }

    override suspend fun writeTo(t: ExtractorSettings, output: OutputStream) {
        ExtractorSettings.ADAPTER.encode(output, t)
    }
}
