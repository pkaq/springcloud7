package org.pkaq

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class DockerBooter : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("Docker started")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            org.springframework.boot.SpringApplication.run(org.pkaq.DockerBooter::class.java, *args)
        }
    }
}
