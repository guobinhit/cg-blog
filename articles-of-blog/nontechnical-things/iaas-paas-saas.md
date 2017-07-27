# 详述 IaaS、PaaS 和 SaaS 的含义及区别

> **博主说**：常识性概念，对 IaaS、PaaS 和 SaaS 还有些迷糊的童鞋，可以通过此文掌握之。

## 正文


越来越多的软件，开始采用云服务。

云服务只是一个统称，可以分成三大类。

![1](http://img.blog.csdn.net/20170725205649476)

 - **IaaS**：基础设施服务，Infrastructure-as-a-service
 - **PaaS**：平台服务，Platform-as-a-service
 - **SaaS**：软件服务，Software-as-a-service

它们有什么区别呢？

IBM 的软件架构师 Albert Barron 曾经使用披萨作为比喻，解释这个问题。David Ng 进一步引申，让它变得更准确易懂。

请设想你是一个餐饮业者，打算做披萨生意。

![2](http://img.blog.csdn.net/20170725205827848)

你可以从头到尾，自己生产披萨，但是这样比较麻烦，需要准备的东西多，因此你决定外包一部分工作，采用他人的服务。你有三个方案：

**方案一：IaaS**

 - 他人提供厨房、炉子、煤气，你使用这些基础设施，来烤你的披萨。

**方案二：PaaS**

 - 除了基础设施，他人还提供披萨饼皮。你只要把自己的配料洒在饼皮上，让他帮你烤出来就行了。也就是说，你要做的就是设计披萨的味道（海鲜披萨或者鸡肉披萨），他人提供平台服务，让你把自己的设计实现。

**方案三：SaaS**

 - 他人直接做好了披萨，不用你的介入，到手的就是一个成品。你要做的就是把它卖出去，最多再包装一下，印上你自己的 Logo。


以上三种方案，可以总结成下面这张图：

![7](http://img.blog.csdn.net/20170725210333149)

从左到右，自己承担的工作量（上图蓝色部分）越来越少，IaaS > PaaS > SaaS。对应软件开发，则是下面这张图：

![8](http://img.blog.csdn.net/20170725210608447)

SaaS 是软件的开发、管理、部署都交给第三方，不需要关心技术问题，可以拿来即用。普通用户接触到的互联网服务，几乎都是 SaaS，下面是一些例子：

 - 客户管理服务 Salesforce
 - 团队协同服务 Google Apps
 - 储存服务 Box
 - 储存服务 Dropbox
 - 社交服务 Facebook / Twitter / Instagram

PaaS 提供软件部署平台（runtime），抽象掉了硬件和操作系统细节，可以无缝地扩展（scaling）。开发者只需要关注自己的业务逻辑，不需要关注底层。下面这些都属于 PaaS：

 - Heroku
 - Google App Engine
 - OpenShift

IaaS 是云服务的最底层，主要提供一些基础资源。它与 PaaS 的区别是，用户需要自己控制底层，实现基础设施的使用逻辑。下面这些都属于 IaaS：

 - Amazon EC2
 - Digital Ocean
 - RackSpace Cloud


----------

**转载声明**：本文转自「阮一峰的网络日志」，[IaaS，PaaS，SaaS 的区别](http://www.ruanyifeng.com/blog/2017/07/iaas-paas-saas.html)。
