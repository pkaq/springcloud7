package org.pkaq.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TigerController {
    @Autowired
    var discoveryClient: DiscoveryClient? = null

    @Value("\${tiger}")
    internal var roar: String? = null

    @Value("\${foo}")
    internal var foo: String? = null

    @RequestMapping("/say")
    fun say(): String {
        val services = "Services: " + discoveryClient!!.services
        println("Tiger Roar")
        return services+"-> foo "+ foo+" - >Tiger say:" + roar!!
    }
}
