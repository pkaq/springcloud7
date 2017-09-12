
# Spring Cloud 之服务治理 - Eureka服务发现- 消费者(Feign)
Feign是一种声明式、模板化的HTTP客户端。在Spring Cloud中使用Feign, 我们可以做到使用HTTP请求远程服务时能与调用本地方法一样的编码体验，开发者完全感知不到这是远程方法，更感知不到这是个HTTP请求。
Feign使得 Java HTTP 客户端编写更方便。Feign 灵感来源于Retrofit、JAXRS-2.0和WebSocket。Feign最初是为了降低统一绑定Denominator到HTTP API的复杂度，不区分是否支持Restful。Feign旨在通过最少的资源和代码来实现和HTTP API的连接。通过可定制的解码器和错误处理，可以编写任意的HTTP API。

** 主要特点 **
- 定制化
- 提供多个接口
- 支持JSON格式的编码和解码
- 支持XML格式的编码和解码

由于Feign是基于Ribbon的,所以这里使用了Feign就已经具备了Ribbon的负载均衡功能

1.引入依赖

    ```groovy
   "org.springframework.cloud:spring-cloud-starter-feign:${cloud_config}"
```
2.稍作配置

在`bootstrap.yaml`中加入`Eureka server`相关配置
    ```yaml
spring:
    cloud:
        config:
            uri: http://localhost:8888
                profile: rabbit
                name: appliaction
                username: pkaq
                password: pkaqx
```


Feign在默认情况下使用的是JDK原生的URLConnection发送HTTP请求，没有连接池，但是对每个地址会保持一个长连接，即利用HTTP的persistence connection 。我们可以用Apache的HTTP Client替换Feign原始的http client, 从而获取连接池、超时时间等与性能息息相关的控制能力。Spring Cloud从Brixtion.SR5版本开始支持这种替换，首先在项目中声明Apache HTTP Client和feign-httpclient依赖：\
```groovy
compile "com.netflix.feign:feign-httpclient:${feign_httpclient}"
```

    ```yaml
feign:
    httpclient:
        enabled: true
```
3.添加注解

    ```java
@EnableEurekaClient
```
4.来点代码

启动类代码
    ```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
open class RibbonClientBooter: CommandLineRunner{
    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("  --- --- --- [ Ribbon client started ] --- --- ---  ")
    }
}

fun main(args: Array<String>) {
    org.springframework.boot.SpringApplication.run(org.pkaq.RibbonClientBooter::class.java, *args)
}
```

Feign接口

    ```java
@org.springframework.cloud.netflix.feign.FeignClient("tiger")
internal interface FeignClient {
    // 所请求的服务接口
    @RequestMapping("/say")
    fun say(): String
}
```

Controller代码

    ```java
@RestController
open class FeignController {

    @Autowired
    private var feignClient: FeignClient? = null
    @RequestMapping("/Feign")
    fun say(): String {
        return feignClient!!.say()+"--> Feign"

    }
}
```

5.启动服务,大功告成