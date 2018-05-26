`only for the version of springcloud at 1.5.13`

通过配置中心动态更新所有服务配置的流程：

##1、eureka server注册中心

依赖`web`、`eureka server`

用于管理注册的服务（对于注册服务器`eureka server`来说，其他任何服务器都是`client`）

运行环境的配置`application.yml`，如下：

```
server:
  port: 8700  				   //自定义端口号
spring:
  application:
    name: eureka-server  		//自定义应用名
eureka:
  client:
    fetch-registry: false		//当前注册服务器禁止拉取其他注册服务器的服务列表
    register-with-eureka: false	 //当前服务器禁止把自己发布到service-url.defaultZone所指定的注册中心
    service-url:
      defaultZone: http://localhost:8700/eureka/	//目标注册服务器的地址
```

程序入口的类上需要配置如下注解，开启注册服务器功能

```
@EnableEurekaServer
```



##2、bus service控制总线

创建`config server`服务器，

依赖`config server`、`web`、`eureka discovery`、`bus`、`rabbitmq`

运行环境的配置`application.yml`，如下：

```
server:
  port: 9000	#自定义端口号
spring:
  application:
    name: config-server		#应用名(应用的唯一标识)
  cloud:
    config:
      server:		#配置服务器
        git:		#git（gitLab仓库）
          uri: http://47.104.232.74/Ftibw/app-config.git # 项目的uri
          username: Ftibw #  git的用户名称
          password: 123456 #  git的用户密码
          search-paths: config/ # 在哪个文件夹里面
  rabbitmq:		#rabbitmq的参数
    host: 192.168.3.117
    username: user
    password: 123456
eureka:			#注册中心地址
  client:
    service-url:
      defaultZone: http://localhost:8700/eureka/
management:
  security:		#关闭端点的访问权限
    enabled: false		
```

程序入口的类上需要配置如下注解，开启配置服务器功能、开启服务发布/拉取功能

```
@EnableConfigServer
@EnableEurekaClient
```



##3、user service `API`实现

依赖`start config`、`web`、`eureka discovery`、`bus`、`rabbitmq`、`actuator（端点监控的监视器、执行机构）`，以及`API`的定义

运行环境的配置`bootstrap.yml`，如下：

```
spring:
  application:
    name: user-service		  	    #应用名(应用的唯一标识)
  cloud:
    config:						   #启动后从配置服务器拉取配置信息
      uri: http://localhost:9000/	 #配置服务器的访问地址
      profile: test					#配置文件描述（完整配置文件名由>>>应用名+profile+.yml<<<生成）
        # user-service-dev
  rabbitmq:
    host: 192.168.3.117		#rabbitmq所在主机地址（spring内置了默认端口值为5672，不需要显示配置）
    username: user					
    password: 123456
management:   # 关闭端点的访问权限
  security:
    enabled: false
```

程序入口的类上需要配置如下注解，开启服务发布/拉取功能

```
@EnableEurekaClient
```

`controller`层的类上需要配置如下注解，开启配置刷新功能

```
@RefreshScope
```



## 4、`API`定义

创建`user service api`

依赖`web`、`feign`

创建`UserServiceAPI`接口，`陈列接口的详细服务`

在接口上设置`API`实现的应用名（应用名绑定了应用服务器的主机地址），以及服务熔断器

```
@FeignClient(value = "user-service", fallback = UserFallback.class)
```