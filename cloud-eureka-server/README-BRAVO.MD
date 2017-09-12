# Spring Cloud 之服务治理 - Eureka服务发现-生产者

实现服务发现非常容易，只需要如下几步：
1.添加依赖
```groovy
compile "org.springframework.cloud:spring-cloud-starter-eureka:${cloud_config}"
```
2.添加注解
```java
@EnableEurekaClient
```
3.稍作配置
在应用上下文(一般是application.yaml)中配置`Eureka Server`地址
```
eureka:
  client:
    service-url:
      defaultZone: http://localhost:9871/eureka
```     

测试代码
```kotlin
    @Autowired
    var discoveryClient: DiscoveryClient? = null

    @RequestMapping("/say")
    fun say(): String {
        return "Services: " + discoveryClient!!.services
    }
```
4.启动服务，大功告成
