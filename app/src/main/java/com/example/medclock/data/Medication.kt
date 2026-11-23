package com.example.medclock.data

import java.util.UUID

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val dosage: Double,
    val dosageUnit: String
)