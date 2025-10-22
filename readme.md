## 项目结构

项目包结构

111

```txt
src
├── common -- 用于存放通用代码
|   ├── api -- 通用结果集封装类
|   ├── config -- 通用配置类
|   ├── domain -- 通用封装对象
|   ├── exception -- 全局异常处理相关类
|   └── service -- 通用业务类
├── config -- SpringBoot中的Java配置
├── domain -- 共用封装对象
├── generator -- MyBatis-Plus代码生成器
├── modules -- 存放业务代码的基础包
|   └── ums -- 权限管理模块业务代码
|       ├── controller -- 该模块相关接口
|       ├── dto -- 该模块数据传输封装对象
|       ├── mapper -- 该模块相关Mapper接口
|       ├── model -- 该模块相关实体类
|       └── service -- 该模块相关业务处理类
└── security -- SpringSecurity认证授权相关代码
    ├── annotation -- 相关注解
    ├── aspect -- 相关切面
    ├── component -- 认证授权相关组件
    ├── config -- 相关配置
    └── util -- 相关工具类
```

资源文件结构

```txt
resources
├── mapper -- MyBatis中mapper.xml存放位置
├── application.yml -- SpringBoot通用配置文件
├── application-dev.yml -- SpringBoot开发环境配置文件
├── application-prod.yml -- SpringBoot生产环境配置文件
└── generator.properties -- MyBatis-Plus代码生成器配置
```



## 数据结构

数据表：与实体类结构相同

<img src="img/mall_tiny.png" alt="mall_tiny.png" style="zoom:90%; float:left;" />



## mybatis代码生成器（MyBatisPlusGenerator.java）

设计mybatis-plus依赖的应用和配置（类似于mybatis-plus插件生成的效果）

略



## Security的用户详情实体类（AdminUserDetails.java）

继承SpringSecurity的UserDetails接口

```txt
interface UserDetails
func：getAuthorities()、getUsername()、getPassword()、isAccountNonExpired()、isAccountNonLocked()、isCredentialsNonExpired()、isEnabled()

AdminUserDetails
prop: UmsAdmin umsAdmin; List<UmsResource> resourceList
func: getAuthorities()
	  输出结果：[1:商品品牌管理, 2:商品属性分类管理, 3:商品属性管理, ...]
	  others: 略
```



## dto(Data Transfer Object)数据封装类

```UmsAdminLoginParam.java```

```txt
用于接收前端登录的账户密码
String username; String password
```

```UmsAdminParam.java```

```txt
用于接受前端用户注册时的信息
String username; String password; String icon; String email; String nickName; String note
```

```UmsMenuNode.java```

```txt
用于构建Menu树的结点
extends UmsMenu
List<UmsMenuNode> children
```

```UpdateAdminPasswordParam.java```

```txt
用于接收前端用户修改密码的参数
String username; String oldPassword; String newPassword
```



## 构建Menu树

```txt
UmsMenuNode: id、parentId、createTime、title、level、sort、name、icon、hidden、children
```

```java
public List<UmsMenuNode> treeList() {
    List<UmsMenu> menuList = list();
    List<UmsMenuNode> result = menuList.stream()
        .filter(menu -> menu.getParentId().equals(0L))
//                过滤满足条件的menu，否则将被拦截
//                即保留属性parentId等于0的menu，其余丢弃
        .map(menu -> convertMenuNode(menu, menuList)).collect(Collectors.toList());
    return result;
}

private UmsMenuNode convertMenuNode(UmsMenu menu, List<UmsMenu> menuList) {
    UmsMenuNode node = new UmsMenuNode();
    BeanUtils.copyProperties(menu, node);
    List<UmsMenuNode> children = menuList.stream()
        .filter(subMenu -> subMenu.getParentId().equals(menu.getId()))
        .map(subMenu -> convertMenuNode(subMenu, menuList)).collect(Collectors.toList());
    node.setChildren(children);
    return node;
}
```



## mapper层

mybatis-plus：xxxMapper extends BaseMapper<xxx>

可使用BaseMapper 的insert、delete、update、select等相关方法

自定义方法：

