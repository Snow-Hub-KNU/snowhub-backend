package com.example.demo.crawler

import com.example.demo.domain.exception.CrawlingNotAllowedException
import com.example.demo.domain.model.SkiResort
import org.jsoup.Jsoup
import utils.CrawlerUtils
import java.util.UUID

class SkiResortCrawler : Crawler {
    override fun crawl(url: String): SkiResort {
        val crawlerUtils = CrawlerUtils()

        if (!crawlerUtils.isCrawlable(url)) {
            throw CrawlingNotAllowedException()
        }

        val doc = Jsoup.connect(url).get()

        // 웹사이트의 HTML 구조에 따라 선택자를 변경해야 합니다.
        val resortName = doc.select("div.resortName").first()?.text()
        val openHours = doc.select("div.openHours").first()?.text()
        val closeHours = doc.select("div.closeHours").first()?.text()

        return SkiResort(UUID.randomUUID(), resortName, openHours, closeHours)
    }
}