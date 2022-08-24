package com.worldcup.bracket

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.nio.file.Paths
import com.google.gson.Gson; 
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken
import com.worldcup.bracket.Entity.Group


@RestController
class HelloWorld {
    @GetMapping
    fun hello() {
        println("hello")
    }
}