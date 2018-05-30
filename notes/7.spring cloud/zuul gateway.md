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

```java
        String url = "http://localhost:9000/user/info"
        String mapping = "user";
        for (String serviceId : serviceIds) {
            if (mapping.equals(serviceId.replace("(?<path>.*)-service", "${path}")) {
                String ret = restTemplate.getForObject(url.replace(mapping, serviceId), String.class);
                break;
            }
        }
```



`PatternServiceRouteMapper`类的工作：

1.提取所有拉取到的`serviceId`正则匹配后的子串

2.格式化字串：先将多斜杠`/{2,}`替换为单斜杠`/`，然后将首位的单斜杠（如果存在）`/`舍弃

3.验证子格式化后的子串非空，则返回该子串，否则返回`serviceId`

最终返回的结果就是路由的mapping

## 核心

当接受到请求后获取映射路径，再通过`getRemainingPath`按条件过滤后获取剩余的映射路径，此时的映射mapping。（例如`http://localhost:9000/user/info`经过过滤后得到`/user/info`）

```java
	/**
	 * Return the path within the web application for the given request.
	 * <p>Detects include request URL if called within a RequestDispatcher include.
	 * @param request current HTTP request
	 * @return the path within the web application
	 */
	public String getPathWithinApplication(HttpServletRequest request) {
		String contextPath = getContextPath(request);
		String requestUri = getRequestUri(request);
        //通过request.getRequestUri()获取URL("http://localhost:9000/user/info")中的"/user/info"部分
        //通过request.getContextPath()获取结果为空字符串
		String path = getRemainingPath(requestUri, contextPath, true);
		if (path != null) {
			// Normal case: URI contains context path.
			return (StringUtils.hasText(path) ? path : "/");
		}
		else {
			return requestUri;
		}
	}
```

```java
/**
 * Match the given "mapping" to the start of the "requestUri" and if there
 * is a match return the extra part. This method is needed because the
 * context path and the servlet path returned by the HttpServletRequest are
 * stripped of semicolon content unlike the requesUri.
 */
private String getRemainingPath(String requestUri, String mapping, boolean ignoreCase) {
   int index1 = 0;
   int index2 = 0;
    //比较项目内的资源路径/user/info，与项目名，这里项目名为空""
   for (; (index1 < requestUri.length()) && (index2 < mapping.length()); index1++, index2++) {
      char c1 = requestUri.charAt(index1);
      char c2 = mapping.charAt(index2);
      if (c1 == ';') {
         index1 = requestUri.indexOf('/', index1);
         if (index1 == -1) {
            return null;
         }
         c1 = requestUri.charAt(index1);
      }
      if (c1 == c2 || (ignoreCase && (Character.toLowerCase(c1) == Character.toLowerCase(c2)))) {
         continue;
      }
      return null;
   }
   if (index2 != mapping.length()) {
      return null;
   }
   else if (index1 == requestUri.length()) {
      return "";
   }
   else if (requestUri.charAt(index1) == ';') {
      index1 = requestUri.indexOf('/', index1);
   }
   //经过层层判断走到了这里，结果执行了"/user/info".substring(0)...原值返回
   return (index1 != -1 ? requestUri.substring(index1) : "");
}
```

```java
//mapRouteToService(serviceId)方法中实现了我们自定义的正则匹配
//serviceId正则匹配后得到user，然后在前拼接/，在后拼接/**
String key = "/" + mapRouteToService(serviceId) + "/**";
......
//映射后key="/user/**" serviceId=user-service
routesMap.put(key, new ZuulRoute(key, serviceId)); 
```

经典枚举实例

```java
package com.netflix.zuul;

public enum ExecutionStatus {

    SUCCESS (1), SKIPPED(-1), DISABLED(-2), FAILED(-3);
    
    private int status;

    ExecutionStatus(int status) {
        this.status = status;
    }
}
```