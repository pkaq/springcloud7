package org.pkaq.web

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
open class FeignController {

    @Autowired
    private var feignClient: FeignClient? = null

    @RequestMapping("/Feign")
    fun say(): String {
        return feignClient!!.say()+"--> Feign"

    }
}
