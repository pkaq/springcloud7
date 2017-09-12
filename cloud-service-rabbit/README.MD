# Spring Cloud 之服务治理 - Eureka服务发现- 消费者

本部分需要依赖之前的[配置中心](https://github.com/pkaq/springcloud7/tree/master/cloud-config-server)和[服务注册中心](https://github.com/pkaq/springcloud7/tree/master/cloud-eureka-server-simple)、[服务提供者](https://github.com/pkaq/springcloud7/tree/master/cloud-config-client)三个示例代码运行

三种方式
- RestTemplate
- Ribbon
- Feign

下面描述的是通过`RestTemplate`的方式进行服务调用
- RestTemplate
　　RestTemplate是Spring提供的用于访问Rest服务的客户端，RestTemplate提供了多种便捷访问远程Http服务的方法，能够大大提高客户端的编写效率
   1.引入依赖
```groovy
compile "org.springframework.cloud:spring-cloud-starter-eureka:${cloud_config}"
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
   3.添加注解
```java
@EnableEurekaClient
```
   4.来点代码
```kotlin
@SpringBootApplication
@EnableEurekaClient
open class RabbitClientBooter: CommandLineRunner{
   //spring默认已注入RestTemplateBuilder实例
    @Autowired
    val builder: RestTemplateBuilder? = null
    
    // 实例化restTemplate
    @Bean
    open fun restTemplate(): RestTemplate {
        return builder!!.build()
    }

    @Throws(Exception::class)
    override fun run(vararg args: String) {
        println("  --- --- --- [ Rabbit client started ] --- --- ---  ")
    }
}

fun main(args: Array<String>) {
    org.springframework.boot.SpringApplication.run(org.pkaq.RabbitClientBooter::class.java, *args)
}
```

Controller代码
```
@RestController
class RabbitController {

@Autowired
var loadBalancerClient: LoadBalancerClient? = null;

@Autowired
var restTemplate: RestTemplate? = null

@RequestMapping("/jump")
    fun say(): String {
        // 这里配置要调用发的服务提供者的spring.
        val microServiceNode = "tiger"
        val serviceName = "say"
        val serviceInstance  = loadBalancerClient!!.choose(microServiceNode)
        
        val url = "http://"+serviceInstance.host+":"+serviceInstance.port+"/"+serviceName
        
        return restTemplate!!.getForObject(url, String::class.java)+" - > Rabbit"
    }
}
```
可以看到 采用这种方式 比较繁琐,其中拼接url的部分代码会产生大量重复,或许你第一反应会是封装一下,但好在已经有了足够多的现成轮子可以使用
   5.启动服务,大功告成

