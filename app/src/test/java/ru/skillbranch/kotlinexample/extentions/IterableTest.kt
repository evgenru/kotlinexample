package ru.skillbranch.kotlinexample.extentions

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class IterableTest {

    @Test
    fun dropLastUntil() {

        assertArrayEquals(arrayOf(1), listOf(1, 2, 3).dropLastUntil { it == 2 }.toTypedArray())

        assertArrayEquals(
            arrayOf("House", "Nymeros", "Martell"),
            "House Nymeros Martell of Sunspear".split(" ")
                .dropLastUntil { it == "of" }.toTypedArray()
        )
    }
}

