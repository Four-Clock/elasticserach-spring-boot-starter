### 轻量化接入
  1.引入jar
  ```xml
   <dependency>
     <groupId>com.zondy</groupId>
     <artifactId>elasticserach-spring-boot-starter</artifactId>
     <version>7.8.1</version>
   </dependency>
  ```
  2.添加配置(yaml配置)
  ```yaml
    liqin:
      elasticsearch:
        username: 
        password: 
        uris:
          - http://127.0.0.1:9200
          - http://127.0.0.1:9300
  ```
  
### 功能丰富
  1.支持绝大部分场景下的查询。
  2.高亮，聚合，热力，地理相关查询等。
  3.可根据ES版本切换API,只需在pom文件中修改对应版本即可

### Q&A
  欢迎使用，有问题随时沟通！！！