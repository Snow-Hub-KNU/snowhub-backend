package com.example.demo.crawler

import com.example.demo.domain.exception.CrawlingNotAllowedException
import com.example.demo.domain.model.SkiResort
import com.example.demo.domain.model.Slope
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import utils.CrawlerUtils
import java.util.UUID

class High1Crawler : Crawler {
    override fun crawl(): SkiResort {
        val crawlerUtils = CrawlerUtils()

        if (!crawlerUtils.isCrawlable("https://www.high1.com")) {
            throw CrawlingNotAllowedException()
        }

        val url = "https://www.high1.com/ski/slopeView.do?key=748&mode=p"
        val doc = Jsoup.connect(url).get()
        val slopes = getSlopes(doc)

        return SkiResort(
                skiResortId = UUID.randomUUID(),
                resortName = "하이원리조트",
                slopes = slopes
        )
    }

    private fun getSlopes (doc: Document): List<Slope> {
        var i = 1
        var slopeName = ""
        val slopes = mutableListOf<Slope>()

        while (true) {
            val slopeNameSelector = createSelector(i, 1)
            val slopeNameElements: Elements = doc.select(slopeNameSelector)

            if (slopeNameElements.isEmpty()) {
                break
            }

            val firstColumn = slopeNameElements.text()

            val slopeNumberSelector = createSelector(i, if (firstColumn.contains("(")) 2 else 1)
            val dayStatusColumn = if (firstColumn.contains("(")) 3 else 2
            val nightStatusColumn = if (firstColumn.contains("(")) 4 else 3

            if (firstColumn.contains("(")) {
                slopeName = firstColumn
            }

            val slopeNumberElements: Elements = doc.select(slopeNumberSelector)
            val slopeNumber = slopeNumberElements.text()

            val dayStatus = getStatus(doc, i, dayStatusColumn)
            val nightStatus = getStatus(doc, i, nightStatusColumn)

            slopes.add(Slope(UUID.randomUUID(), "$slopeName $slopeNumber", dayStatus, nightStatus))

            i++
        }

        return slopes
    }

    private fun createSelector(row: Int, column: Int): String {
        return "#contents > div > div.p-wrap > div > table > tbody > tr:nth-child($row) > td:nth-child($column)"
    }

    private fun getStatus(doc: Document, row: Int, column: Int): Boolean {
        val statusSelector = createSelector(row, column) + " > span"
        val statusElements: Elements = doc.select(statusSelector)
        val status = if (statusElements.isEmpty()) "" else statusElements.text()

        return status == "OPEN"
    }
}