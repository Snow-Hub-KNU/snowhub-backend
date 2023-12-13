package com.example.demo.model

import java.util.UUID

data class Slope(
        val slopeId: UUID,
        val slopeName: String,
        val dayStatus: Boolean,
        val nightStatus: Boolean,
)