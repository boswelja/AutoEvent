package com.boswelja.autoevent.eventextractor

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.entityextraction.EntityAnnotation
import com.google.mlkit.nl.entityextraction.EntityExtractionParams
import com.google.mlkit.nl.entityextraction.EntityExtractor

class DummyEntityExtractor : EntityExtractor {
    private var isModelDownloaded: Boolean = false

    var isClosed = false
        private set

    var lastAnnotateParams: EntityExtractionParams? = null
        private set

    val annotationResults = mutableListOf<EntityAnnotation>()

    override fun close() {
        isClosed = true
    }

    override fun annotate(text: String): Task<List<EntityAnnotation>> {
        return annotate(EntityExtractionParams.Builder(text).build())
    }

    override fun annotate(params: EntityExtractionParams): Task<List<EntityAnnotation>> {
        lastAnnotateParams = params
        return Tasks.forResult(annotationResults)
    }

    override fun downloadModelIfNeeded(): Task<Void> {
        isModelDownloaded = true
        return Tasks.forResult(null)
    }

    override fun downloadModelIfNeeded(
        conditions: DownloadConditions
    ): Task<Void> = downloadModelIfNeeded()

    override fun isModelDownloaded(): Task<Boolean> = Tasks.forResult(isModelDownloaded)

    fun reset() {
        lastAnnotateParams = null
        isClosed = false
        isModelDownloaded = false
    }
}
