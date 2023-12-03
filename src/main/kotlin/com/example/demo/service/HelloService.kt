package com.example.demo.service

import org.springframework.stereotype.Service
import java.util.Random

@Service
class HelloService {
    fun getGreeting(): String {
        return "안녕 세상아"
    }

    fun getRandomGreeting(): String {
        val random = Random().nextInt(10)
        return if (random < 5) {
            "안녕 세상아"
        } else {
            throw IllegalArgumentException("예외 발생!")
        }
    }
}
