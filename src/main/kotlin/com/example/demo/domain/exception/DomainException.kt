package com.example.demo.domain.exception

import com.example.demo.domain.exception.code.ErrorCode

open class DomainException(val errorCode: ErrorCode) : RuntimeException()

class UnknownException : DomainException(ErrorCode.UNKNOWN_ERROR)