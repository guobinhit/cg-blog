# 详述 Spring Boot 中内嵌 Tomcat 的实现原理


对于一个 Spring Boot Web 工程来说，一个主要的依赖标志就是有`spring-boot-starter-web`这个`starter`，`spring-boot-starter-web`模块在 Spring Boot 中其实并没有代码存在，只是在`pom.xml`中携带了一些依赖，包括`web`、`webmvc`和`tomcat`等：

```xml
<dependencies>
    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-json</artifactId>
    </dependency>
    <dependency>
    	<groupId>org.springframework.boot</groupId>
    	<artifactId>spring-boot-starter-tomcat</artifactId>
    </dependency>
    <dependency>
    	<groupId>org.hibernate.validator</groupId>
    	<artifactId>hibernate-validator</artifactId>
    </dependency>
    <dependency>
    	<groupId>org.springframework</groupId>
    	<artifactId>spring-web</artifactId>
    </dependency>
    <dependency>
    	<groupId>org.springframework</groupId>
    	<artifactId>spring-webmvc</artifactId>
    </dependency>
</dependencies>
```

> Spring Boot 默认的 web 服务容器是 Tomcat ，如果想使用 Jetty 等来替换 Tomcat ，可以自行参考官方文档来解决。

`web`、`webmvc`和`tomcat`等提供了 Web 应用的运行环境，那`spring-boot-starter`则是让这些运行环境工作的开关，因为`spring-boot-starter`中会间接引入`spring-boot-autoconfigure`。

## WebServer 自动配置
在`spring-boot-autoconfigure`模块中，有处理关于`WebServer`的自动配置类 `ServletWebServerFactoryAutoConfiguration`。

### ServletWebServerFactoryAutoConfiguration
代码片段如下：

```java
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(ServletRequest.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
@EnableConfigurationProperties(ServerProperties.class)
@Import({ ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar.class,
		ServletWebServerFactoryConfiguration.EmbeddedTomcat.class,
		ServletWebServerFactoryConfiguration.EmbeddedJetty.class,
		ServletWebServerFactoryConfiguration.EmbeddedUndertow.class })
public class ServletWebServerFactoryAutoConfiguration
```

两个`Condition`表示当前运行环境是基于 Servlet 标准规范的 Web 服务：

- `ConditionalOnClass(ServletRequest.class)`：表示当前必须有`servlet-api`依赖存在
- `ConditionalOnWebApplication(type = Type.SERVLET)`：仅基于 Servlet 的 Web 应用程序

而`@EnableConfigurationProperties(ServerProperties.class)`注解的使用，则是为了加载`ServerProperties `配置，其包括了常见的`server.port`等配置属性。

通过`@Import`导入嵌入式容器相关的自动配置类，有`EmbeddedTomcat`、`EmbeddedJetty`和`EmbeddedUndertow`。

综合来看，`ServletWebServerFactoryAutoConfiguration`自动配置类中主要做了以下几件事情：

- 导入了内部类`BeanPostProcessorsRegistrar`，它实现了`ImportBeanDefinitionRegistrar`，可以实现`ImportBeanDefinitionRegistrar`来注册额外的`BeanDefinition`。
- 导入了`ServletWebServerFactoryConfiguration.EmbeddedTomcat`等嵌入容器相关配置（我们主要关注 Tomcat 相关的配置）。
- 注册了`ServletWebServerFactoryCustomizer`、`TomcatServletWebServerFactoryCustomizer`两个`WebServerFactoryCustomizer`类型的 Bean。

下面就针对这几个点，做下详细的分析。

### BeanPostProcessorsRegistrar
`BeanPostProcessorsRegistrar`这个内部类的代码如下（省略了部分代码）：

```java
public static class BeanPostProcessorsRegistrar
    implements ImportBeanDefinitionRegistrar, BeanFactoryAware {
    // 省略代码
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        if (this.beanFactory == null) {
            return;
        }
        // 注册 WebServerFactoryCustomizerBeanPostProcessor
        registerSyntheticBeanIfMissing(registry,
                                       "webServerFactoryCustomizerBeanPostProcessor",
                                       WebServerFactoryCustomizerBeanPostProcessor.class);
        // 注册 errorPageRegistrarBeanPostProcessor
        registerSyntheticBeanIfMissing(registry,
                                       "errorPageRegistrarBeanPostProcessor",
                                       ErrorPageRegistrarBeanPostProcessor.class);
    }
    // 省略代码
}
```

