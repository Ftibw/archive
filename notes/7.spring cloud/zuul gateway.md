##1.获取项目的服务路径

（这应该最后的步骤）例如：

`ServerSocket`通过监听`9000`端口`accept`浏览器请求的`socket`

从`socket`中获取`InputStream`然后去读`Http`请求

请求字符串如下：

```
GET /user/info HTTP/1.1
Host: localhost:9000
User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:59.0) Gecko/20100101 Firefox/59.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2
Accept-Encoding: gzip, deflate
Connection: keep-alive
Upgrade-Insecure-Requests: 1
```

URL规则：

```
1.+ URL 中+号表示空格 %2B 

2.空格 URL中的空格可以用+号或者编码 %20 

3./ 分隔目录和子目录 %2F 

4.? 分隔实际的 URL 和参数 %3F 

5.% 指定特殊字符 %25 

6.# 表示书签 %23 

7.& URL 中指定的参数间的分隔符 %26 

8.= URL 中指定参数的值 %3D
```

根据上述，可以通过前两个空格的索引截取URL中的内容（例如`/user/info`）



##2.通过serviceId生成路径映射

`zuul gateway`通过注册服务器eureka拉取所有服务地址列表`serviceIds`

根据自定义的映射器生成`serviceIds`的映射（例如`user-service`映射为`user`，`feign-service`映射为`feign`）



##3.请求URL的路由

逻辑上：

```
        String url = "http://localhost:9000/user/info"
        String mapping = "user";
        for (String serviceId : serviceIds) {
            if (mapping.equals(serviceId.replace("(?<path>.*)-service", "${path}")) {
                String ret = restTemplate.getForObject(url.replace(project, serviceId), String.class);
                break;
            }
        }
```



`PatternServiceRouteMapper`类的工作：

1.提取所有拉取到的`serviceId`正则匹配后的子串

2.格式化字串：先将多斜杠`/{2,}`替换为单斜杠`/`，然后将首位的单斜杠（如果存在）`/`舍弃

3.验证子格式化后的子串非空，则返回该子串，否则返回`serviceId`

最终返回的结果就是路由的mapping