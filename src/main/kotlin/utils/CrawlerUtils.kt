package utils

import org.jsoup.Jsoup

class CrawlerUtils {
    fun isCrawlable(url: String): Boolean {
        val baseUrl = getBaseUrl(url)
        val robotsTxtUrl = "$baseUrl/robots.txt"
        val robotsTxtDoc = Jsoup.connect(robotsTxtUrl).ignoreContentType(true).get()
        val lines = robotsTxtDoc.body().text().split("\n")

        for (line in lines) {
            val parts = line.split(": ").map { it.trim() }
            if (parts.size == 2 && parts[0] == "Disallow") {
                val disallowedUrl = if (parts[1].startsWith("/")) "$baseUrl${parts[1]}" else parts[1]
                if (url.startsWith(disallowedUrl)) return false
            }
        }
        return true
    }

    private fun getBaseUrl(url: String): String {
        val parts = url.split("/")
        return parts[0] + "//" + parts[2]
    }
}