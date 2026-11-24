package com.example.medclock.reminders


data class DraftValue<T>(
    val saved: T,
    val draft: T = saved
) {
    val hasChanges: Boolean get() = draft != saved

    fun update(value: T): DraftValue<T> = copy(draft = value)

    fun discard(): DraftValue<T> = copy(draft = saved)

    fun commit(): DraftValue<T> = copy(saved = draft)
}
