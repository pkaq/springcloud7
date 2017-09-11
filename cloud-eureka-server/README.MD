# Spring Cloud 之服务治理 - 搭建高可用的Eureka服务注册中心

## 概述

   著名的CAP理论指出，一个分布式系统不可能同时满足C(一致性)、A(可用性)和P(分区容错性)。由于分区容错性在是分布式系统中必须要保证的，因此我们只能在A和C之间进行权衡。

　　`Eureka Server`提供服务注册服务，各个节点启动后，会在`Eureka Server`中进行注册，这样`Eureka Server`中的服务注册表中将会存储所有可用服务节点的信息，服务节点的信息可以在界面中直观的看到。

　　`Eureka`看明白了这一点，因此在设计时就优先保证可用性。在此`Zookeeper`保证的是CP, 而`Eureka`则是AP。`Eureka`各个节点都是平等的，几个节点挂掉不会影响正常节点的工作，剩余的节点依然可以提供注册和查询服务。

　　在应用启动后，将会向`Eureka Server`发送心跳,默认周期为30秒，如果`Eureka Server`在多个心跳周期内没有接收到某个节点的心跳，`Eureka Server`将会从服务注册表中把这个服务节点移除(默认90秒)。
　　其次，`Eureka Client`对已经获取到的注册信息也做了30s缓存。即服务通过`eureka`客户端第一次查询到可用服务地址后会将结果缓存，下次再调用时就不会真正向`Eureka`发起HTTP请求了。
　　再次， 负载均衡组件`Ribbon`也有30s缓存。**`Ribbon`会从上面提到的`Eureka Client`获取服务列表，然后将结果缓存`30s`。
　　最后，如果你并不是在`Spring Cloud`环境下使用这些组件(`Eureka`, `Ribbon`)，你的服务启动后并不会马上向`Eureka`注册，而是需要等到第一次发送心跳请求时才会注册。心跳请求的发送间隔也是30s。（Spring Cloud对此做了修改，服务启动后会马上注册）
　　`Eureka Server`之间通过复制的方式完成数据的同步，`Eureka`还提供了客户端缓存机制，即使所有的`Eureka Server`都挂掉，客户端依然可以利用缓存中的信息消费其他服务的API。
　　以上这几个30秒正是官方wiki上写服务注册最长需要2分钟的原因。`Eureka`通过心跳检查、客户端缓存等机制，确保了系统的高可用性、灵活性和可伸缩性。
　　而`Eureka`的客户端在向某个`Eureka`注册或时如果发现连接失败，则会自动切换至其它节点，只要有一台`Eureka`还在，就能保证注册服务可用(保证可用性)，只不过查到的信息可能不是最新的(不保证强一致性)。
除此之外，`Eureka`还有一种自我保护机制，如果在15分钟内超过85%的节点都没有正常的心跳，那么`Eureka`就认为客户端与注册中心出现了网络故障，此时会出现以下几种情况：
1. `Eureka`不再从注册列表中移除因为长时间没收到心跳而应该过期的服务
2. `Eureka`仍然能够接受新服务的注册和查询请求，但是不会被同步到其它节点上(即保证当前节点依然可用)
3. 当网络稳定时，当前实例新的注册信息会被同步到其它节点中

因此， `Eureka`可以很好的应对因网络故障导致部分节点失去联系的情况，而不会像`Zookeeper`那样使整个注册服务瘫痪。

## 创建服务

创建一个`Eureka`服务中心十分简单,只需要引入相关依赖,添加注解,稍作配置即可。
1.引入依赖
```groovy
compile "org.springframework.cloud:spring-cloud-starter-eureka-server:${cloud_eureka}"
```
2.添加注解
```
@EnableEurekaServer
```
3.稍作配置
```yaml
eureka:
    instance:
        #配置主机名
        hostname: eureka.alpha
        appname: reka-alpha
    client:
        #  、在默认设置下，Eureka服务注册中心也会将自己作为客户端来尝试注册它自己，
        #    所以我们需要禁用它的客户端注册行为。
        #    因为当注册中心将自己作为客户端注册的时候，发现在server上的端口被自己占据了，然后就挂了
        register-with-eureka: false
        fetch-registry: false
        service-url:
            defaultZone: http://eureka.alpha:9871/eureka
```

