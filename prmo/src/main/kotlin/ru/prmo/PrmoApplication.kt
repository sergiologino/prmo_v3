package ru.prmo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrmoApplication

fun main(args: Array<String>) {
	runApplication<PrmoApplication>(*args)
}
