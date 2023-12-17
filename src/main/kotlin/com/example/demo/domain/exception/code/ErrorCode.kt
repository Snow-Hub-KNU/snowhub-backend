package com.example.demo.domain.exception.code

enum class ErrorCode(
        val code: String,
        val message: String,
) {
    UNKNOWN_ERROR("unknown-000", "알 수 없는 오류가 발생했습니다."),

    CRAWLING_NOT_ALLOWED("crawling-000", "크롤링이 허용되지 않았습니다.")
    ;

    companion object {
        private val map = values().associateBy { it.code }

        fun valueOf(code: String?): ErrorCode = map[code] ?: UNKNOWN_ERROR
    }
}