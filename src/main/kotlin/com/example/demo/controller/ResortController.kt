package com.example.demo.controller

import com.example.demo.model.SkiResort
import com.example.demo.model.Slope
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class ResortController {

    private val dummySlopes = listOf(
            Slope(UUID.randomUUID(), "제우스 (Zeus) Ⅰ", false, false),
            Slope(UUID.randomUUID(), "제우스 (Zeus) Ⅱ", false, false),
            Slope(UUID.randomUUID(), "아테나 (Athena) Ⅲ", true, true)
    )

    private val dummyResorts = listOf(
            SkiResort(UUID.randomUUID(), "Resort 1", dummySlopes.filter { it.slopeName.startsWith("제우스") }),
            SkiResort(UUID.randomUUID(), "Resort 2", dummySlopes.filter { it.slopeName.startsWith("아테나") }),
    )

    @GetMapping
    fun listResorts(): List<SkiResort> {
        return dummyResorts
    }

    @GetMapping("/{id}")
    fun getResort(@PathVariable id: UUID): SkiResort {
        return dummyResorts.find { it.skiResortId == id }
                ?: throw Exception("Resort not found!")
    }
}