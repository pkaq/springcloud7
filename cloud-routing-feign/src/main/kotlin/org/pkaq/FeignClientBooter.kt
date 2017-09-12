package org.pkaq

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.feign.EnableFeignClients

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
open class RibbonClientBooter: CommandLineRunner{
    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("  --- --- --- [ Feign client started ] --- --- ---  ")
    }
}

fun main(args: Array<String>) {
    org.springframework.boot.SpringApplication.run(org.pkaq.RibbonClientBooter::class.java, *args)
}