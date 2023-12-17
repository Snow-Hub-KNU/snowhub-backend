package com.example.demo.crawler

import com.example.demo.domain.model.SkiResort

class VivaldiParkCrawler : Crawler {
    override fun crawl(): SkiResort {
        return SkiResort(
                skiResortId = java.util.UUID.randomUUID(),
                resortName = "비발디파크",
                slopes = listOf()
        )
    }
}