package com.boswelja.autoevent.common

fun <E> List<E>.secondOrNull(): E? = if (this.size >= 2) this[1] else null