上面这段代码中，注册了两个 Bean，一个`WebServerFactoryCustomizerBeanPostProcessor`，一个`ErrorPageRegistrarBeanPostProcessor`；这两个都实现类`BeanPostProcessor`接口，属于 Bean 的后置处理器，作用是在 Bean 初始化前后加一些自己的逻辑处理。

- `WebServerFactoryCustomizerBeanPostProcessor`：作用是在`WebServerFactory`初始化时调用上面自动配置类注入的那些`WebServerFactoryCustomizer`，然后调用`WebServerFactoryCustomizer`中的`customize`方法来处理`WebServerFactory`。
- `ErrorPageRegistrarBeanPostProcessor`：和上面的作用差不多，不过这个是处理`ErrorPageRegistrar`的。

下面简单看下`WebServerFactoryCustomizerBeanPostProcessor`中的代码：

```java
public class WebServerFactoryCustomizerBeanPostProcessor
		implements BeanPostProcessor, BeanFactoryAware {
    // 省略部分代码
    
    // 在 postProcessBeforeInitialization 方法中，如果当前 bean 是 WebServerFactory，则进行
    // 一些后置处理
    @Override
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		if (bean instanceof WebServerFactory) {
			postProcessBeforeInitialization((WebServerFactory) bean);
		}
		return bean;
	}
    // 这段代码就是拿到所有的 Customizers ，然后遍历调用这些 Customizers 的 customize 方法
    private void postProcessBeforeInitialization(WebServerFactory webServerFactory) {
		LambdaSafe
				.callbacks(WebServerFactoryCustomizer.class, getCustomizers(),
						webServerFactory)
				.withLogger(WebServerFactoryCustomizerBeanPostProcessor.class)
				.invoke((customizer) -> customizer.customize(webServerFactory));
	}
    
    // 省略部分代码
}
```
## 自动配置类中注册的两个 Customizer Bean
这两个`Customizer`实际上就是去处理一些配置值，然后绑定到各自的工厂类的。

### WebServerFactoryCustomizer
将`serverProperties`配置值绑定给`ConfigurableServletWebServerFactory`对象实例上。

```java
@Override
public void customize(ConfigurableServletWebServerFactory factory) {
    PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
    // 端口
    map.from(this.serverProperties::getPort).to(factory::setPort);
    // address
    map.from(this.serverProperties::getAddress).to(factory::setAddress);
    // contextPath
    map.from(this.serverProperties.getServlet()::getContextPath)
        .to(factory::setContextPath);
    // displayName
    map.from(this.serverProperties.getServlet()::getApplicationDisplayName)
        .to(factory::setDisplayName);
    // session 配置
    map.from(this.serverProperties.getServlet()::getSession).to(factory::setSession);
    // ssl
    map.from(this.serverProperties::getSsl).to(factory::setSsl);
    // jsp
    map.from(this.serverProperties.getServlet()::getJsp).to(factory::setJsp);
    // 压缩配置策略实现
    map.from(this.serverProperties::getCompression).to(factory::setCompression);
    // http2 
    map.from(this.serverProperties::getHttp2).to(factory::setHttp2);
    // serverHeader
    map.from(this.serverProperties::getServerHeader).to(factory::setServerHeader);
    // contextParameters
    map.from(this.serverProperties.getServlet()::getContextParameters)
        .to(factory::setInitParameters);
}
```
### TomcatServletWebServerFactoryCustomizer
相比于上面那个，这个`customizer`主要处理 Tomcat 相关的配置值。

```java
@Override
public void customize(TomcatServletWebServerFactory factory) {
    // 拿到 tomcat 相关的配置
    ServerProperties.Tomcat tomcatProperties = this.serverProperties.getTomcat();
    // server.tomcat.additional-tld-skip-patterns
    if (!ObjectUtils.isEmpty(tomcatProperties.getAdditionalTldSkipPatterns())) {
        factory.getTldSkipPatterns()
            .addAll(tomcatProperties.getAdditionalTldSkipPatterns());
    }
    // server.redirectContextRoot
    if (tomcatProperties.getRedirectContextRoot() != null) {
        customizeRedirectContextRoot(factory,
                                     tomcatProperties.getRedirectContextRoot());
    }
    // server.useRelativeRedirects
    if (tomcatProperties.getUseRelativeRedirects() != null) {
        customizeUseRelativeRedirects(factory,
                                      tomcatProperties.getUseRelativeRedirects());
    }
}
```
## WebServerFactory
用于创建`WebServer`的工厂的标记接口。
### 类体系结构

