package org.pkaq.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
class RabbitController {

    @Autowired
    var loadBalancerClient: LoadBalancerClient? = null;

    @Autowired
    var restTemplate: RestTemplate? = null

    @RequestMapping("/jump")
    fun say(): String {
        var microServiceNode = "tiger"
        var serviceName = "say"
        var serviceInstance  = loadBalancerClient!!.choose(microServiceNode)
        var url = "http://"+serviceInstance.host+":"+serviceInstance.port+"/"+serviceName
        return restTemplate!!.getForObject(url, String::class.java)+" - > Rabbit"

    }
}
