package com.example.demo.crawler

import com.example.demo.domain.model.SkiResort

interface Crawler {
    fun crawl(): SkiResort
}