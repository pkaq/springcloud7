package org.pkaq.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TigerController {
    @Value("\${tiger}")
    internal var roar: String? = null

    @Value("\${foo}")
    internal var foo: String? = null

    @RequestMapping("/say")
    fun say(): String {
        return "foo "+ foo+" - >Tiger say:" + roar!!
    }
}
