package com.example.demo.domain.model

import java.util.UUID

data class SkiResort(
        val skiResortId: UUID,
        val resortName: String,
        val openHours: String,
        val closeHours: String,
        val slopes: List<Slope> = listOf(),
)