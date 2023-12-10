package com.example.demo.domain.model

import java.util.UUID

data class Slope(
        val slopeId: UUID,
        val slopeName: String,
        val dayStatus: Boolean,
        val nightStatus: Boolean,
)