```txt
UmsAdminMapper
// 根据某个资源的Id获取该资源相关的用户Id列表
List<UmsAdmin> getAdminIdList(@Param("resourceId") Long resourceId);  

UmsMenuMapper
// 根据指定用户的Id获取其具有权限的目录列表
List<UmsMenu> getMenuList(@Param("adminId") Long adminId);
// 根据某类角色的Id获取其对应的目录列表
List<UmsMenu> getMenuListByRoleId(@Param("roleId") Long roleId);

UmsResourceMapper
// 根据某个用户的Id获取其具有权限的资源列表
List<UmsResource> getResourceList(@Param("adminId") Long adminId);
// 根据某类角色的Id获取其对应的资源列表
List<UmsResource> getResourceListByRoleId(@Param("roleId") Long roleId);

UmsRoleMapper
// 根据某个用户的Id获取其对应的角色列表
List<UmsRole> getRoleList(@Param("adminId") Long adminId);
```



## HTTP API响应封装

enum枚举类：`ResultCode.java`

```txt
SUCCESS(200, "操作成功"),
FAILED(500, "操作失败"),
VALIDATE_FAILED(404, "参数检验失败"),
UNAUTHORIZED(401, "暂未登录或token已经过期"),
FORBIDDEN(403, "没有相关权限");
```

泛型类：`CommonResult<T>`

```java
public class CommonResult<T> {
    private long code; // 返回码
    private String message;
    private T data;

    protected CommonResult(){}

    protected CommonResult(long code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    // 封装响应方法。。。。。。
}
```



## Spring Bean的注册和配置

- mapper文件夹下dao层文件的配置：

  1、@Mapper      2、@Repository     3、@MapperScan("xxx.xxxxx.xxxx.mapper")    4、@Component

  5、自定义注解

  ```MybatisConfig.java```中使用注解```@MapperScan({"com.mall_tiny.modules.*.mapper"})```

  `*`匹配modules目录下的任意一级子包，`.mapper`匹配任意以`mapper`结尾的包

- @Component 和 @Bean：前者对类使用，后者对类中的方法使用

  ```java
  @Bean
  public RedisService redisService() {
      return new RedisServiceImpl();
  }
  ```

- 注册和管理Bean（不直接使用@Bean或@Component注解）

  ```txt
  @Configuration
  public class xxxConfig {
  	@Bean
  	public RedisService redisService() {
          return new RedisServiceImpl();
      }
  }
  // 在上述带有Configuration注解的文件中，定义并实现Bean
  
  // 在需要使用时自动注入
  @Autowired
  private RedisService redisService;
  ```

**项目中Bean的注册管理详情**：

```txt
BaseRedisConfig.java
RedisTemplate --> Redis读写操作模板
RedisService --> Redis服务，使用RedisTemplate完成具体的读写业务

BaseSwaggerConfig.java
Docket createRestApi --> ？？？

GlobalCorsConfig.java
CorsFilter --> 配置允许跨域调用的过滤器

MallSecurityConfig.java
UserDetailsService --> 根据用户名获取符合 UserDetails类型的用户详情
DynamicSecurityService --> 获取当前项目的资源列表（获取所有resource作为configAttributeMap）

MybatisConfig.java
MybatisPlusInterceptor --> mybatis plus分页插件配置

SwaggerConfig.java
BeanPostProcessor springfoxHandlerProviderBeanPostProcessor --> ？？？

CommonSecurityConfig.java
PasswordEncoder --> 选择具体的加密算法
JwtTokenUtil --> jwt工具类
IgnoreUrlsConfig --> url白名单配置，此处设置具体的白名单
RestAuthenticationEntryPoint --> 自定义未通过认证处理类
RestfulAccessDeniedHandler --> 自定义（认证通过但）用户权限不足处理类
JwtAuthenticationTokenFilter --> jwt认证过滤器
DynamicSecurityMetadataSource --> 
DynamicAccessDecisionManager --> 
DynamicSecurityFilter --> 动态权限过滤器

SecurityConfig.java
SecurityFilterChain --> Spring Security 权限过滤器链
```

 

## JwtTokenUtil.java（JWT工具类）

### JWT的生成

```txt
JWT token的格式：header.payload.signature
header的格式（算法、token的类型）：{"alg": "HS512","typ": "JWT"}
payload的格式（用户名、创建时间、生成时间）：{"sub":"wang","created":1489079981393,"exp":1489684781}
signature的生成算法：HMACSHA512(base64UrlEncode(header) + "." +base64UrlEncode(payload),secret)

jwt = base64(header) + '.' + base64(payload)
jwt = jwt + '.' + Signature_Algorithm(jwt, secret)
```

