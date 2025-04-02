package com.prewave.edge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EdgeApplication

fun main(args: Array<String>) {
	runApplication<EdgeApplication>(*args)
}
