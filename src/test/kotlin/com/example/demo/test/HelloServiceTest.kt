package com.example.demo.test

import com.example.demo.service.HelloService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class HelloServiceTest {

    @Autowired
    private lateinit var helloService: HelloService

    @Test
    fun helloWorld() {
        val result = helloService.getGreeting()
        assertEquals("안녕 세상아", result)
    }
}