```txt
实例：（2024-11-29 14:01:37.871）
header：{"alg":"HS512"}  --base64-->  eyJhbGciOiJIUzUxMiJ9
payload：{"sub":"test","created":1732860097871,"exp":1733464897} --base64--> eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTczMjg2MDA5Nzg3MSwiZXhwIjoxNzMzNDY0ODk3fQ

// jjwt包中的Jwts类生成的JWT
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTczMjg2MDA5Nzg3MSwiZXhwIjoxNzMzNDY0ODk3fQ.qnRyVZ48z_L5VFK8Xjg2wNUf29kY7bs_2Nj0Efd2UBicUxNDTx97or8VsOOAGYqoQu5J__attn5swiMPRW98Ag
// 按照官方定义生成的JWT（与官网jwt.io中手动生成的结果相同）
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTczMjg2MDA5Nzg3MSwiZXhwIjoxNzMzNDY0ODk3fQ.qbreR3KtbtwI_vD3ZceZQ1-1SH8wWkhkxRPvf5g-LINsI8Ycbknx3m_KGKxeJN2E4vFTHaufOLArI9l5SejsaA

// 总结，jjwt依赖中的Jwts类生成的JWT，是按照JWT规范生成的，但在生成signature之前对secret进行了某种转换处理，使得生成的JWT与官网生成的JWT在signature上有不同

（Sun Oct 26 2025 20:14:20 GMT+0800 (中国标准时间)）
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0IiwiY3JlYXRlZCI6MTc2MDg3NjA2MDMxNiwiZXhwIjoxNzYxNDgwODYwfQ.Usz2cJBbq8Iac-YfuYDRDMcdGiLML9DhVuLzajSLqWZP11ZkY0EO1xFVT-o_72pEyiosmHFnuMa1PMtmn2vOQQ
```

### JWT验证

```txt
getClaimsFromToken(String token)  // 从token中获取JWT的负载
getUserNameFromToken(String token)  // 从token中获取登录用户
validateToken(String token, UserDetails userDetails) // 验证token是否正确
isTokenExpired(String token) // 验证token是否失效
getExpiredDateFromToken(String token) // 从token中获取过期时间
refreshHeadToken(String token) // 刷新token
tokenRefreshJustBefore(String token, int time) // 判断token是否在指定时间内刷新过 
```



## Redis缓存

### redis操作

`redis配置`：

```xml
spring:
    redis:
        host: localhost # Redis服务器地址
        database: 0 # Redis数据库索引（默认为0）
        port: 6379 #Redis连接端口
        password: # Redis服务器连接地址（默认为空）
        timeout: 3000ms # 连接超时时间（毫秒）
```

`redisTemplate`：`RedisConfig.java`，`BaseRedisConfig.java`

`redis数据类型`：redis的键值对可以存储多种数据结构，字符串String、列表List、集合Set、哈希Hash。`RedisService.java`中定义了对这四类数据类型的部分操作。

```txt
Object：set()、get()、del()、expire()、getExpire()、hashKey()、incr()、decr()
Hash：hGet()、hSet()、hGetAll()、hSetAll()、hDel()、hHashKey()、hIncr()、hDecr()
Set：sMembers()、sAdd()、sIsMember()、sSize()、sRemove()
List：lrange()、lSize()、lIndex()、lPush()、lPushAll()、lRemove()
```

`redis操作`：使用redisTemplate对redis进行操作

```txt
redisTemplate.opsForValue ----> Object类型
redisTemplate.opsForHash  ----> Hash结构
redisTemplate.opsForSet   ----> Set结构
redisTemplate.opsForList  ----> List结构
```

### redis缓存切面

对xxx包下以CacheService结尾的类的公共方法进行切入，在方法执行抛出异常后进行处理。

