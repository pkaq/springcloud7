---
    title: Spring Cloud 之创建配置中心服务
author: PKAQ
date: 2017-09-07 11:20:07
tags: ['Spring Cloud','微服务']
categories:['微服务']

---




    启动后访问 `http://localhost:8888`

        [TOC]

在开发微服务时,各个服务有一些共同的配置,若是几十个微服务那么便需要将这些配置复制几十份.在开发过程中,对这些配置文件的编辑维护是一件十分麻烦的事情,若是可以对这些公用配置进行统一配置无疑将会极大减轻维护的难度和工作量,借助spring提供的配置中心服务可以对这些公共配置进行统一管理,具体操作如下
<!-- more -->

## 1.创建配置中心仓库
略
## 2.创建配置中心文件
其中`spring.cloud.config.server.git.uri`支持如下几种方式进行配置

### 2.1 版本控制系统(git,svn)
    ```
   spring:
      cloud:
        config:
          name: configcenter
          server:
            git:
              uri: https://git.oschina.net/pkaq/spring-cloud-7simple.git
              # 从该库下的cloud-cconig-repo路径下加载,这里如果需要配置多个可以用`,`分隔开
			  search-paths: cloud-config-repo
   ```
如果采用的`svn`作为版本控制,只需要将`git`节点替换为`svn`即可

#### 认证配置
- ##### [a].http认证
对于需要进行认证的远程配置库,可以使用HTTP的 basic authentication 方法进行认证,只需要添加`username`,`password` 属性即可
    ```yml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/pkaq/springcloud7.git
          username: tiger
          password: scott
```

模式匹配多个库
然而会发现在访问http://localhost:8080/master/special-default.yml显示profile:profile-default,即 https://git.coding.net/eggyer/learn-spring-cloud-config.git默认配置文件的内容
原因是pattern: special*/dev*,special/test*,只匹配dev结尾和test结尾的内容.不建议使用这种方式,建议使用通配符的方式.

- ##### [b].ssh认证
这个比较简单,只要将`uri`中的地址换成对应的ssh地址,并且保证你本地`~/.ssh`目录中存储好认证的`key`即可

伴随着版本控制系统作为后端(git, svn)，文件都会被check out 或clone 到本地文件系统中. 默认这些文件会被放置到以config-repo-为前缀的系统临时目录中.在 linux 上, 譬如应该是 /tmp/config-repo-<randomid>目录. 有些操作系统http://serverfault.com/questions/377348/when-does-tmp-get-cleared/377349#377349[routinely clean out]放到临时目录中,这会导致不可预知的问题出现.为了避免这个问题,通过设置spring.cloud.config.server.git.basedir 或spring.cloud.config.server.svn.basedir参数值为非系统临时目录.

### 2.2 native filesystem
可以通过采用本地文件系统的方式访问配置文件
    ```yml
spring:
  cloud:
    config:
      name: configcenter
      server:
        native:
          search-locations: file:///${path}/config-repo
```
可支持以`[classpath:/, classpath:/config,file:./, file:./config]`等方式加载
>p.s:注意,这里有个坑`win`下`file`后需要多加个`/`即,`file:///${path}/config-repo`

### 2.3 vault
可以通过`spring.cloud.config.server.vault`节点对`vault`方式进行配置

默认情况下,配置服务会在配置文件第一次被请求时`clone`远程的配置库.当然也可以配置为在启动时clone远程的配置库
只需要配置`spring.cloud.config.server.git.clone-on-start`为`true`即可
    ```yml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/pkaq/springcloud7.git
          clone-on-start: true
```



## 3.使用`@EnableConfigServer`注解发布配置服务
1.添加 `spring-cloud-config-server` 相关依赖
    ```
compile "org.springframework.cloud:spring-cloud-config-server:1.5.6.RELEASE"
```
2.在你的`spring boot`启动类上添加`@EnableConfigServer`注解
3.启动服务,大功告成.

>p.s:`application.properties, application.yml, application-*.properties`这类配置文件都会通过服务发布出去.但是,当采用`native filesystem`方式时,`application*`此类文件不会被发布.

## 4.访问配置
可以通过如下规则对配置资源进行访问读取`profile`的优先级,激活的profiles的优先级高于defaults,有多个profiles,最后一个起作用。
```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```
![配置资源规则](.\rs\configserver.png)

>
- `application`:可以通过`spring.config.name`进行配置,默认为`application`;
- `profile`:为配置名称,对应`sprng.profiles.active`配置;
- `label`:可选项 ，git分支名称或者tag名称,默认为`master`;如果git分支或tag的名称包含一个斜杠 ("/"),此时HTTP URL中的label需要使用特殊字符串"(_)"来替代(为了避免与其他URL路径相互混淆)

## 5.健康检查
## 6.安全配置
需要添加`spring-boot-starter-security`依赖包,进行安全认证配置,

## 7.加密解密
Spring Cloud可以在本地进行预处理的解密，与Config Server有相同的规则，通过设置相同的`encrypt.*`来实现。
因此，可以使用加密的值在`{cipher}*`上，在应用通过`Environment`获得属值之前进行解密。使用加密特性需要包含Spring Security RSA的包到classpath。
Maven依赖”org.springframework.security:spring-security-rsa”。并且，需要在JVM添加JCE扩展。 
如果使用Sun的JDK遇到`Illegal key size`异常，需要安装JCE 更多信息请查看：


