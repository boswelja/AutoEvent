package com.boswelja.autoevent

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

class InMemoryDataStore<T>(
    initialValue: T
) : DataStore<T> {
    private val _data = MutableStateFlow(initialValue)
    override val data: Flow<T>
        get() = _data

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        val oldData = data.first()
        val newData = transform(oldData)
        _data.emit(newData)
        return newData
    }
}
