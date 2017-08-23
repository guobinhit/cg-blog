# 解读 Maven 安装目录

> **博主说**：Maven 作为一款优秀的构建工具、依赖管理工具和项目管理工具，已经被业界广泛应用，我们可以利用 Maven 对项目进行清理、测试、构建、打包、部署等操作。在此，我们就 Maven 的安装目录，进行解读。

正文
==

从 Apache Maven 官网下载 Maven 的安装包并解压之后，进入安装目录，我们会看到如下内容：

 - `bin`
 - `boot`
 - `conf`
 - `lib`
 - `LICENSE.txt`
 - `NOTICE.txt`
 - `README.txt`

接下来，分别解读以上目录的内容及功能：

**第 1 个：`bin`**

　　该目录包含了`mvn`运行的脚本，分别为`mvn`、`mvn.bat`、`mvnDebug`、`mvnDebug.bat`和`m2.conf`，这些脚本用来配置 Java 命令，准备 CLASSPATH 和相关的 Java 系统属性，然后执行 Java 命令。其中，`mvn`是基于 UNIX 平台的`shell`脚本，`mvn.bat`是基于 Windows 平台的`bat`脚本；同理，`mvnDebug`是基于 UNIX 平台的`shell`脚本，`mvnDebug.bat`是基于 Windows 平台的`bat`脚本。在命令行输入任何一条`mvn`命令时，实际上就是调用这些脚本。而`mvn`和`mvnDebug`的区别就在于后者比前者多了一条`MAVEN_DEBUG_OPTS`配置，其作用就是在运行 Maven 时开启`debug`，以便调试 Maven 本身。此外，`m2.conf`是`classworlds`的配置文件。

**第 2 个：`boot`**

　　该目录只包含一个文件，以`maven 3.0`为例，该文件为`plexus-classworlds-2.2.3.jar`。`plexus-classworlds`是一个类加载器框架，相对于默认的 Java 类加载器，它提供了更丰富的语法以方便配置，Maven 使用该框架加载自己的类库。

**第 3 个：`conf`**

　　该目录包含了要给非常重要的文件`settings.xml`。直接修改该文件，就能再机器上全局地定制 Maven 的行为。一般情况下，我们更偏向于复制该文件至`~/.m2/`目录下（`~`表示用户目录），然后修改该文件，在用户范围定制 Maven 的行为。

**第 4 个：`lib`**

　　该目录包含了所有 Maven 运行时需要的 Java 类库，Maven 本身是分模块开发的，因此用户能看到诸如`maven-core-3.0.jar`和`maven-model-3.0.jar`之类的文件。此外，这里还包含一些 Maven 用到的第三方依赖，如`common-cli-1.2.jar`和`google-collection-1.0.jar`等。对于 Maven 2 来说，该目录只包含一个如`maven-2.2.1-uber.jar`的文件，原本各为独立的 JAR 文件的 Maven 模块和第三方类库都被拆解后重新合并到了这个 JAR 文件中。可以说，`lib`目录就是真正的 Maven。还有一点值得一提的是，用户可以在这个目录中找到 Maven 内置的超级 POM。

**第 5 个：`LICENSE.txt`**

　　该文件记录了 Maven 使用的软件许可证 Apache License Version 2.0。

**第 6 个：`NOTICE.txt`**

　　该文件记录了 Maven 包含的第三方软件。

**第 7 个：`README.txt`**

　　该文件则包含了 Maven 的简要介绍，包括安装需求以及如何安装的简要指令等。

如上所述，至此，咱们就将 Maven 的安装目录里面的内容全部解读完毕啦！


----------

**版权申明**：本文的内容多来自于徐晓斌所著的《Maven实战》。
