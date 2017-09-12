package org.pkaq.web

import org.springframework.web.bind.annotation.RequestMapping

@org.springframework.cloud.netflix.feign.FeignClient("tiger")
internal interface FeignClient {
    @RequestMapping("/say")
    fun say(): String
}
