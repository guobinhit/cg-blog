# 详述 Cookie 与 Session 的区别

> **博主说**：在本篇文章中，主要介绍了 Cookie 机制和 Session 机制，并着重讲解了两者之间的区别，希望能够帮助大家对 Cookie 和 Session 有一个更为深入的了解。

## 正文


### Cookie 机制


　　**Cookie 是服务器在本地机器上存储的小段文本并随每一个请求发送至同一个服务器**。IETF RFC 2965 HTTP State Management Mechanism 是通用 cookie 规范。网络服务器用 HTTP 头向客户端发送 cookie，在客户终端，浏览器解析这些 cookie 并将它们保存为一个本地文件，它会自动将同一服务器的任何请求缚上这些 cookie.

　　具体来说，cookie 机制采用的是在客户端保持状态的方案。它是在用户端的会话状态的存贮机制，它需要用户打开客户端的cookie支持。cookie的作用就是为了解决HTTP协议无状态的缺陷所作的努力。正统的 cookie 分发是通过扩展 HTTP 协议来实现的，服务器通过在 HTTP 的响应头中加上一行特殊的指示以提示浏览器按照指示生成相应的 cookie。然而纯粹的客户端脚本如 JavaScript 也可以生成 cookie。而 cookie 的使用是由浏览器按照一定的原则在后台自动发送给服务器的。浏览器检查所有存储的 cookie，如果某个 cookie 所声明的作用范围大于等于将要请求的资源所在的位置，则把该 cookie 附在请求资源的 HTTP 请求头上发送给服务器。

　　**Cookie 的内容主要包括：名字，值，过期时间，路径和域**。路径与域一起构成 cookie 的作用范围。若不设置过期时间，则表示这个 cookie 的生命期为浏览器会话期间，关闭浏览器窗口，cookie 就消失。这种生命期为浏览器会话期的 cookie 被称为会话 cookie。会话 cookie 一般不存储在硬盘上而是保存在内存里，当然这种行为并不是规范规定的。若设置了过期时间，浏览器就会把 cookie 保存到硬盘上，关闭后再次打开浏览器，这些 cookie 仍然有效直到超过设定的过期时间。存储在硬盘上的 cookie 可以在不同的浏览器进程间共享，比如两个 IE 窗口。而对于保存在内存里的 cookie，不同的浏览器有不同的处理方式。

　　而 session 机制采用的是一种在服务器端保持状态的解决方案。同时我们也看到，由于采用服务器端保持状态的方案在客户端也需要保存一个标识，所以 session 机制可能需要借助于 cookie 机制来达到保存标识的目的。而 session 提供了方便管理全局变量的方式 。

　　Session 是针对每一个用户的，变量的值保存在服务器上，用一个 session ID 来区分是哪个用户 session 变量，这个值是通过用户的浏览器在访问的时候返回给服务器，当客户禁用 cookie 时，这个值也可能设置为由 get 来返回给服务器。

　　就安全性来说：**当你访问一个使用 session 的站点，同时在自己机子上建立一个 cookie，建议在服务器端的 session 机制更安全些，因为它不会任意读取客户存储的信息**。

### Session 机制


　　**Session 机制是一种服务器端的机制，服务器使用一种类似于散列表的结构（也可能就是使用散列表）来保存信息。**

　　当程序需要为某个客户端的请求创建一个 session 时，服务器首先检查这个客户端的请求里是否已包含了一个 session 标识（称为 session ID），如果已包含则说明以前已经为此客户端创建过 session，服务器就按照 session ID 把这个 session 检索出来使用（检索不到，会新建一个），如果客户端请求不包含 session ID，则为此客户端创建一个 session 并且生成一个与此 session 相关联的 session ID，session ID 的值应该是一个既不会重复，又不容易被找到规律以仿造的字符串，这个 session ID 将被在本次响应中返回给客户端保存。

　　保存这个 session ID 的方式可以采用 cookie，这样在交互过程中浏览器可以自动的按照规则把这个标识发挥给服务器。一般这个 cookie 的名字都是类似于 session ID。但 cookie 可以被人为的禁止，则必须有其他机制以便在 cookie 被禁止时仍然能够把 session ID 传递回服务器。

　　经常被使用的一种技术叫做 URL 重写，就是把 session ID 直接附加在 URL 路径的后面。还有一种技术叫做表单隐藏字段，就是服务器会自动修改表单，添加一个隐藏字段，以便在表单提交时能够把 session ID 传递回服务器。

### Cookie 和 Session 的区别


