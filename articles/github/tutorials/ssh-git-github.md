# 利用 SSH 完成 Git 与 GitHub 的绑定

在「[史上最简单的 GitHub 教程](https://github.com/guobinhit/cg-blog/blob/master/articles/github/GITHUB_README.md)」中，我们已经对 GitHub 有了一定的了解，包括创建仓库、拉分支，或者通过`Clone or download`克隆或者下载代码；我们也下载并安装了 Git，也了解了其常用的命令。But，无论是 GitHub，还是 Git，我们都是单独或者说是独立操作的，并没有将两者绑定啊！也就是说，我们现在只能通过 GitHub 下载代码，并不能通过 Git 向 GitHub 提交代码。

因此，在本篇博文中，我们就一起完成 Git 和 GitHub 的绑定，体验通过 Git 向 GitHub 提交代码的能力。不过在这之前，我们需要先了解 SSh（安全外壳协议），因为在 GitHub 上，一般都是通过 SSH 来授权的，而且大多数 Git 服务器也会选择使用 SSH 公钥来进行授权，所以想要向 GitHub 提交代码，首先就得在 GitHub 上添加 `SSH key`配置。在这里，如果大家对 SSH 还不太了解，那么建议先阅读博主之前写的文章「[详述 SSH 的原理及其应用](https://github.com/guobinhit/cg-blog/blob/master/articles/others/detail-ssh.md) 」，从而对 SSH 有一个大致的了解。

> 第 1 步：生成 `SSH key`

我们要想生成`SSH key`，首先就得先安装 SSH，对于 Linux 和 Mac 系统，其默认是安装 SSH 的，而对于 Windows 系统，其默认是不安装 SSH 的，不过由于我们安装了 Git Bash，其也应该自带了 SSH.  可以通过在 Git Bash 中输入`ssh`命令，查看本机是否安装 SSH：

![1](http://img.blog.csdn.net/20170404131908500)

如上图所示，此结果表示我们已经安装 SSH 啦！接下来，输入`ssh-keygen -t rsa`命令，表示我们指定 RSA 算法生成密钥，然后敲三次回车键，期间不需要输入密码，之后就就会生成两个文件，分别为`id_rsa`和`id_rsa.pub`，即密钥`id_rsa`和公钥`id_rsa.pub`. 对于这两个文件，其都为隐藏文件，默认生成在以下目录：

 - Linux 系统：`~/.ssh`
 - Mac 系统：`~/.ssh`
 - Windows 系统：`C:\Documents and Settings\username\\.ssh`
 - Windows 10 ThinkPad：`C:\Users\think\.ssh`

密钥和公钥生成之后，我们要做的事情就是把公钥`id_rsa.pub`的内容添加到 GitHub，这样我们本地的密钥`id_rsa`和 GitHub 上的公钥`id_rsa.pub`才可以进行匹配，授权成功后，就可以向 GitHub 提交代码啦！

> 第 2 步：添加 `SSH key`

![2](http://img.blog.csdn.net/20170404134608330)

如上图所示，进入我们的 GitHub 主页，先点击右上角所示的倒三角`▽`图标，然后再点击`Settins`，进行设置页面；点击我们的头像亦可直接进入设置页面：

![3](http://img.blog.csdn.net/20170404135026832)

如上图所示，进入`Settings`页面后，再点击`SSH and GPG Keys`进入此子界面，然后点击`New SSH key`按钮：

![4](http://img.blog.csdn.net/20170404135835070)

如上图所示，我们只需要将公钥`id_rsa.pub`的内容粘贴到`Key`处的位置（`Titles`的内容不填写也没事），然后点击`Add SSH key` 即可。

> 第 3 步：验证绑定是否成功

在我们添加完`SSH key`之后，也没有明确的通知告诉我们绑定成功啊！不过我们可以通过在 Git Bash 中输入`ssh -T git@github.com`进行测试：

![5](http://img.blog.csdn.net/20170404141307339)

如上图所示，此结果即为Git 与 GitHub 绑定成功的标志。


----------
———— ☆☆☆ —— [返回 -> 史上最简单的 GitHub 教程 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/github/GITHUB_README.md) —— ☆☆☆ ————




