# 详述 tomcat 的 server.xml 配置文件

> **博主说**：Tomcat 服务器是一个免费的开放源代码的 Web 应用服务器，属于轻量级应用服务器，在中小型系统和并发访问用户不是很多的场合下被普遍使用，是开发和调试 JSP 程序的首选。同时，Tomcat 也是我们日常工作中，接触最多最频繁的服务器之一，了解其配置，有助于深化我们对 Tomcat 的理解。

## 1 前言

　　Tomcat 隶属于 Apache 基金会，是开源的轻量级 Web 应用服务器，使用非常广泛。`server.xml`是 Tomcat 中最重要的配置文件，`server.xml`的每一个元素都对应了 Tomcat 中的一个组件；通过对 XML 文件中元素的配置，可以实现对 Tomcat 中各个组件的控制。因此，学习`server.xml`文件的配置，对于了解和使用 Tomcat 至关重要。

　　本文将通过实例，介绍`server.xml`中各个组件的配置，并详细说明 Tomcat 各个核心组件的作用以及各个组件之间的相互关系。

　　**说明**：由于`server.xml`文件中元素与 Tomcat 中组件的对应关系，后文中为了描述方便，“元素”和“组件”的使用不严格区分。

## 2 `server.xml`配置实例

　　`server.xml`位于`$TOMCAT_HOME/conf`目录下，下面是一个`server.xml`实例。后文中将结合该实例讲解`server.xml`中，各个元素的含义和作用；在阅读后续章节过程中，可以对照该 XML 文档进行理解。

```
<Server port="8005" shutdown="SHUTDOWN">
   <Listener className="org.apache.catalina.startup.VersionLoggerListener" />
   <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
   <Listener className="org.apache.catalina.core.JasperListener" />
   <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
   <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
   <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />
 
   <GlobalNamingResources>
     <Resource name="UserDatabase" auth="Container"
               type="org.apache.catalina.UserDatabase"
               description="User database that can be updated and saved"
               factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
               pathname="conf/tomcat-users.xml" />
   </GlobalNamingResources>
  
   <Service name="Catalina">
     <Connector port="8080" protocol="HTTP/1.1"
                connectionTimeout="20000"
                redirectPort="8443" />
     <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
     <Engine name="Catalina" defaultHost="localhost">
       <Realm className="org.apache.catalina.realm.LockOutRealm">
         <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
                resourceName="UserDatabase"/>
       </Realm>
  
       <Host name="localhost"  appBase="webapps"
             unpackWARs="true" autoDeploy="true">
         <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs"
                prefix="localhost_access_log." suffix=".txt"
                pattern="%h %l %u %t &quot;%r&quot; %s %b" />
       </Host>
     </Engine>
   </Service>
</Server>
```

## 3 `server.xml`文档的整体结构和元素分类

### 3.1 整体结构

　　`server.xml`的整体结构如下：

```
<Server>
     <Service>
         <Connector />
         <Connector />
         <Engine>
             <Host>
                 <Context /><!-- 现在常常使用自动部署，不推荐配置Context元素，Context小节有详细说明 -->
             </Host>
         </Engine>
     </Service>
</Server>
```

该结构中只给出了 Tomcat 的核心组件，除了核心组件外，Tomcat 还有一些其他组件，下面介绍一下组件的分类。

### 3.2 元素分类

　　`server.xml`文件中的元素可以分为以下 4 类：

**第 1 类：顶层元素，`<Server>`和`<Service>`**

　　`<Server>`元素是整个配置文件的根元素，`<Service>`元素则代表一个`Engine`元素以及一组与之相连的`Connector`元素。

**第 2 类：连接器，`<Connector>`**

　　`<Connector>`代表了外部客户端发送请求到特定 Service 的接口；同时也是外部客户端从特定 Service 接收响应的接口。

**第 3 类：容器，`<Engine>`、`<Host>`和`<Context>`**