　　Cookie 与 Session 都能够进行会话跟踪，但是完成的原理不太一样。普通状况下二者均能够满足需求，但有时分不能够运用 Cookie，有时分不能够运用 Session。下面经过比拟阐明两者的特性以及适用的场所：

**1. 存取方式的不同**

　　Cookie 中只能保管 ASCII 字符串，假如需求存取 Unicode 字符或者二进制数据，需求先进行编码。Cookie 中也不能直接存取 Java 对象。若要存储略微复杂的信息，运用 Cookie 是比较艰难的。而 Session 中能够存取任何类型的数据，包括而不限于 String、Integer、List、Map 等。Session 中也能够直接保管 Java Bean 乃至任何 Java 类，对象等，运用起来十分便当。能够把 Session 看做是一个 Java 容器类。

**2. 隐私策略的不同**

　　Cookie 存储在客户端阅读器中，对客户端是可见的，客户端的一些程序可能会窥探、复制以至修正 Cookie 中的内容。而 Session 存储在服务器上，对客户端是透明的，不存在敏感信息泄露的风险。假如选用 Cookie，比较好的方法是，敏感的信息如账号密码等尽量不要写到 Cookie 中。最好是像 Google、Baidu 那样将 Cookie 信息加密，提交到服务器后再进行解密，保证 Cookie 中的信息只要本人能读得懂。而假如选择 Session 就省事多了，反正是放在服务器上，Session 里任何隐私都能够有效的保护。

**3. 有效期上的不同**

　　使用过 Google 的人都晓得，假如登录过 Google，则 Google 的登录信息长期有效。用户不用每次访问都重新登录，Google 会持久地记载该用户的登录信息。要到达这种效果，运用 Cookie 会是比较好的选择。只需要设置 Cookie 的过期时间属性为一个很大很大的数字。由于 Session 依赖于名为 JSESSIONID 的 Cookie，而 Cookie JSESSIONID 的过期时间默许为`–1`，只需关闭了阅读器该 Session 就会失效，因而 Session 不能完成信息永世有效的效果。运用 URL 地址重写也不能完成。而且假如设置 Session 的超时时间过长，服务器累计的 Session 就会越多，越容易招致内存溢出。

**4. 服务器压力的不同**

　　Session 是保管在服务器端的，每个用户都会产生一个 Session。假如并发访问的用户十分多，会产生十分多的 Session，耗费大量的内存。因而像 Google、Baidu、Sina 这样并发访问量极高的网站，是不太可能运用 Session 来追踪客户会话的。而 Cookie 保管在客户端，不占用服务器资源。假如并发使用的用户十分多，Cookie 是很好的选择。关于 Google、Baidu、Sina 来说，Cookie 或许是唯一的选择。

**5. 浏览器支持的不同**

　　Cookie 是需要客户端浏览器支持的。假如客户端禁用了 Cookie，或者不支持 Cookie，则会话跟踪会失效。关于 WAP 上的应用，常规的 Cookie 就派不上用场了。假如客户端浏览器不支持 Cookie，需要运用 Session 以及 URL 地址重写。需要注意的是一切的用到 Session 程序的 URL 都要进行 URL 地址重写，否则 Session 会话跟踪还会失效。关于 WAP 应用来说，Session + URL 地址重写或许是它唯一的选择。假如客户端支持 Cookie，则 Cookie 既能够设为本浏览器窗口以及子窗口内有效（把过期时间设为`–1`），也能够设为一切阅读器窗口内有效（把过期时间设为某个大于`0`的整数）。但 Session 只能在本阅读器窗口以及其子窗口内有效。假如两个浏览器窗口互不相干，它们将运用两个不同的 Session.

**6. 跨域支持上的不同**

　　Cookie 支持跨域名访问，例如将`domain`属性设置为`.biaodianfu.com`，则以`.biaodianfu.com`为后缀的一切域名均能够访问该 Cookie。跨域名 Cookie 如今被普遍用在网络中，例如 Google、Baidu、Sina 等。而 Session 则不会支持跨域名访问。Session 仅在他所在的域名内有效。仅运用 Cookie 或者仅运用 Session 可能完成不了理想的效果。这时应该尝试一下同时运用 Cookie 与 Session。Cookie 与 Session 的搭配运用在实践项目中会完成很多意想不到的效果。


----------

**转载声明**：本文转自网站「lai18」，[Cookie与Session的区别-总结很好的文章](http://www.lai18.com/content/407204.html?from=cancel)。

