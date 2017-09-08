# Spring Cloud 之创建配置中心服务-服务端

在开发微服务时,各个服务有一些共同的配置,若是几十个微服务那么便需要将这些配置复制几十份.在开发过程中,对这些配置文件的编辑维护是一件十分麻烦的事情,若是可以对这些公用配置进行统一配置无疑将会极大减轻维护的难度和工作量,借助spring提供的配置中心服务可以对这些公共配置进行统一管理,具体操作如下

## 1 创建配置中心仓库
略
## 2 创建配置中心文件
其中`spring.cloud.config.server.git.uri`支持如下几种方式进行配置

### 2.1 版本控制系统(git,svn)
```yml
   spring:
      cloud:
        config:          
          server:
            git:
              uri: https://git.oschina.net/pkaq/spring-cloud-7simple.git
              # 从该库下的cloud-cconig-repo路径下加载,这里如果需要配置多个可以用`,`分隔开
			  search-paths: cloud-config-repo
   ```
如果采用的`svn`作为版本控制,只需要将`git`节点替换为`svn`即可


伴随着版本控制系统作为后端(`git` `svn`，文件都会被`check out`或`clone`到本地文件系统中. 默认这些文件会被放置到以`config-repo-`为前缀的系统临时目录中.在 `linux` 譬如应该是 `/tmp/config-repo-<randomid>`录.这会导致不可预知的问题出现.为了避免这个问题,通过设置`spring.cloud.config.server.git.basedir`参数值为非系统临时目录.
   
#### 2.1.1 认证配置
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

- ##### [b].ssh认证
这个比较简单,只要将`uri`中的地址换成对应的ssh地址,并且保证你本地`~/.ssh`目录中存储好认证的`key`即可

### 2.1.2 模式匹配多个仓库
```yml
spring:
  cloud:
    config:
      server:
        git:
        # 远程仓库地址
          uri: https://github.com/pkaq/springcloud7.git
        # 子目录
          search-paths: cloud-config-repo
        # 在启动时就下载配置文件
          clone-on-start: true
        # 读取配置超时时间 默认5s
          timeout: 15
        # 强制读取远程版本(当本地版本被篡改时仍然保证使用远程副本)
          force-pull: true
        # 禁止调用端覆盖此属性
          overrides:
            foo: bar
#          多仓库配置
          repos:
#            别名,随意
            repo-a:
#               匹配符
               pattern:
                  - 'bravo*'
#               仓库地址
               uri: http://git/config-repo.git
            repo-b:
               pattern:
                  - 'alpha*'
               uri: file://D:/git/spring-cloud-7simple
```
客户端可以通过`spring.application.name`或`spring.cloud.config.name`来多个库中的`application`部分描述,通过`spring.cloud.config.profile`配置`profile`部分的描述.如果加载不到响应的配置文件则从`git.uri`下进行加载.
> 注意:添加多个模式库配置后,直接通过浏览器访问配置中心服务会将默认配置也加载出来

### 2.1.3 clone-on-start
默认情况下,配置服务会在配置文件第一次被请求时`clone`远程的配置库.当然也可以配置为在启动时clone远程的配置库
只需要配置`spring.cloud.config.server.git.clone-on-start`为`true`即可

### 2.2 native filesystem
可以通过采用本地文件系统的方式访问配置文件
```yml
spring:
  cloud:
    config:      
      server:
        native:
          search-locations: file:///${path}/config-repo
```
可支持以`[classpath:/, classpath:/config,file:./, file:./config]`等方式加载
>p.s:注意,这里有个坑`win`下`file`后需要多加个`/`即,`file:///${path}/config-repo`

### 2.3 vault
可以通过`spring.cloud.config.server.vault`节点对`vault`方式进行配置

## 3 使用`@EnableConfigServer`注解发布配置服务
1.添加 `spring-cloud-config-server` 相关依赖
```groovy
compile "org.springframework.cloud:spring-cloud-config-server:1.5.6.RELEASE"
```
2.在你的`spring boot`启动类上添加`@EnableConfigServer`注解
3.启动服务,大功告成.

>p.s:`application.properties, application.yml, application-*.properties`这类配置文件都会通过服务发布出去.但是,当采用`native filesystem`方式时,`application*`此类文件不会被发布.

## 4 访问配置
可以通过如下规则对配置资源进行访问读取`profile`的优先级,激活的profiles的优先级高于defaults,有多个profiles,最后一个起作用。
```yml
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```
![配置资源规则](.\sc-configserver\configserver.png)

>
- `application`:`client`可以通过`spring.config.name`进行配置,默认为`application`;
- `profile`:为配置名称,对应`sprng.profiles.active`配置;
- `label`:可选项 ，git分支名称或者tag名称,默认为`master`;如果git分支或tag的名称包含一个斜杠 ("/"),此时HTTP URL中的label需要使用特殊字符串"(_)"来替代(为了避免与其他URL路径相互混淆)

## 5.健康检查

可以通过设置`spring.cloud.config.server.health.enabled=false` 来禁用运行状况指示器。
## 6.安全配置

为了保证配置中心配置项的安全性,通常需要对访问进行限制,可以借助spring-ssecurity,对配置中心服务添加身份认证.
1.需要添加`spring-boot-starter-security`依赖包
2.进行安全认证配置
```yml
# 访问认证
security:
  basic:
    enabled: true
  user:
    name: pkaq
    password: pkaqx
```

**参考**
- http://cloud.spring.io/spring-cloud-static/Dalston.SR3/