```java
// CacheException.java
/**
 * 自定义注解，有该注解的缓存方法会抛出异常
 * 用于标记那些在发生缓存操作失败时需要抛出异常的方法。
 * 当这些方法执行过程中出现错误时，它们不会静默地记录错误，而是会直接将异常传播出去
 */

@Documented  // 表明该注解应该被 javadoc 工具记录
@Target(ElementType.METHOD) // 指定了此注解可以应用于方法级别
@Retention(RetentionPolicy.RUNTIME) // 表示这个注解将在运行时可用，这意味着它可以通过反射机制在程序运行期间被读取
public @interface CacheException {
}

// RedisCacheAspect.java
/**
 * Redis缓存切面，防止Redistribution宕机影响正常业务逻辑
 * 即在使用该注解的方法中，出现异常时可选择性抛出，或仅在日志中记录不抛出
 */
@Aspect
@Component
// @Aspect 和 @Component 标记了这是一个Spring AOP切面类，并且是一个Spring管理的组件，自动参与Spring容器的生命周期管理。
@Order(2) // 置了切面的优先级，数字越小表示优先级越高
public class RedisCacheAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    // TODO: security??? 待修改表达式中的security
    @Pointcut("execution(public * com.mall_tiny.security.*CacheService.*(..))")
    // 定义了一个切入点表达式，它指定了哪些方法是这个切面的目标。
    // 这里的表达式匹配的是所有在 com.mall_tiny.security 包下以 CacheService 结尾的类中的公共方法
    public void cacheAspect() {
    }

    // @Around：一个环绕通知（around advice），它会在切入点所匹配的方法执行前后运行
    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        /**
         * 它接收一个 ProceedingJoinPoint 参数，代表了连接点（即目标方法调用），允许我们控制目标方法的执行。
         * 获取当前执行的方法签名并转换为 MethodSignature，进而获取到具体的方法对象。
         * 尝试执行目标方法 (joinPoint.proceed())
         * 如果执行过程中抛出了异常：
         *   如果目标方法上有 CacheException 注解，则重新抛出异常。
         *   否则，只是记录错误日志而不抛出异常，这通常意味着系统将继续运行而不会中断业务流程。
         * 最终返回目标方法的结果。
         */
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            // 有CacheException注解的方法需要抛出异常
            if (method.isAnnotationPresent(CacheException.class)) {
                throw throwable;
            } else {
                LOGGER.error(throwable.getMessage());
            }
        }
        return result;
    }
}
```



## Mybatis Plus分页配置（拦截器）

```Interceptor(拦截器)和Filter(过滤器)```

Filter属于Servlet层面，作用于整个web应用；Interceptor位于SpringMVC架构层面，仅作用于Controller方法

```txt
客户端请求 -> [过滤器] -> [Spring MVC前端控制器(DispatcherServlet)] -> [拦截器 preHandle] -> [控制器方法] -> [拦截器 postHandle] -> [视图渲染] -> [拦截器 afterCompletion] -> [过滤器] -> 客户端响应
```

**使用Mybatis Plus 的分页插件（适配mybatis plus）**

1、 配置mybatis plus 的分页插件

```java
@Configuration
@EnableTransactionManagement
@MapperScan({"com.mall_tiny.modules.*.mapper"})
public class MybatisConfig {

    // MybatisPlus 分页插件的配置
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return mybatisPlusInterceptor;
    }
}
```

2、 使用mybatis plus 的Page类进行分页查询（service层，无需操作SQL语句）

​		拓展：需要自定义SQL语句且分页，该如何实现？？？

```java
public Page<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
    // 1：定义一个Page类，指定泛型
    Page<UmsAdmin> page = new Page<>(pageNum, pageSize);
    QueryWrapper<UmsAdmin> wrapper = new QueryWrapper<>();
    LambdaQueryWrapper<UmsAdmin> lambda = wrapper.lambda();
    if (StrUtil.isNotEmpty(keyword)) {
        lambda.like(UmsAdmin::getUsername, keyword);
        lambda.or().like(UmsAdmin::getNickName, keyword);
    }
    // 2：将查询结果进行分页处理，返回类型为Page<T>
    return page(page, wrapper);
}
```

3、 将返回类型Page<T> 转为统一的API封装类型（可选）

```java
// controller 层调用流程
Page<UmsAdmin> adminList = umsAdminService.list(keyword, pageSize, pageNum);
return CommonResult.success(CommonPage.restPage(adminList));

// 将Page<T> 类型转为 CommonPage<T>
public class CommonPage<T> {
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPage;
    private Long total;
    private List<T> list;
    
    public static <T> CommonPage<T> restPage(Page<T> pageResult) {
        CommonPage<T> result = new CommonPage<>();
        result.setPageNum(Convert.toInt(pageResult.getCurrent()));
        result.setPageSize(Convert.toInt(pageResult.getSize()));
        result.setTotal(pageResult.getTotal());
        result.setTotalPage(Convert.toInt(pageResult.getTotal()/pageResult.getSize() + 1));
        result.setList(pageResult.getRecords());
        return result;
    }
    // ......   
}
```



