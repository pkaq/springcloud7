1.引入依赖

    ```groovy
  compile "org.springframework.cloud:spring-cloud-starter-ribbon:${cloud_config}"
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

启动类代码
    ```java
@SpringBootApplication
@EnableEurekaClient
open class RibbonClientBooter: CommandLineRunner{
    @Autowired
    val builder: RestTemplateBuilder? = null

    @Bean
    // 这个是新加的注解,表示开启robbin的负载均衡功能。
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
```

Controller代码
    ```java
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
```
可以看到这里已经不用通过注入`LoadBalancerClient`获取`serviceInstance`的方式来拼接请求路径了,这是因为,当一个被@LoadBalanced注解修饰的RestTemplate对象向外发起HTTP请求时，会被LoadBalancerInterceptor类的intercept函数所拦截。由于我们在使用RestTemplate时候采用了服务名作为host，所以直接从HttpRequest的URI对象中通过getHost()就可以拿到服务名，然后调用execute函数去根据服务名来选择实例并发起实际的请求。

5.启动服务,大功告成