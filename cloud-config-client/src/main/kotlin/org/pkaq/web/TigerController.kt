package org.pkaq.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TigerController {
    @Value("\${tiger}")
    internal var roar: String? = null

    @RequestMapping("/say")
    fun say(): String {
        return "Tiger say:" + roar!!
    }
}