**使用Mybatis的分页插件（如PageHelper，需要在SQL中指定页号、行数）**

略

## Swagger文档

文件：`SwaggerProperties.java`、`BaseSwaggerConfig.java`、`SwaggerConfig.java`

- **Spring Boot + Swagger 2（Springfox）** 的配置结构，采用**抽象基类 + 具体实现**的方式，实现了 Swagger 文档的灵活配置

- `SwaggerProperties.java` —— 配置属性封装类

  ```txt
  apiBasePackage
  enableSecurity
  title、description、version、contactName、contactUrl、contactEmail
  ```

- `BaseSwaggerConfig.java` —— Swagger 配置抽象基类

  ```java
  public abstract class BaseSwaggerConfig {
      @Bean
      // 完整的 Swagger 配置流程（创建 Docket、设置 ApiInfo、安全配置等）
      public Docket createRestApi() { ... }
  	
      // 根据 SwaggerProperties 构建 API 文档基本信息
      private ApiInfo apiInfo(...) { ... }
  
      // 安全认证支持（当 enableSecurity=true 时），定义认证方式和认证路径
      private List<SecurityScheme> securitySchemes() { ... }
      private List<SecurityContext> securityContexts() { ... }
  
      // 将 可变部分（即具体的配置值）抽象为 swaggerProperties() 方法，由子类实现
      // 任何继承 BaseSwaggerConfig 的子类，只需提供 SwaggerProperties，即可获得完整的 Swagger 配置。实现解耦和复用
      public abstract SwaggerProperties swaggerProperties();
  }
  ```

- `SwaggerConfig.java` —— 具体 Swagger 配置实现类

  ```java
  @Configuration // 标识为配置类，程序运行时对swagger的配置由此开始
  @EnableSwagger2
  public class SwaggerConfig extends BaseSwaggerConfig {
      @Override
      public SwaggerProperties swaggerProperties() {
          return SwaggerProperties.builder()
  				.......
                  .build();
      }
  
      @Bean
      // 解决 Spring Boot 2.6+ 兼容性问题（关键！）
      // 从 Spring Boot 2.6 开始，Spring MVC 默认使用 PathPatternParser（而非 AntPathMatcher）。
  	// 而 Springfox Swagger 2 不兼容 PathPatternParser，会导致启动报错或 API 无法显示。
  	// springfoxHandlerProviderBeanPostProcessor() 的作用：
  	// 通过反射获取 handlerMappings。
  	// 过滤掉使用 PatternParser 的映射（mapping.getPatternParser() == null）。
  	// 保留传统的 Ant 风格路径匹配，使 Swagger 能正常工作。
      public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() { ... }
  }
  ```

- 使用以下路径进入项目swagger文档：`http://ip:port/swagger-ui/index.html`

- 以下路径获取API 的原始 OpenAPI/Swagger 描述：`http://ip:port/v2/api-docs`

- debug：

  一、静态资源访问的指向（该操作在API访问图片中做过）

  ```java
  @Configuration
  public class WebConfig implements WebMvcConfigurer {
      @Override
      public void addResourceHandlers(ResourceHandlerRegistry registry) {
          registry.addResourceHandler("/swagger-ui/**")
                  .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/3.52.5/");
          registry.addResourceHandler("/webjars/**")
                  .addResourceLocations("classpath:/META-INF/resources/webjars/");
      }
  }
  ```

  二、路径白名单：以下这些路由无需进行权限验证，加入白名单

  ```txt
  "/swagger-ui/**"、"/swagger-resources/**"、"/.well-known/**"、"/v2/api-docs"、
  "/v2/api-docs/**"、"/webjars/**"、"/error"
  ```

  

## 自定义异常处理





## 业务逻辑（Controller层和Service层）

### 缓存服务

`UmsAdminCacheService`、`UmsAdminCacheServiceImpl`

该项目只使用到了普通键值对，未使用Hash、Set、List

缓存类型：

- 单个用户信息：mall:ums:admin:test（username） - UmsAdmin umsAdmin
- 单个用户的资源列表：mall:ums:resourceList:16（adminId）- List<UmsResource> resourceList 

