# 出现 curl: (51) SSL: no alternative certificate subject name matches target host name 错误的原因及解决方法


## 问题描述

![curl-ssh-51](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/curl-ssl-51-issue/curl-ssh-51.png)

如上图所示，通过`curl`发起 POST 请求，出现 SSL 51 异常：

> curl: (51) SSL: no alternative certificate subject name matches target host name

通过异常描述，我们知道，该错误为：没有与目标主机名匹配的证书。

## 解决方法
既然该错误为主机名称与证书不匹配，那么解决方案肯定就是要求主机修复证书。

但由于某些原因，我们可能并不能直接干预主机的行为，因此我们可以通过下面的临时解决方案，暂时跳过该问题。

- **方法 1**：添加`-k`请求参数

![solve-curl-ssh-51-k](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/curl-ssl-51-issue/solve-curl-ssh-51-k.png)

- **方法 2**：添加`--insecure`请求参数

![solve-curl-ssh-51-insecure](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/curl-ssl-51-issue/solve-curl-ssh-51-insecure.png)

如上述结果图所示，无论是添加`-k`还是添加`--insecure`请求参数，均可以解决该异常。

但正如新增参数的含义一样，添加参数的作用就是放弃了 HTTPS 的安全检查，因此该方法是治标不治本，慎用。

## 更进一步

最后，对于这个问题，说一下我们遇到的场景：

- 最初的域名是`testC.testB.testA`，属于三级域名；
- 后变更域名为`testD.testC.testB.testA`，属于四级域名。

正常来说，无论是三级域名还是四级域名，通过 HTTP 协议访问都是没有问题的，但是想要使用 HTTPS 协议，则需要购买安全证书，而这个证书是跟域名关联的，例如：

- 我们购买了`*.testB.testA`三级域名的 HTTPS 证书；
- 那我们通过 HTTPS 协议访问`*.*.testB.testA`四级域名是不可以的。

因此，想要真正解决这个问题，就需要我们升级域名的证书了。


----------
———— ☆☆☆ —— [返回 -> 超实用的「Exception」和「Error」解决案例 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/solutioncase/README.md) —— ☆☆☆ ————