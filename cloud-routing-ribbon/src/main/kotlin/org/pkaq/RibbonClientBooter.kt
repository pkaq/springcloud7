package org.pkaq

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableEurekaClient
open class RibbonClientBooter: CommandLineRunner{
    @Autowired
    val builder: RestTemplateBuilder? = null

    @Bean
    //    该注解用来给RestTemplate标记，以使用负载均衡的客户端（LoadBalancerClient）来配置它。
    @LoadBalanced
    open fun restTemplate(): RestTemplate {
        return builder!!.build()
    }

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("  --- --- --- [ Ribbon client started ] --- --- ---  ")
    }
}

fun main(args: Array<String>) {
    org.springframework.boot.SpringApplication.run(org.pkaq.RibbonClientBooter::class.java, *args)
}