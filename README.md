# go-cqhttp-springboot

基于springboot+go-cqhttp实现的机器人

## go-cqhttp

go-cqhttp的具体使用参考[Mrs4s/go-cqhttp](https://github.com/Mrs4s/go-cqhttp)以及[文档](https://docs.go-cqhttp.org/)

### 1.运行

从[官网](https://github.com/Mrs4s/go-cqhttp/releases)下载适合的发行版

运行go-cqhttp.exe生成bat文件, 再运行bat文件, 选择HTTP通信

websocket通信还未实现

### 2.配置

修改config.yml, 找到post关键字, 修改为以下配置(注意格式缩进对齐) [参考](config.yml)

```yaml
post:                               # 反向HTTP POST地址列表
  - url: 'http://127.0.0.1:8080'    # 地址
    secret: ''                      # 密钥
```

### 3.测试

使用postman或apifox测试(测试时请确保cqhttp已经连接上机器人的QQ)

GET请求http://127.0.0.1:5700/send_private_msg?user_id=xxxxx&message=你好~

机器人会向指定的user_id发送私聊信息

参考[API | go-cqhttp 帮助中心](https://docs.go-cqhttp.org/api/#发送私聊消息)

## springboot环境搭建

### 1.maven依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.6</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.entropy</groupId>
    <artifactId>spb</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>spb</name>
    <description>spb</description>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <!--web-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!--lombok-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!--test-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <!--fastjson-->
        <!--fastjson 1.2.80以下版本存在反序列化漏洞, 请使用最新版本或其它json处理工具-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.83</version>
        </dependency>
        <!--httpUtils-->
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpcore</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.httpcomponents</groupId>
            <artifactId>httpclient</artifactId>
        </dependency>
        <!--websocket-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```

### 2.Controller

```java
@RestController
@Slf4j
public class SpbController {
    @Resource
    private SpbService spbService;

    @PostMapping
    public void SpbEvent(HttpServletRequest request) {
        spbService.SpbEventHandler(request);
    }
}
```

### 3.Service

service

```java
public interface SpbService {

    void SpbEventHandler(HttpServletRequest request);
}
```

serviceImpl

```java
@Service
@Slf4j
public class SpbServiceImpl implements SpbService {
    @Override
    public void SpbEventHandler(HttpServletRequest request) {
        JSONObject jsonParam = this.getJSONParam(request);
        log.info("接收参数为:{}", jsonParam.toString());
        if ("message".equals(jsonParam.getString("post_type"))) {
            String userId = jsonParam.getString("user_id");
            String message = jsonParam.getString("message");
            if ("你好".equals(message)) {
                String url = "http://127.0.0.1:5700/send_private_msg?user_id=" + userId + "&message=你好~";
                String result = HttpRequestUtils.doGet(url);
                log.info("发送成功:==>{}", result);
            }
        }
    }

    public JSONObject getJSONParam(HttpServletRequest request) {
        JSONObject jsonParam = null;

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            jsonParam = JSONObject.parseObject(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return jsonParam;
    }
}

```

### 4.Utils

```java
public class HttpRequestUtils {
    /**
     * @Description: 发送Get请求
     */
    public static String doGet(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("content-type", "application/json");
        httpGet.setHeader("DataEncoding", "UTF-8");
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(35000)
                .setConnectionRequestTimeout(35000)
                .setSocketTimeout(60000)
                .build();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse httpResponse = null;

        try {
            httpResponse = httpClient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            return EntityUtils.toString(entity);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * @Description: 发送http post请求
     */
    public static String doPost(String url, String jsonString) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(35000)
                .setConnectionRequestTimeout(35000)
                .setSocketTimeout(60000)
                .build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("content-type", "application/json");
        httpPost.setHeader("DataEncoding", "UTF-8");
        CloseableHttpResponse httpResponse = null;

        try {
            httpPost.setEntity(new StringEntity(jsonString));
            httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            String result = EntityUtils.toString(entity);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
```

### To be continue