　　容器的功能是处理 Connector 接收进来的请求，并产生相应的响应。Engine、Host 和 Context 都是容器，但它们不是平行的关系，而是父子关系：Engine 包含 Host，Host 包含 Context。一个 Engine 组件可以处理 Service 中的所有请求，一个 Host 组件可以处理发向一个特定虚拟主机的所有请求，一个 Context 组件可以处理一个特定 Web 应用的所有请求。

**第 4 类：内嵌组件，可以内嵌到容器中的组件**

　　实际上，Server、Service、Connector、Engine、Host 和 Context 是最重要的最核心的 Tomcat 组件，其他组件都可以归为内嵌组件。下面将详细介绍 Tomcat 中各个核心组件的作用，以及相互之间的关系。

## 4 核心组件

　　本部分将分别介绍各个核心组件的作用、特点以及配置方式等。

### 4.1 Server

　　Server 元素在最顶层，代表整个 Tomcat 容器，因此它必须是`server.xml`中唯一一个最外层的元素。一个 Server 元素中可以有一个或多个 Service 元素。

　　在第一部分的例子中，在最外层有一个`<Server>`元素，`shutdown`属性表示关闭 Server 的指令；`port`属性表示 Server 接收`shutdown`指令的端口号，设为`-1`可以禁掉该端口。

　　Server 的主要任务，就是提供一个接口让客户端能够访问到这个 Service 集合，同时维护它所包含的所有的 Service 的生命周期，包括如何初始化、如何结束服务、如何找到客户端要访问的 Service 等。

### 4.2 Service

　　Service 的作用，是在 Connector 和 Engine 外面包了一层，把它们组装在一起，对外提供服务。一个 Service 可以包含多个 Connector，但是只能包含一个 Engine；其中 Connector 的作用是从客户端接收请求，Engine 的作用是处理接收进来的请求。

　　在第一部分的例子中，Server 中包含一个名称为`Catalina`的 Service。实际上，Tomcat 可以提供多个 Service，不同的 Service 监听不同的端口，后文会有介绍。

### 4.3 Connector

　　Connector 的主要功能，是接收连接请求，创建 Request 和 Response 对象用于和请求端交换数据；然后分配线程让 Engine 来处理这个请求，并把产生的 Request 和 Response 对象传给 Engine。

　　通过配置 Connector，可以控制请求 Service 的协议及端口号。在第一部分的例子中，Service 包含两个 Connector： 

```
<Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
<Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
```

　　通过配置第 1 个 Connector，客户端可以通过 8080 端口号使用`http`协议访问 Tomcat。其中，`protocol`属性规定了请求的协议，`port`规定了请求的端口号，`redirectPort`表示当强制要求`https`而请求是`http`时，重定向至端口号为 8443 的 Connector，`connectionTimeout`表示连接的超时时间。在这个例子中，Tomcat 监听 HTTP 请求，使用的是 8080 端口，而不是正式的 80 端口；实际上，在正式的生产环境中，Tomcat 也常常监听 8080 端口，而不是 80 端口。这是因为在生产环境中，很少将 Tomcat 直接对外开放接收请求，而是在 Tomcat 和客户端之间加一层代理服务器（如 nginx），用于请求的转发、负载均衡、处理静态文件等；通过代理服务器访问 Tomcat 时，是在局域网中，因此一般仍使用 8080 端口。

　　通过配置第 2 个 Connector，客户端可以通过 8009 端口号使用`AJP`协议访问 Tomcat。`AJP`协议负责和其他的 HTTP 服务器（如 Apache）建立连接；在把 Tomcat 与其他 HTTP 服务器集成时，就需要用到这个连接器。之所以使用 Tomcat 和其他服务器集成，是因为 Tomcat 可以用作 Servlet/JSP 容器，但是对静态资源的处理速度较慢，不如 Apache 和 IIS 等 HTTP 服务器；因此常常将 Tomcat 与 Apache 等集成，前者作 Servlet 容器，后者处理静态资源，而 AJP 协议便负责 Tomcat 和 Apache 的连接。Tomcat 与 Apache 等集成的原理如下图所示：