这里访问的是`http://eureka.alpha`,需要修改`hosts`文件将`eureka.alpha`指向`127.0.0.1`,如果不做指向,这里也可以使用`localhost`访问,当然,为了后面的高可用配置建议这里做指向.
4.启动服务,大功告成

    [代码传送门](https://github.com/pkaq/springcloud7/tree/master/cloud-eureka-server-simple)


## 高可用配置

主要是利用多个eureka相互注册,下例用采用三台服务器做集群部署,其中每台`eureka`服务器向另外两台注册自己的实例.
    这里需要注意如下几点

> 1.Eureka Server的同步遵循着一个非常简单的原则：只要有一条边将节点连接，就可以进行信息传播与同步

> 2.如果Eureka A的peer指向了B, B的peer指向了C，那么当服务向A注册时，B中会有该服务的注册信息，但是C中没有。也就是说，如果你希望只要向一台Eureka注册其它所有实例都能得到注册信息，那么就必须把其它所有节点都配置到当前Eureka的peer属性中。这一逻辑是在PeerAwareInstanceRegistryImpl#replicateToPeers()方法中实现的：

[代码传送门](https://github.com/pkaq/springcloud7/tree/master/cloud-eureka-server)

示例代码中拆分了三个配置,可以根据这三个配置分别打成jar包运行即可,当然,这里建议配合`docker-compose`进行编排部署.

1.在Eureka中，一个`instance`通过一个`eureka.instance.instanceId` 来唯一标识，如果这个值没有设置，就采用`eureka.instance.metadataMap.instanceId`来代替。`instance`之间通过`eureka.instance.appName` 来彼此访问，在`spring cloud`中默认值是`spring.application.name`,如果没有设置则为`UNKNOWN`。在实际使用中`spring.application.name`不可或缺,因为相同名字的应用会被Eureka合并成一个群集。`eureka.instance.instanceId`也可以不设置，直接使用缺省值(`client.hostname:application.name:port`)
    ,同一个`appName`下`InstanceId`不能相同。

2.如果 `eureka.client.registerWithEureka`设置成true（默认值true），应用启动时，会利用指定的`eureka.client.serviceUrl.defaultZone`注册到对应的Eureka server中。之后每隔30s（通过`eureka.instance.leaseRenewalIntervalInSeconds`来配置）向`Eureka server`发送一次心跳，如果`Eureka server`在90s（通过`eureka.instance.leaseExpirationDurationInSeconds`配置）内没有收到某个`instance`发来的心跳就会把这个`instance`从注册中心中移走。发送心跳的操作是一个异步任务，如果发送失败，则以2的指数形式延长重试的时间，直到达到`eureka.instance.leaseRenewalIntervalInSeconds * eureka.client.heartbeatExecutorExponentialBackOffBound`这个上限,之后一直以这个上限值作为重试间隔，直至重新连接到`Eureka server`，并且重新尝试连接到`Eureka server`的次数是不受限制的。

3.在`Eureka server`中每一个`instance`都由一个包含大量这个`instance`信息的`com.netflix.appinfo.InstanceInfo`标识，`client`向`Eureka server`发送心跳和更新注册信息是不相同的，InstanceInfo也以固定的频率发送到`Eureka server`，这些信息在`Eureka client`启动后的40s（通过`eureka.client.initialInstanceInfoReplicationIntervalSeconds`配置）首次发送，之后每隔30s(通过`eureka.client.instanceInfoReplicationIntervalSeconds`配置)发送一次。

4.如果`eureka.client.fetchRegistry`设置成true（默认值true），`Eureka client`在启动时会从`Eureka server`获取注册信息并缓存到本地，之后只会增量获取信息（可以把`eureka.client.shouldDisableDelta`设置成false来强制每次都全量获取）。获取注册信息的操作也是一个异步任务，每隔30秒执行一次（通过`eureka.client.registryFetchIntervalSeconds`配置），如果操作失败，也是以2的指数形式延长重试时间，直到达到`eureka.client.registryFetchIntervalSeconds * eureka.client.cacheRefreshExecutorExponentialBackOffBound` 这个上限，之后一直以这个上限值作为重试间隔，直至重新获取到注册信息，并且重新尝试获取注册信息的次数是不受限制的。
这些任务都是在`com.netflix.discovery.DiscoveryClient中启动，spring cloud`用`org.springframework.cloud.netflix.eureka.CloudEurekaClient`对这个类进行了扩展。

## [常见问题]

1.自我保护模式
```cmd
EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.
```

默认情况下，如果`Eureka Server`在一定时间内没有接收到某个微服务实例的心跳，`Eureka Server`将会注销该实例（默认90秒）。但是当网络分区故障发生时，微服务与`Eureka Server`之间无法正常通信，以上行为可能变得非常危险了——因为微服务本身其实是健康的，此时本不应该注销这个微服务。

Eureka通过“自我保护模式”来解决这个问题——当Eureka Server节点在短时间内丢失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。一旦进入该模式，`Eureka Server`就会保护服务注册表中的信息，不再删除服务注册表中的数据（也就是不会注销任何微服务）。当网络故障恢复后，该`Eureka Server`节点会自动退出自我保护模式。

综上，自我保护模式是一种应对网络异常的安全保护措施。它的架构哲学是宁可同时保留所有微服务（健康的微服务和不健康的微服务都会保留），也不盲目注销任何健康的微服务。使用自我保护模式，可以让`Eureka`集群更加的健壮、稳定。

在Spring Cloud中，可以使用`eureka.server.enable-self-preservation = false` 禁用自我保护模式。


2.`unavailable-replicas`
在配置文件中如果不使用域名的方式，而指定localhost或者ip(127.0.0.1/外网ip)，服务能够正常启动，但分片服务总显示在`unavailable-replicas`中，因此在host中指定了相应的域名做服务区分

** 参考 **
- http://www.cnblogs.com/ityouknow/p/6854805.html
- https://eacdy.gitbooks.io/spring-cloud-book/content/
- http://fengyilin.iteye.com/blog/2367265mder