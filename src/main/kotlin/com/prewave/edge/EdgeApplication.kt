package com.prewave.edge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main spring boot application class.
 */
@SpringBootApplication
class EdgeApplication

/**
 * Main method to run app.
 */
fun main(args: Array<String>) {
    runApplication<EdgeApplication>(*args)
}
