
# Spring Cloud 之创建配置中心服务-客户端

各个微服务想要使用配置中心服务非常简单,仅需要在`bootstrap.yaml`(或`.properties`)文件中稍作配置即可

## 1.添加依赖
    ```groovy    
`org.springframework.cloud:spring-cloud-starter-config:${cloud_config}`
```
## 2.配置上下文
    `bootstrap.yaml`中添加
    ```yml
spring:
    application:
        name: config-client
    cloud:
        config:
            # 配置中心地址
            uri: http://localhost:8888
            # 要应用的配置文件
            profile: dev
            # 要读取的配置文件名
            name: bravo
            # 对应服务端security设置的用户名密码
            username: pkaq
            password: pkaqx
```
> 注意这里是`bootstrap.yml`而不是`appliction.yml`,因为`bootstrap.yml`会在应用启动之前读取, 而`spring.cloud.config.uri`会影响应用启动,关于上下文可参见如下内容.

### 2.1 启动上下文
Spring Cloud会创建一个`Bootstrap Context`，作为Spring应用的`Application Context`的父上下文。初始化的时候，`Bootstrap Context`负责从外部源加载配置属性并解析配置。这两个上下文共享一个从外部获取的`Environment`。`Bootstrap`属性有高优先级，默认情况下，它们不会被本地配置覆盖。 `Bootstrap context`和`Application Context`有着不同的约定，所以新增了一个`bootstrap.yml`文件，而不是使用`application.yml` (或者`application.properties`)。保证`Bootstrap Context`和`Application Context`配置的分离。

　　推荐在`bootstrap.yml` or `application.yml`里面配置`spring.application.name`. 你可以通过设置`spring.cloud.bootstrap.enabled=false`来禁用`bootstrap`。

### 2.2 应用上下文层次结构

如果你通过`SpringApplication`或者`SpringApplicationBuilder`创建一个`Application Context`,那么会为spring应用的`Application Context`创建父上下文`Bootstrap Context`。在Spring里有个特性，子上下文会继承父类的`property sources` and `profiles` ，所以`main application context` 相对于没有使用Spring Cloud Config，会新增额外的`property sources`。额外的`property sources`有：

- “bootstrap” : 如果在`Bootstrap Context`扫描到`PropertySourceLocator`并且有属性，则会添加到`CompositePropertySource`。`Spirng Cloud Config`就是通过这种方式来添加的属性的，详细看源码`ConfigServicePropertySourceLocator`。下面也也有一个例子自定义的例子。
- “applicationConfig: [classpath:bootstrap.yml]” ，（如果有`spring.profiles.active=production`则例如 `applicationConfig: [classpath:/bootstrap.yml]#production）`: 如果你使用`bootstrap.yml`来配置`Bootstrap Context`，他比`application.yml`优先级要低。它将添加到子上下文，作为Spring Boot应用程序的一部分。下文有介绍。
　　由于优先级规则，`Bootstrap Context`不包含从`bootstrap.yml`来的数据，但是可以用它作为默认设置。

>`bootstrap.yml`是由`spring.cloud.bootstrap.name`（默认:”bootstrap”）或者`spring.cloud.bootstrap.location`（默认空）

**覆盖远程属性**

`property sources`被`bootstrap context` 添加到应用通常通过远程的方式，比如”Config Server”。默认情况下，本地的配置文件不能覆盖远程配置，但是可以通过启动命令行参数来覆盖远程配置。如果需要本地文件覆盖远程文件，需要在远程配置文件里设置授权
    `spring.cloud.config.allowOverride=true`（这个配置不能在本地被设置）。一旦设置了这个权限，你可以配置更加细粒度的配置来配置覆盖的方式，

比如： 
```yml
spring:
    cloud:
        config:
            # 覆盖任何本地属性 
            overrideNone: true 
            # 仅仅系统属性和环境变量 
            overrideSystemProperties: false
```
源文件见PropertySourceBootstrapPropertie


## .启动服务,大功告成.

    端口号采用配置中心相应配置文件的端口
启动后访问 http://localhost:port/say

**参考**
- http://cloud.spring.io/spring-cloud-static/Dalston.SR3/