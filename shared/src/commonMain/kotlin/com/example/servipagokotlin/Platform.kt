package com.example.servipagokotlin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform