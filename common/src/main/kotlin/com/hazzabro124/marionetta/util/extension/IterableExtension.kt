package com.hazzabro124.marionetta.util.extension

// TODO: replace with Sequence<T>

fun <T> Iterable<T>.with(other: Iterable<T>): Iterable<T> =
    object: Iterable<T> {
        override fun iterator(): Iterator<T> =
            object: Iterator<T> {
                val it1 = this@with.iterator()
                val it2 = other.iterator()

                override fun hasNext(): Boolean = it1.hasNext() && it2.hasNext()

                override fun next(): T = if (it1.hasNext()) it1.next() else it2.next()
            }
    }