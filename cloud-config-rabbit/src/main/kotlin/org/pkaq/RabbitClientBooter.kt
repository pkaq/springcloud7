package org.pkaq

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableEurekaClient
open class RabbitClientBooter: CommandLineRunner{
    @Autowired
    val builder: RestTemplateBuilder? = null

    @Bean
    open fun restTemplate(): RestTemplate {
        return builder!!.build()
    }

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("  --- --- --- [ Rabbit client started ] --- --- ---  ")
    }
}

fun main(args: Array<String>) {
    org.springframework.boot.SpringApplication.run(org.pkaq.RabbitClientBooter::class.java, *args)
}