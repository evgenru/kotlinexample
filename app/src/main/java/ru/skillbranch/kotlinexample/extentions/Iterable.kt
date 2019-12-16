package ru.skillbranch.kotlinexample.extentions

fun <T> List<T>.dropLastUntil(predicate: (T) -> Boolean): List<T> {
    val index = this.indexOfLast { predicate(it) }
    return if (index > 0) {
        this.dropLast(size - index)
    } else {
        this
    }
}