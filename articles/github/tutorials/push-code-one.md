# 通过 Git 将代码提交到 GitHub（上）

1 前言
----

在「[利用 SSH 完成 Git 与 GitHub 的绑定](https://github.com/guobinhit/cg-blog/blob/master/articles/github/tutorials/ssh-git-github.md)」一文中，我们完成了本地 Git 与远程 GitHub 的绑定，这意味着我们已经可以通过 Git 向 GitHub 提交代码啦！但是在进行演示之前，我们需要先了解两个命令，也是我们在将来需要经常用到的两个命令，分别为`push`和`pull`.

- **`push`**：该单词直译过来就是“推”的意思，如果我们本地的代码有了更新，为了保持本地与远程的代码同步，我们就需要把本地的代码推到远程的仓库，代码示例：

```
git push origin master
```


- **`pull`**：该单词直译过来就是“拉”的意思，如果我们远程仓库的代码有了更新，同样为了保持本地与远程的代码同步，我们就需要把远程的代码拉到本地，代码示例：

```
git pull origin master
```
此外，在之前我们讲到过`pull request`，在这里，估计大家就能更好的理解了，它表示：如果我们`fork`了别人的项目（或者说代码），并对其进行了修改，想要把我们的代码合并到原始项目（或者说原始代码）中，我们就需要提交一个`pull request`，让原作者把我们的代码拉到 ta 的项目中，至少对于 ta 来说，我们都是属于远程端的。

一般情况下，我们在`push`操作之前都会先进行`pull`操作，这样不容易造成冲突。


2 提交代码
------

对于向远处仓库（GitHub）提交代码，我们可以细分为两种情况：

- **第一种**：本地没有 Git 仓库，这时我们就可以直接将远程仓库`clone`到本地。通过`clone`命令创建的本地仓库，其本身就是一个 Git 仓库了，不用我们再进行`init`初始化操作啦，而且自动关联远程仓库。我们只需要在这个仓库进行修改或者添加等操作，然后`commit`即可。

接下来，以博主的 GitHub 账号中的`mybatis-tutorial`项目为例，进行演示。首先，进入 GitHub 个人主页：

![01](http://img.blog.csdn.net/20170408131002040?)

如上图所示，点击`mybatis-tutorial`项目：

![02](http://img.blog.csdn.net/20170408131311668)

如上图所示，进入`mybatis-tutorial`项目后，点击`Clone or download`，复制上图所示的地址链接。然后，进入我们准备存储 Git 仓库的目录，例如下面我们新建的`GitRepo`目录， 从此目录进入 Git Bash：

![03](http://img.blog.csdn.net/20170408132004101)

接下来，输入`git clone https://github.com/guobinhit/mybatis-tutorial.git`命令，其中`clone`后面所接的链接为我们刚刚复制的远程仓库的地址：

![04](http://img.blog.csdn.net/20170408132422782)

如上图所示，我们已经把远程的`mybatis-tutorial`仓库`clone`到本地啦！下面，我们看看`clone`到本地的仓库内容与远程仓库的内容，是否完全一致：

![05](http://img.blog.csdn.net/20170408132803379)

如上图所示，显示我们已经把远程仓库`mybatis-tutorial`的内容都`clone`到本地啦！接下来，为了方便演示，我们直接把之前重构的「[史上最简单的 MyBatis 教程](https://github.com/guobinhit/mybatis-tutorial)」里面的`mybatis-demo`项目的代码复制过来：

![06](http://img.blog.csdn.net/20170408133733524)

如上图所示，我们已经把`mybatis-demo`项目里面的主要内容`src`目录和`web`目录复制过来啦！接下来，从此目录进入 Git Bash，然后输入`git status`命令查看仓库状态：

![07](http://img.blog.csdn.net/20170408134133223)

如上图所示，`mybatis-tutorial`已经是一个 Git 仓库了，而且在输入`git status`命令后显示有两个文件未被追踪，也就是我们刚刚复制过来的两个文件没有提交。通过「[Git 初体验及其常用命令介绍](https://github.com/guobinhit/cg-blog/blob/master/articles/github/tutorials/experence-git-one.md)」，我们已经知道了在真正提交代码之前，需要先进行`git add`操作：

![08](http://img.blog.csdn.net/20170408134942529)

如上图所示，我们已经将`src`目录`add`并`commit`到`mybatis-tutorial`仓库啦！接下来，我们将`web`目录提交到仓库，然后输入`git log`命令查看仓库日志：

![09](http://img.blog.csdn.net/20170408135427335)

再输入`git status`命令查看仓库状态：

![10](http://img.blog.csdn.net/20170408135642901)

如上图所示，我们已经将`mybatis-tutorial`仓库里面新添加的两个目录都提交啦！下面，我们将本地仓库的内容`push`到远程仓库，输入`git push origin master`命令：

![11](http://img.blog.csdn.net/20170408140015375)

如上图所示，在第一次向远程仓库提交代码的时候，需要输入账号及密码进行验证，验证成功后，显示如下结果：

![12](http://img.blog.csdn.net/20170408140219940)

然后，刷新 GitHub 中`mybatis-tutorial`仓库：

![13](http://img.blog.csdn.net/20170408140649448)

如上图所示，我们已经将项目（仓库）中新添加的内容提交到了远程仓库。接下来，返回 GitHub 个人主页：

![14](http://img.blog.csdn.net/20170408140937821)

观察上图，我们会发现一个现象，那就是：`mybatis-tutorial`仓库的概要中新增了一个`Java`语言的标记。对于这个仓库语言的标记，其来源有两个，一是在我们创建仓库时就指定语言；二是在我们提交或者新建代码后由 GitHub 自动识别该语言。

- **第二种**：详见「[通过 Git 将代码提交到 GitHub（下）](https://github.com/guobinhit/cg-blog/blob/master/articles/github/tutorials/push-code-two.md)」.



----------
———— ☆☆☆ —— [返回 -> 史上最简单的 GitHub 教程 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/github/GITHUB_README.md) —— ☆☆☆ ————