```txt
setAdmin(UmsAdmin admin)
getAdmin(String username)
delAdmin(Long adminId) // 根据Id删除单个用户信息

setResourceList(Long adminId, List<UmsResource> resourceList)
getResourceList(Long adminId)
delResourceList(Long adminId) // 根据Id删除对应的资源列表

delResourceListByRole(Long roleId) // 根据某个角色对应的多个用户，依次删除对应的资源列表缓存
delResourceListByRoles(List<Long> roleIds) // 根据多个角色对应的多个用户，依次删除对应的资源列表缓存
delResourceListByResource(Long resourceId) // 获取拥有某个资源的多个用户，依次删除对应的资源列表缓存
```



## Spring Security

### token：生成和验证流程

使用token的业务逻辑：登录、刷新token、登出

getAdminInfo(Principal)?

![login](img/login.png)



![refreshToken](img/refreshToken.png)



### Security 安全过滤器链		

#### SecurityConfig.java

通过这个类去配置 Spring Boot Security 的安全过滤器链。

**自动配置机制**：在Spring Security框架中，SecurityFilterChain是通过Spring的自动配置机制被纳入到应用的安全过滤器链中。程序启动时，Spring Boot的SecurityAutoConfiguration会扫描所有带有@Configuration注解的类，并查找任何实现了SecurityFilterChain的@Bean方法，找到后将其自动注册到应用的过滤器链中。无需显示调用。

```java
@Configuration
public class SecurityConfig {
    @Autowired
    private IgnoreUrlsConfig ignoreUrlsConfig;
    @Autowired
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler;
    @Autowired
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    @Autowired
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    @Autowired
    public DynamicSecurityService dynamicSecurityService;
    @Autowired
    public DynamicSecurityFilter dynamicSecurityFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = httpSecurity.authorizeRequests();
        // 不需要保护的资源路径通过访问
        for (String url: ignoreUrlsConfig.getUrls()) {
            registry.antMatchers(url).permitAll();
        }
        // 允许跨域请求的OPTIONS请求
        registry.antMatchers(HttpMethod.OPTIONS).permitAll();
        // 任何请求需要身份验证
        registry.and()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                // 关闭跨站请求防护及不使用session
                .and()
                .csrf()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // 自定义权限拒绝处理类
                .and()
//.addFilterBefore(jwtAuthenticationTokenFilter, FilterSecurityInterceptor.class);
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 有动态权限配置时添加动态权限校验过滤器
        if (dynamicSecurityService != null) {
            registry.and().addFilterBefore(dynamicSecurityFilter, FilterSecurityInterceptor.class);
        }
        return httpSecurity.build();
    }
}
```

注：

- `and()`：返回到之前的配置对象，以便继续链式调用。不同的规则之间用此隔开

- 规则顺序：`Spring Security`按照定义的顺序来检查请求匹配规则，并且一旦找到一个匹配项就会停止进一步检查。 因此，如果你首先定义了不需要保护的资源路径（使用 permitAll()），那么这些路径将不会受到后续更严格的规则的影响。（实测存疑？？？）

- `registry.and().authorizeRequests().anyRequest().authenticated()`：`authorizeRequests()`定义url模式和相应的访问规则，后面可跟`.antMatchrs("xxx")`表示具体的控制规则。`anyRequest().authenticated()`表示除了前面定义的规则，其余的请求都必须是通过认证的。

- `crsf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`：此处表示关闭跨站请求防护以及不使用`session`会话管理。将`SessionCreationPolicy`设置为`STATELESS`意味着每次请求视为独立的，认证信息不会保存到服务端，每个请求都要携带必要的认证信息（如JWT令牌），并且这些信息能在请求到达时正确解析并放到`SecurityConetxtHolder`中。

- `jwtAuthenticationTokenFiletr`和`dynamicSecurityFilter`这两个自定义的过滤器，如果依次`addFilterBefore(xxxx, FiletrSecurityInterceptor.class)`放在同一个过滤器前，此时的顺序是`jwtxxxx`过滤器在`dynamicxxx`之前。

