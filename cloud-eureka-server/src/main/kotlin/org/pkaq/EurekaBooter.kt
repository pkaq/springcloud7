package org.pkaq

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

// 激活为eureka\服务器
@EnableEurekaServer
@SpringBootApplication
open class EurekaBooter: CommandLineRunner{

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("  --- --- --- [ eureka server started ] --- --- ---  ")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            org.springframework.boot.SpringApplication.run(org.pkaq.EurekaBooter::class.java, *args)
        }
    }

}

