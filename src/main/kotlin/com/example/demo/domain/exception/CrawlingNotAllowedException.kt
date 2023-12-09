package com.example.demo.domain.exception

import com.example.demo.domain.exception.code.ErrorCode

class CrawlingNotAllowedException : DomainException(ErrorCode.CRAWLING_NOT_ALLOWED)