![web](http://img.blog.csdn.net/20170825194252961)

### 4.4 Engine

　　Engine 组件在 Service 组件中有且只有一个；Engine 是 Service 组件中的请求处理组件。Engine 组件从一个或多个 Connector 中接收请求并处理，并将完成的响应返回给 Connector，最终传递给客户端。

　　前面已经提到过，Engine、Host 和 Context 都是容器，但它们不是平行的关系，而是父子关系：Engine 包含 Host，Host 包含 Context。

　　在第一部分的例子中，Engine 的配置语句如下：

```
<Engine name="Catalina" defaultHost="localhost">
```

其中，`name`属性用于日志和错误信息，在整个 Server 中应该唯一。`defaultHost`属性指定了默认的 host 名称，当发往本机的请求指定的 host 名称不存在时，一律使用 defaultHost 指定的 host 进行处理；因此，`defaultHost`的值，必须与 Engine 中的一个 Host 组件的`name`属性值匹配。

### 4.5 Host

**4.5.1 Engine 与 Host**

　　Host 是 Engine 的子容器。Engine 组件中可以内嵌 1 个或多个 Host 组件，每个 Host 组件代表 Engine 中的一个虚拟主机。Host 组件至少有一个，且其中一个的`name`必须与 Engine 组件的`defaultHost`属性相匹配。

**4.5.2 Host 的作用**

　　Host 虚拟主机的作用，是运行多个 Web 应用（一个 Context 代表一个 Web 应用），并负责安装、展开、启动和结束每个 Web 应用。

　　Host 组件代表的虚拟主机，对应了服务器中一个网络名实体（如`www.test.com`，或 IP 地址`116.25.25.25`），为了使用户可以通过网络名连接 Tomcat 服务器，这个名字应该在 DNS 服务器上注册。

　　客户端通常使用主机名来标识它们希望连接的服务器；该主机名也会包含在 HTTP 请求头中。Tomcat 从 HTTP 头中提取出主机名，寻找名称匹配的主机。如果没有匹配，请求将发送至默认主机。因此默认主机不需要是在 DNS 服务器中注册的网络名，因为任何与所有 Host 名称不匹配的请求，都会路由至默认主机。

**4.5.3 Host 的配置**

　　在第一部分的例子中，Host 的配置如下：

```
<Host name="localhost" appBase="webapps" unpackWARs="true" autoDeploy="true">
```
下面对其中配置的属性进行说明：

　　`name`属性指定虚拟主机的主机名，一个 Engine 中有且仅有一个 Host 组件的`name`属性与 Engine 组件的`defaultHost`属性相匹配；一般情况下，主机名需要是在 DNS 服务器中注册的网络名，但是 Engine 指定的`defaultHost`不需要，原因在前面已经说明。

　　`unpackWARs`指定了是否将代表 Web 应用的 WAR 文件解压；如果为`true`，通过解压后的文件结构运行该 Web 应用，如果为`false`，直接使用 WAR 文件运行 Web 应用。

　　Host 的`autoDeploy`和`appBase`属性，与 Host 内 Web 应用的自动部署有关；此外，本例中没有出现的`xmlBase`和`deployOnStartup`属性，也与 Web 应用的自动部署有关；将在下一节（Context）中介绍。

### 4.6 Context

**4.6.1 Context 的作用**

　　Context 元素代表在特定虚拟主机上运行的一个 Web 应用。在后文中，提到 Context、应用或 Web 应用，它们指代的都是 Web 应用。每个 Web 应用基于 WAR 文件，或 WAR 文件解压后对应的目录（这里称为应用目录）。Context 是 Host 的子容器，每个 Host 中可以定义任意多的 Context 元素。

　　在第一部分的例子中，可以看到`server.xml`配置文件中并没有出现 Context 元素的配置。这是因为，Tomcat 开启了自动部署，Web 应用没有在`server.xml`中配置静态部署，而是由 Tomcat 通过特定的规则自动部署。下面介绍一下 Tomcat 自动部署 Web 应用的机制。

**4.6.2 Web 应用自动部署**

**第 1 点：Host 的配置**

　　要开启 Web 应用的自动部署，需要配置所在的虚拟主机；配置的方式就是前面提到的 Host 元素的`deployOnStartup`和`autoDeploy`属性。如果`deployOnStartup`和`autoDeploy`设置为`true`，则 tomcat 启动自动部署：当检测到新的 Web 应用或 Web 应用的更新时，会触发应用的部署（或重新部署）。二者的主要区别在于，`deployOnStartup`为`true`时，Tomcat 在启动时检查 Web 应用，且检测到的所有 Web 应用视作新应用；`autoDeploy`为`true`时，Tomcat 在运行时定期检查新的 Web 应用或 Web 应用的更新。除此之外，二者的处理相似。

　　通过配置`deployOnStartup`和`autoDeploy`可以开启虚拟主机自动部署 Web 应用；实际上，自动部署依赖于检查是否有新的或更改过的 Web 应用，而 Host 元素的`appBase`和`xmlBase`设置了检查 Web 应用更新的目录。

　　其中，`appBase`属性指定 Web 应用所在的目录，默认值是`webapps`，这是一个相对路径，代表 Tomcat 根目录下`webapps`文件夹。`xmlBase`属性指定 Web 应用的 XML 配置文件所在的目录，默认值为`conf/<engine_name>/<host_name>`，例如第一部分的例子中，主机`localhost`的`xmlBase`的默认值是`$TOMCAT_HOME/conf/Catalina/localhost`。

**第 2 点：检查 Web 应用更新**

　　一个 Web 应用可能包括以下文件：XML 配置文件，WAR 包，以及一个应用目录（该目录包含 Web 应用的文件结构）；其中 XML 配置文件位于`xmlBase`指定的目录，WAR 包和应用目录位于`appBase`指定的目录。Tomcat 按照如下的顺序进行扫描，来检查应用更新：

 - 扫描虚拟主机指定的`xmlBase`下的 XML 配置文件；
 - 扫描虚拟主机指定的`appBase`下的 WAR 文件；
 - 扫描虚拟主机指定的`appBase`下的应用目录。

**第 3 点：`<Context>`元素的配置**

　　Context 元素最重要的属性是`docBase`和`path`，此外`reloadable`属性也比较常用。

　　`docBase`指定了该 Web 应用使用的 WAR 包路径，或应用目录。需要注意的是，在自动部署场景下（配置文件位于`xmlBase`中），`docBase`不在`appBase`目录中，才需要指定；如果`docBase`指定的 WAR 包或应用目录就在`docBase`中，则不需要指定，因为 Tomcat 会自动扫描`appBase`中的 WAR 包和应用目录，指定了反而会造成问题。

　　`path`指定了访问该 Web 应用的上下文路径，当请求到来时，Tomcat 根据 Web 应用的`path`属性与 URI 的匹配程度来选择 Web 应用处理相应请求。例如，Web 应用`app1`的`path`属性是`/app1`，Web 应用`app2`的`path`属性是`/app2`，那么请求`/app1/index.html`会交由`app1`来处理；而请求`/app2/index.html`会交由`app2`来处理。如果一个 Context 元素的`path`属性为`""`，那么这个 Context 是虚拟主机的默认 Web 应用；当请求的 URI 与所有的`path`都不匹配时，使用该默认`Web`应用来处理。

　　但是，需要注意的是，在自动部署场景下（配置文件位于`xmlBase`中），不能指定`path`属性，`path`属性由配置文件的文件名、WAR 文件的文件名或应用目录的名称自动推导出来。如扫描 Web 应用时，发现了`xmlBase`目录下的`app1.xml`，或`appBase`目录下的`app1.WAR`或`app1`应用目录，则该 Web 应用的`path`属性是`app1`。如果名称不是`app1`而是`ROOT`，则该 Web 应用是虚拟主机默认的 Web 应用，此时`path`属性推导为`""`。

　　`reloadable`属性指示 Tomcat 是否在运行时监控在`WEB-INF/classes`和`WEB-INF/lib`目录下`class`文件的改动。如果值为`true`，那么当`class`文件改动时，会触发 Web 应用的重新加载。在开发环境下，`reloadable`设置为`true`便于调试；但是在生产环境中设置为`true`会给服务器带来性能压力，因此`reloadable`参数的默认值为`false`。

　　下面来看自动部署时，`xmlBase`下的 XML 配置文件`app1.xml`的例子：

```
<Context docBase="D:\Program Files\app1.war" reloadable="true"/>
```

在该例子中，`docBase`位于 Host 的`appBase`目录之外；`path`属性没有指定，而是根据`app1.xml`自动推导为`app1`；由于是在开发环境下，因此`reloadable`设置为`true`，便于开发调试。

**第 4 点：自动部署举例**

　　最典型的自动部署，就是当我们安装完 Tomcat 后，`$TOMCAT_HOME/webapps`目录下有如下文件夹：

![tomcat](http://img.blog.csdn.net/20170826120131612)

　　当我们启动 Tomcat 后，可以使用`http://localhost:8080/`来访问 Tomcat，其实访问的就是 ROOT 对应的 Web 应用；我们也可以通过`http://localhost:8080/docs`来访问`docs`应用，同理我们可以访问`examples/host-manager/manager`这几个 Web 应用。

**4.6.3 `server.xml`中静态部署 Web 应用**

　　除了自动部署，我们也可以在`server.xml`中通过`<context>`元素静态部署 Web 应用。静态部署与自动部署是可以共存的。在实际应用中，并不推荐使用静态部署，因为`server.xml`是不可动态重加载的资源，服务器一旦启动了以后，要修改这个文件，就得重启服务器才能重新加载。而自动部署可以在 Tomcat 运行时通过定期的扫描来实现，不需要重启服务器。`server.xml`中使用 Context 元素配置 Web 应用，Context 元素应该位于 Host 元素中。举例如下：

```
<Context path="/" docBase="D:\Program Files \app1.war" reloadable="true"/>
```

 - `docBase`：静态部署时，`docBase`可以在`appBase`目录下，也可以不在；本例中，`docBase`不在`appBase`目录下；
 - `path`：静态部署时，可以显式指定`path`属性，但是仍然受到了严格的限制：只有当自动部署完全关闭（`deployOnStartup`和`autoDeploy`都为`false`）或`docBase`不在`appBase`中时，才可以设置`path`属性。在本例中，`docBase`不在`appBase`中，因此`path`属性可以设置；
 - `reloadable`属性的用法与自动部署时相同。

## 5 核心组件的关联

### 5.1 整体关系

　　核心组件之间的整体关系，在上一部分有所介绍，这里总结一下：

　　Server 元素在最顶层，代表整个 Tomcat 容器；一个 Server 元素中可以有一个或多个 Service 元素。

　　Service 在 Connector 和 Engine 外面包了一层，把它们组装在一起，对外提供服务。一个 Service 可以包含多个 Connector，但是只能包含一个 Engine；Connector 接收请求，Engine 处理请求。

　　Engine、Host 和 Context 都是容器，且 Engine 包含 Host，Host 包含 Context。每个 Host 组件代表 Engine 中的一个虚拟主机；每个 Context 组件代表在特定 Host 上运行的一个 Web 应用。

### 5.2 如何确定请求由谁处理？

　　当请求被发送到 Tomcat 所在的主机时，如何确定最终哪个 Web 应用来处理该请求呢？

**5.2.1 根据协议和端口号选定 Service 和 Engine**

　　Service 中的 Connector 组件可以接收特定端口的请求，因此，当 Tomcat 启动时，Service 组件就会监听特定的端口。在第一部分的例子中，`catalina`这个 Service 监听了 8080 端口（基于 HTTP 协议）和 8009 端口（基于 AJP 协议）。当请求进来时，Tomcat 便可以根据协议和端口号选定处理请求的 Service；Service 一旦选定，Engine 也就确定。

　　通过在 Server 中配置多个 Service，可以实现通过不同的端口号来访问同一台机器上部署的不同应用。

**5.2.2 根据域名或 IP 地址选定 Host**

　　Service 确定后，Tomcat 在 Service 中寻找名称与域名/IP 地址匹配的 Host 处理该请求。如果没有找到，则使用 Engine 中指定的`defaultHost`来处理该请求。在第一部分的例子中，由于只有一个 Host（`name`属性为`localhost`），因此该 Service/Engine 的所有请求都交给该 Host 处理。

**5.2.3 根据 URI 选定 Context/Web 应用**

　　这一点在 Context 一节有详细的说明：Tomcat 根据应用的`path`属性与 URI 的匹配程度来选择 Web 应用处理相应请求，这里不再赘述。

**5.2.4 举例**

　　以请求`http://localhost:8080/app1/index.html`为例，首先通过协议和端口号（`http`和`8080`）选定 Service；然后通过主机名（`localhost`）选定 Host；然后通过 URI（`/app1/index.html`）选定 Web 应用。

### 5.3 如何配置多个服务

　　通过在 Server 中配置多个 Service 服务，可以实现通过不同的端口号来访问同一台机器上部署的不同 Web 应用。在`server.xml`中配置多服务的方法非常简单，分为以下几步：

 - 复制`<Service>`元素，放在当前`<Service>`后面；
 - 修改端口号：根据需要监听的端口号修改`<Connector>`元素的`port`属性；必须确保该端口没有被其他进程占用，否则 Tomcat 启动时会报错，而无法通过该端口访问 Web 应用。

以 Win7 为例，可以用如下方法找出某个端口是否被其他进程占用：`netstat -aon|findstr "8081"`发现 8081 端口被 PID 为 2064 的进程占用，`tasklist |findstr "2064"`发现该进程为`FrameworkService.exe`（这是 McAfee 杀毒软件的进程）。

![net](http://img.blog.csdn.net/20170826121742313)

 - 修改 Service 和 Engine 的`name`属性；
 - 修改 Host 的`appBase`属性（如`webapps2`）
 - Web 应用仍然使用自动部署；
 - 将要部署的 Web 应用（WAR 包或应用目录）拷贝到新的`appBase`下。

以第一部分的`server.xml`为例，多个 Service 的配置如下：

```
<?xml version='1.0' encoding='utf-8'?>
<Server port="8005" shutdown="SHUTDOWN">
  <Listener className="org.apache.catalina.startup.VersionLoggerListener" />
  <Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
  <Listener className="org.apache.catalina.core.JasperListener" />
  <Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
  <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
  <Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />

  <GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container" type="org.apache.catalina.UserDatabase" description="User database that can be updated and saved" factory="org.apache.catalina.users.MemoryUserDatabaseFactory" pathname="conf/tomcat-users.xml" />
  </GlobalNamingResources>

  <Service name="Catalina">
    <Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
    <Connector port="8009" protocol="AJP/1.3" redirectPort="8443" />
    <Engine name="Catalina" defaultHost="localhost">
      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  appBase="/opt/project/webapps" unpackWARs="true" autoDeploy="true">
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs" prefix="localhost_access_log." suffix=".txt" pattern="%h %l %u %t &quot;%r&quot; %s %b" />
      </Host>
    </Engine>
  </Service>

  <Service name="Catalina2">
    <Connector port="8084" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
    <Connector port="8010" protocol="AJP/1.3" redirectPort="8443" />
    <Engine name="Catalina2" defaultHost="localhost">
      <Realm className="org.apache.catalina.realm.LockOutRealm">
        <Realm className="org.apache.catalina.realm.UserDatabaseRealm"
               resourceName="UserDatabase"/>
      </Realm>

      <Host name="localhost"  appBase="/opt/project/webapps2" unpackWARs="true" autoDeploy="true">
        <Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs" prefix="localhost_access_log." suffix=".txt" pattern="%h %l %u %t &quot;%r&quot; %s %b" />
      </Host>
    </Engine>
  </Service>
</Server>
```

再将原`webapps`下的`docs`目录拷贝到`webapps2`中，则通过如下两个接口都可以访问`docs`应用：

 - `http://localhost:8080/docs/`
 - `http://localhost:8084/docs/`

## 6 其他组件

　　除核心组件外，`server.xml`中还可以配置很多其他组件。下面只介绍第一部分例子中出现的组件，如果要了解更多内容，可以查看 [Tomcat 官方文档](http://tomcat.apache.org/tomcat-8.0-doc/config/index.html)。

### 6.1 Listener

```
<Listener className="org.apache.catalina.startup.VersionLoggerListener" />
<Listener className="org.apache.catalina.core.AprLifecycleListener" SSLEngine="on" />
<Listener className="org.apache.catalina.core.JasperListener" />
<Listener className="org.apache.catalina.core.JreMemoryLeakPreventionListener" />
<Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" />
<Listener className="org.apache.catalina.core.ThreadLocalLeakPreventionListener" />
```

　　Listener（即监听器）定义的组件，可以在特定事件发生时执行特定的操作；被监听的事件通常是 Tomcat 的启动和停止。监听器可以在 Server、Engine、Host 或 Context 中，本例中的监听器都是在 Server 中。实际上，本例中定义的 6 个监听器，都只能存在于 Server 组件中。监听器不允许内嵌其他组件。

　　监听器需要配置的最重要的属性是`className`，该属性规定了监听器的具体实现类，该类必须实现了`org.apache.catalina.LifecycleListener`接口。下面依次介绍例子中配置的监听器：

 - `VersionLoggerListener`：当 Tomcat 启动时，该监听器记录 Tomcat、Java 和操作系统的信息。该监听器必须是配置的第一个监听器。
 - `AprLifecycleListener`：Tomcat 启动时，检查 APR 库，如果存在则加载。APR，即 Apache Portable Runtime，是 Apache 可移植运行库，可以实现高可扩展性、高性能，以及与本地服务器技术更好的集成。
 - `JasperListener`：在 Web 应用启动之前初始化 Jasper，Jasper 是 JSP 引擎，把 JVM 不认识的 JSP 文件解析成 Java 文件，然后编译成`class`文件供 JVM 使用。
 - `JreMemoryLeakPreventionListener`：与类加载器导致的内存泄露有关。
 - `GlobalResourcesLifecycleListener`：通过该监听器，初始化`<GlobalNamingResources>`标签中定义的全局 JNDI 资源；如果没有该监听器，任何全局资源都不能使用。`<GlobalNamingResources>`将在后文介绍。
 - `ThreadLocalLeakPreventionListener`：当 Web 应用因`thread-local`导致的内存泄露而要停止时，该监听器会触发线程池中线程的更新。当线程执行完任务被收回线程池时，活跃线程会一个一个的更新。只有当 Web 应用（即 Context 元素）的`renewThreadsWhenStoppingContext`属性设置为`true`时，该监听器才有效。

### 6.2 GlobalNamingResources 与 Realm

　　第一部分的例子中，Engine 组件下定义了 Realm 组件：

```
<Realm className="org.apache.catalina.realm.LockOutRealm">
	<Realm className="org.apache.catalina.realm.UserDatabaseRealm" resourceName="UserDatabase"/>
</Realm>
```

Realm，可以把它理解成“域”；Realm 提供了一种用户密码与 Web 应用的映射关系，从而达到角色安全管理的作用。在本例中，Realm 的配置使用`name`为 UserDatabase 的资源实现。而该资源在 Server 元素中使用 GlobalNamingResources 配置：

```
<GlobalNamingResources>
    <Resource name="UserDatabase" auth="Container" type="org.apache.catalina.UserDatabase" description="User database that can be updated and saved" factory="org.apache.catalina.users.MemoryUserDatabaseFactory" pathname="conf/tomcat-users.xml" />
</GlobalNamingResources>
```

GlobalNamingResources 元素定义了全局资源，通过配置可以看出，该配置是通过读取`$TOMCAT_HOME/conf/tomcat-users.xml`实现的。

### 6.3 Valve

　　在第一部分的例子中，Host 元素内定义了 Valve 组件：

```
<Valve className="org.apache.catalina.valves.AccessLogValve" directory="logs" prefix="localhost_access_log." suffix=".txt" pattern="%h %l %u %t &quot;%r&quot; %s %b" />
```

单词 Valve 的意思是“阀门”，在 Tomcat 中代表了请求处理流水线上的一个组件；Valve 可以与 Tomcat 的容器（Engine、Host 或 Context）关联。不同的 Valve 有不同的特性，下面介绍一下本例中出现的 AccessLogValve。

　　AccessLogValve 的作用是通过日志记录其所在的容器中处理的所有请求，在本例中，Valve 放在 Host 下，便可以记录该 Host 处理的所有请求。AccessLogValve 记录的日志就是访问日志，每天的请求会写到一个日志文件里。AccessLogValve 可以与 Engine、Host 或 Context 关联；在本例中，只有一个 Engine，Engine 下只有一个 Host，Host 下只有一个 Context，因此 AccessLogValve 放在三个容器下的作用其实是类似的。本例的 AccessLogValve 属性的配置，使用的是默认的配置；下面介绍 AccessLogValve 中各个属性的作用：

 - `className`：规定了 Valve 的类型，是最重要的属性；本例中，通过该属性规定了这是一个 AccessLogValve。
 - `directory`：指定日志存储的位置，本例中，日志存储在`$TOMCAT_HOME/logs`目录下。
 - `prefix`：指定了日志文件的前缀。
 - `suffix`：指定了日志文件的后缀。通过`directory`、`prefix`和`suffix`的配置，在`$TOMCAT_HOME/logs`目录下，可以看到如下所示的日志文件。

![LOG](http://img.blog.csdn.net/20170826131329493)

 - `pattern`：指定记录日志的格式，本例中各项的含义如下：
  - `%h`：远程主机名或 IP 地址；如果有 Nginx 等反向代理服务器进行请求分发，该主机名/IP 地址代表的是 Nginx，否则代表的是客户端。后面远程的含义与之类似，不再解释。
  - `%l`：远程逻辑用户名，一律是`-`，可以忽略。
  - `%u`：授权的远程用户名，如果没有，则是`-`。
  - `%t`：访问的时间。
  - `%r`：请求的第一行，即请求方法（Get/Post 等）、URI 及协议。
  - `%s`：响应状态，`200`、`404`等等。
  - `%b`：响应的数据量，不包括请求头，如果为`0`，则是`-`。

例如，下面是访问日志中的一条记录

![record](http://img.blog.csdn.net/20170826131735339)

`pattern`的配置中，除了上述各项，还有一个非常常用的选项是`%D`，含义是请求处理的时间（单位是毫秒），对于统计分析请求的处理速度帮助很大。

　　开发人员可以充分利用访问日志，来分析问题、优化应用。例如，分析访问日志中各个接口被访问的比例，不仅可以为需求和运营人员提供数据支持，还可以使自己的优化有的放矢；分析访问日志中各个请求的响应状态码，可以知道服务器请求的成功率，并找出有问题的请求；分析访问日志中各个请求的响应时间，可以找出慢请求，并根据需要进行响应时间的优化。

## 7 参考文献

 1. [Tomcat 官方文档](http://tomcat.apache.org/tomcat-8.0-doc/config/index.html) 
 2. [Tomcat 6 Realm域管理](http://www.cnblogs.com/xing901022/p/4552843.html)
 3. [Tomcat 6 使用 Jasper 引擎解析 JSP](http://www.cnblogs.com/xing901022/p/4592159.html)
 4. [Tomcat Port 8009 与 AJP13 协议](http://blog.163.com/cmdbat@126/blog/static/170292123201311301419411/)

----------

**转载声明**：本文转自博客园「编程迷思」，[详解Tomcat 配置文件server.xml](http://www.cnblogs.com/kismetv/p/7228274.html)。

