package org.pkaq

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.config.server.EnableConfigServer

// 激活该应用为配置文件服务器
@EnableConfigServer
@SpringBootApplication
open class Booter: CommandLineRunner{
    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("  --- --- --- [ config server started ] --- --- ---  ")
    }
}

fun main(args: Array<String>) {
    org.springframework.boot.SpringApplication.run(org.pkaq.Booter::class.java, *args)
}