![class-structure](https://github.com/guobinhit/cg-blog/blob/master/images/others/spring-boot-nested-tomcat-principle/class-structure.png)

上图为`WebServerFactory`到`TomcatServletWebServerFactory`的整个类结构关系。
### TomcatServletWebServerFactory
`TomcatServletWebServerFactory`是用于获取 Tomcat 作为`WebServer`的工厂类实现，其中最核心的方法就是`getWebServer`，获取一个`WebServer`对象实例。

```java
@Override
public WebServer getWebServer(ServletContextInitializer... initializers) {
    // 创建一个 Tomcat 实例
    Tomcat tomcat = new Tomcat();
    // 创建一个 Tomcat 实例工作空间目录
    File baseDir = (this.baseDirectory != null) ? this.baseDirectory
        : createTempDir("tomcat");
    tomcat.setBaseDir(baseDir.getAbsolutePath());
    // 创建连接对象
    Connector connector = new Connector(this.protocol);
    tomcat.getService().addConnector(connector);
    // 1
    customizeConnector(connector);
    tomcat.setConnector(connector);
    tomcat.getHost().setAutoDeploy(false);
    // 配置 Engine，没有什么实质性的操作，可忽略
    configureEngine(tomcat.getEngine());
    // 一些附加链接，默认是 0 个
    for (Connector additionalConnector : this.additionalTomcatConnectors) {
        tomcat.getService().addConnector(additionalConnector);
    }
    // 2
    prepareContext(tomcat.getHost(), initializers);
    // 返回 webServer
    return getTomcatWebServer(tomcat);
}
```
- `customizeConnector`： 给`Connector`设置`port`、`protocolHandler`、`uriEncoding`等。`Connector`构造的逻辑主要是在 NIO 和 APR 选择中选择一个协议，然后反射创建实例并强转为`ProtocolHandler`
- `prepareContext`：这里并不是说准备当前 Tomcat 运行环境的上下文信息，而是准备一个`StandardContext`，也就是准备一个 Web App。

### 准备 Web App Context 容器
对于 Tomcat 来说，每个`context`就是映射到一个 Web App 的，所以`prepareContext`做的事情就是将 Web 应用映射到一个`TomcatEmbeddedContext`，然后加入到`Host`中

```java
protected void prepareContext(Host host, ServletContextInitializer[] initializers) {
    File documentRoot = getValidDocumentRoot();
    // 创建一个 TomcatEmbeddedContext 对象
    TomcatEmbeddedContext context = new TomcatEmbeddedContext();
    if (documentRoot != null) {
        context.setResources(new LoaderHidingResourceRoot(context));
    }
    // 设置描述此容器的名称字符串。在属于特定父项的子容器集内，容器名称必须唯一。
    context.setName(getContextPath());
    // 设置此Web应用程序的显示名称。
    context.setDisplayName(getDisplayName());
    // 设置 webContextPath  默认是   /
    context.setPath(getContextPath());
    File docBase = (documentRoot != null) ? documentRoot
        : createTempDir("tomcat-docbase");
    context.setDocBase(docBase.getAbsolutePath());
    // 注册一个FixContextListener监听，这个监听用于设置context的配置状态以及是否加入登录验证的逻辑
    context.addLifecycleListener(new FixContextListener());
    // 设置 父 ClassLoader
    context.setParentClassLoader(
        (this.resourceLoader != null) ? this.resourceLoader.getClassLoader()
        : ClassUtils.getDefaultClassLoader());
    // 覆盖Tomcat的默认语言环境映射以与其他服务器对齐。
    resetDefaultLocaleMapping(context);
    // 添加区域设置编码映射（请参阅Servlet规范2.4的5.4节）
    addLocaleMappings(context);
    // 设置是否使用相对地址重定向
    context.setUseRelativeRedirects(false);
    try {
        context.setCreateUploadTargets(true);
    }
    catch (NoSuchMethodError ex) {
        // Tomcat is < 8.5.39. Continue.
    }
    configureTldSkipPatterns(context);
    // 设置 WebappLoader ，并且将 父 classLoader 作为构建参数
    WebappLoader loader = new WebappLoader(context.getParentClassLoader());
    // 设置 WebappLoader 的 loaderClass 值
    loader.setLoaderClass(TomcatEmbeddedWebappClassLoader.class.getName());
    // 会将加载类向上委托
    loader.setDelegate(true);
    context.setLoader(loader);
    if (isRegisterDefaultServlet()) {
        addDefaultServlet(context);
    }
    // 是否注册 jspServlet
    if (shouldRegisterJspServlet()) {
        addJspServlet(context);
        addJasperInitializer(context);
    }
    context.addLifecycleListener(new StaticResourceConfigurer(context));
    ServletContextInitializer[] initializersToUse = mergeInitializers(initializers);
    // 在 host 中 加入一个 context 容器
    // add时给context注册了个内存泄漏跟踪的监听MemoryLeakTrackingListener,详见 addChild 方法
    host.addChild(context);
    //对context做了些设置工作，包括TomcatStarter(实例化并set给context),
    // LifecycleListener,contextValue,errorpage,Mime,session超时持久化等以及一些自定义工作
    configureContext(context, initializersToUse);
    // postProcessContext 方法是空的，留给子类重写用的
    postProcessContext(context);
}
```

从上面可以看下，`WebappLoader`可以通过`setLoaderClass`和`getLoaderClass`这两个方法可以更改`loaderClass`的值。所以也就意味着，我们可以自己定义一个继承`webappClassLoader`的类，来更换系统自带的默认实现。

### 初始化 TomcatWebServer
在`getWebServer`方法的最后就是构建一个`TomcatWebServer`。

```java
// org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
    // new 一个 TomcatWebServer
    return new TomcatWebServer(tomcat, getPort() >= 0);
}
// org.springframework.boot.web.embedded.tomcat.TomcatWebServer
public TomcatWebServer(Tomcat tomcat, boolean autoStart) {
    Assert.notNull(tomcat, "Tomcat Server must not be null");
    this.tomcat = tomcat;
    this.autoStart = autoStart;
    // 初始化
    initialize();
}
```
这里主要是`initialize`这个方法，这个方法中将会启动 Tomcat 服务：

```java
private void initialize() throws WebServerException {
    logger.info("Tomcat initialized with port(s): " + getPortsDescription(false));
    synchronized (this.monitor) {
        try {
            // 对全局原子变量 containerCounter+1，由于初始值是-1，
    // 所以 addInstanceIdToEngineName 方法内后续的获取引擎并设置名字的逻辑不会执行
            addInstanceIdToEngineName();
			// 获取 Context 
            Context context = findContext();
            // 给 Context 对象实例生命周期监听器
            context.addLifecycleListener((event) -> {
                if (context.equals(event.getSource())
                    && Lifecycle.START_EVENT.equals(event.getType())) {
                    // 将上面new的connection以service（这里是StandardService[Tomcat]）做key保存到
                    // serviceConnectors中，并将 StandardService 中的connectors 与 service 解绑(connector.setService((Service)null);)，
                    // 解绑后下面利用LifecycleBase启动容器就不会启动到Connector了
                    removeServiceConnectors();
                }
            });
            // 启动服务器以触发初始化监听器
            this.tomcat.start();
            // 这个方法检查初始化过程中的异常，如果有直接在主线程抛出，
            // 检查方法是TomcatStarter中的 startUpException，这个值是在 Context 启动过程中记录的
            rethrowDeferredStartupExceptions();
            try {
                // 绑定命名的上下文和classloader，
                ContextBindings.bindClassLoader(context, context.getNamingToken(),
                                                getClass().getClassLoader());
            }
            catch (NamingException ex) {
                // 设置失败不需要关心
            }

			// 与Jetty不同，Tomcat所有的线程都是守护线程，所以创建一个非守护线程
            // （例：Thread[container-0,5,main]）来避免服务到这就shutdown了
            startDaemonAwaitThread();
        }
        catch (Exception ex) {
            stopSilently();
            throw new WebServerException("Unable to start embedded Tomcat", ex);
        }
    }
}
```
查找`Context`，实际上就是查找一个Tomcat 中的一个 Web 应用，Spring Boot 中默认启动一个 Tomcat ，并且一个 Tomcat 中只有一个 Web 应用（FATJAR 模式下，应用与 Tomcat 是 1:1 关系），所有在遍历`Host`下的`Container`时，如果`Container`类型是`Context`，就直接返回了。

```java
private Context findContext() {
    for (Container child : this.tomcat.getHost().findChildren()) {
        if (child instanceof Context) {
            return (Context) child;
        }
    }
    throw new IllegalStateException("The host does not contain a Context");
}
```
## Tomcat 启动过程
在`TomcatWebServer`的`initialize`方法中会执行 Tomcat 的启动。

```java
// Start the server to trigger initialization listeners
this.tomcat.start();
```
`org.apache.catalina.startup.Tomcat`的`start`方法：

```java
public void start() throws LifecycleException {
    // 初始化 server
    getServer();
    // 启动 server
    server.start();
}
```
### 初始化 Server
初始化`Server`实际上就是构建一个`StandardServer`对象实例，关于 Tomcat 中的`Server`可以参考附件中的说明。

```java
public Server getServer() {
	// 如果已经存在的话就直接返回
    if (server != null) {
        return server;
    }
	// 设置系统属性 catalina.useNaming
    System.setProperty("catalina.useNaming", "false");
	// 直接 new 一个 StandardServer
    server = new StandardServer();
	// 初始化 baseDir （catalina.base、catalina.home、 ~/tomcat.{port}）
    initBaseDir();

    // Set configuration source
    ConfigFileLoader.setSource(new CatalinaBaseConfigurationSource(new File(basedir), null));

    server.setPort( -1 );

    Service service = new StandardService();
    service.setName("Tomcat");
    server.addService(service);
    return server;
}
```
## 小结
上面对 Spring Boot 中内嵌 Tomcat 的过程做了分析，这个过程实际上并不复杂，就是在刷新 Spring 上下文的过程中将 Tomcat 容器启动起来，并且将当前应用绑定到一个`Context`，然后添加了`Host`。下图是程序的执行堆栈和执行内嵌 Tomcat 初始化和启动的时机。

![nest-tomcat-start](https://github.com/guobinhit/cg-blog/blob/master/images/others/spring-boot-nested-tomcat-principle/nest-tomcat-start.png)

下面总结下整个过程：

- 通过自定配置注册相关的 Bean ，包括一些`Factory`和后置处理器等
- 上下文刷新阶段，执行创建`WebServer`，这里需要用到前一个阶段所注册的 Bean
  - 包括创建`ServletContext`
  - 实例化`WebServer`
- 创建 Tomcat 实例、创建`Connector`连接器
- 绑定应用到`ServletContext`，并添加相关的生命周期范畴内的监听器，然后将`Context`添加到`Host`中
- 实例化`webServer`并且启动 Tomcat 服务

Spring Boot 的 Fatjar 方式没有提供共享 Tomcat 的实现逻辑，就是两个 FATJAT 启动可以只实例化一个 Tomcat 实例（包括`Connector`和`Host`），从前面的分析知道，每个 Web 应用（一个 FATJAT 对应的应用）实例上就是映射到一个`Context`；而对于`war`方式，一个`Host `下面是可以挂载多个`Context`的。


## 附：Tomcat 架构图及组件说明

![tomcat-structure](https://github.com/guobinhit/cg-blog/blob/master/images/others/spring-boot-nested-tomcat-principle/tomcat-structure.png)


| 组件名称 | 说明   |
|:--------| :-------------|
| `Server` | 表示整个 Servlet 容器，因此 Tomcat 运行环境中只有唯一一个`Server`实例 |
| `Service` | `Service`表示一个或者多个`Connector`的集合，这些`Connector`共享同一个`Container`来处理其请求。在同一个 Tomcat 实例内可以包含任意多个`Service`实例，他们彼此独立。 |
|`Connector`  | Tomcat 连接器，用于监听和转化 Socket 请求，同时将读取的 Socket 请求交由`Container`处理，支持不同协议以及不同的 I/O 方式。 |
|`Container` | `Container`表示能够执行客户端请求并返回响应的一类对象，在 Tomcat 中存在不同级别的容器：`Engine`、`Host`、`Context`、`Wrapper` |
| `Engine` | `Engine`表示整个 Servlet 引擎。在 Tomcat 中，`Engine`为最高层级的容器对象，虽然`Engine`不是直接处理请求的容器，确是获取目标容器的入口 |
| `Host` |`Host`作为一类容器，表示 Servlet 引擎（即`Engine`）中的虚拟机，与一个服务器的网络名有关，如域名等。客户端可以使用这个网络名连接服务器，这个名称必须要在 DNS 服务器上注册  |
| `Context` | `Context`作为一类容器，用于表示`ServletContext`，在 Servlet 规范中，一个`ServletContext`即表示一个独立的 Web 应用 |
|`Wrapper`  | `Wrapper`作为一类容器，用于表示 Web 应用中定义的 Servlet |
| `Executor` | 表示 Tomcat 组件间可以共享的线程池 |


