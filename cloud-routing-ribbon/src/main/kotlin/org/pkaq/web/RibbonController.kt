package org.pkaq.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
class RibbonController {

    @Autowired
    var restTemplate: RestTemplate? = null

    @RequestMapping("/Ribbon")
    fun say(): String {
        val microServiceNode = "tiger"
        val serviceName = "say"
        val url = "http://${microServiceNode}/${serviceName}"

        return restTemplate!!.getForObject(url, String::class.java)+" - > Ribbon"

    }
}
