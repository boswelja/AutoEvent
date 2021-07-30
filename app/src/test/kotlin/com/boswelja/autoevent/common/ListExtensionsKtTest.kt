package com.boswelja.autoevent.common

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class ListExtensionsKtTest {

    @Test
    fun `secondOrNull returns the second item in a list`() {
        val list = listOf("one", "two", "three")
        val result = list.secondOrNull()
        expectThat(result).isEqualTo("two")
    }

    @Test
    fun `secondOrNull returns null if there's less than two items`() {
        val list = listOf("one")
        val result = list.secondOrNull()
        expectThat(result).isNull()
    }
}