- `addFilterBefore(jwtAuthenticationTokenFiletr, FiletrSecurityInterceptor.calss)`为何在这里不能使用。

  如果使用该行代码，在`jwtxxx`之前有一个`UsernamePasswordAuthenticationFilter`，按理说在会话管理状态为`STATSLESS`时，只对`POST`请求中携带用户名和密码表单的请求进行拦截处理，对携带`jwt`的请求不处理。实际使用中，发现在`jwtxxxx`过滤器前，`Usernamexxx`过滤器就已经起作用了，将未携带用户名和密码的请求验证失败并将`ROLE_ANONYMOUS`身份的匿名用户放到`SecurityContextHolder`中，导致后续的两个过滤器中用户被识别为匿名用户，无法通过验证。

- `restAuthenticationEntryPoint`和`restfulAccessDeniedHandler`：自定义的认证处理类，`restAuthenticationEntryPoint`用于处理未通过认证的请求的相应；`restfulAccessDeniedHandler`用于处理通过认证的用户但是角色或权限不足的清空，返回自定义的信息及格式。这两个配置需要出现在配置中，上面的程序中为显式调用，将会使用默认的处理类，自定义类将不会生效。

  ```java
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http
          .csrf().disable()
          .exceptionHandling()
              .authenticationEntryPoint(restAuthenticationEntryPoint) // 设置未认证入口点
              .accessDeniedHandler(restfulAccessDeniedHandler)     // 设置访问被拒绝处理器
          .and()
          //xxxxx
      return http.build();
  }
  ```

- `SecurityContextHolder.getContext().setAuthentication(xxx)`与`SecurityContextHolder.getContext().getAuthentication()`的线程安全：`SecurityContextHolder`使用的是线程局部变量`ThreadLocal`来存储，意味着每个线程都有自己独立的实例，多线程环境中，即使有多个请求，每个请求都会在自己的线程上操作独立的`Securityxxxx`，不会互相干扰，保证用户信息的存储和检索是线程安全的。

#### 历史版本

上述 `SecurityFilterChain` 的定义方法是在spring  security 5.x 版本之后的实现方法。

在spring security 5.x 版本之前常见的实现方式是继承`WebSecurityConfigurationAdapter`，重写`configure`方法

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationTokenFilter;

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf().disable()
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests().antMatchers("/auth/**").permitAll()
            .anyRequest().authenticated();
        httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

    // Other configurations...
}
```



#### jwt认证过滤器





## 其余配置

### SpringUtil.java


提供Spring框架中ApplicationContext的访问和Bean的获取功能

```txt
getBean(String name); // 通过name获取Bean 
getBean(Class<T> clazz); // 通过class获取Bean
getBean(String name, Class<T> clazz); // 通过name,以及Clazz返回指定的Bean
```

需要根据项目中是否存在相同类型的Bean、是否存在相同姓名不同类型的Bean确定使用哪种方法。

详情见Spring相关知识


### 跨域处理

```java
/*** 全局跨域设置*/
@Configuration
public class GlobalCorsConfig {
    /*** 允许跨域调用的过滤器*/
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名进行跨域调用
        config.addAllowedOriginPattern("*");
        // 允许跨域发送cookie
        config.setAllowCredentials(true);
        // 放行全部原始头信息，允许所有HTTP请求方法进行跨域调用（GET、POST、UPDATE、DELETE等）
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 将cors配置注册到所有的url路径上
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

上述配置设置全局跨域。

spring boot中常见的**局部跨域**设置方法如下：

```java
@RestController
@CrossOrigin(origins = "*")
public class HelloController {
    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }
}
```



### Logger日志

主流的日志相关的框架和工具：log4j、Logback、slf4j

三者关系：slf4j是一个日志接口，定义了方法但不执行，依赖具体实现。log4j和Logback都是具体实现。实际开发中，通常使用slf4j + Logback的组合，同时这一组合已经包含着在spring-boot-starter依赖中。

**实践：slf4j + Logback**

- 引入依赖，单独引入slf4j + logback 或 在spring-boot-starter中包含（无需再次引入）

- 配置一：logback.xml或logback-spring.xml（适用spring boot）文件中配置：略

- 配置二：spring boot的application.yml或application.properties文件中配置：略

- 配置一可以做到更加精细控制日志，配置二简易更方便

- 使用：

  ```java
  private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
  
  logger.debug();
  logger.info();
  logger.warn();
  logger.error();
  ```

  

### 静态资源路由

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	// 用于swagger-ui的静态资源获取
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/3.52.5/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        // 访问 resources/static/ 文件夹下的资源，包括html、css、js、图片、音视频等
        registry.addResourceHandler("static/**").addResourceLocations("classpath:/static/");
    }
}
```

