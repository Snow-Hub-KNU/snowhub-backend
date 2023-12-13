package com.example.demo.model

import java.util.UUID

data class SkiResort(
        val skiResortId: UUID,
        val resortName: String,
        val slopes: List<Slope> = listOf(),
)
