# Git 初体验及其常用命令介绍（下）

在前一篇博文「[Git 初体验及其常用命令介绍（上）](https://github.com/guobinhit/cg-blog/blob/master/articles/github/tutorials/experence-git-one.md)」中，我们已经了解了 Git 的一些常用命令了，包括`status`、`init`、`add`、`commit`和`log`等，下面我们接着上一篇博文的内容，继续介绍 Git 的常用命令。

**第 6 个命令：`git branch`**

在命令行窗口的光标处，输入`git branch`命令，查看 Git 仓库的分支情况：

![1](http://img.blog.csdn.net/20170403170211777)

如上图所示，显示了仓库`demo`中的分支情况，现在仅有一个`master`分支，其中`master`分支前的`*`号表示“**当前所在的分支**”，例如`* master`就意味着我们所在的位置为`demo`仓库的主分支。输入命令`git branch a`，再输入命令`git branch`，结果如下图所示：

![2](http://img.blog.csdn.net/20170403170955742)

如上图所示，我们创建了一个名为`a`的分支，并且当前的位置仍然为主分支。

**第 7 个命令：`git checkout`**

在命令行窗口的光标处，输入`git checkout a`命令，切换到`a`分支：

![3](http://img.blog.csdn.net/20170403171302293)

如上图所示，我们已经切换到`a`分支啦！也可以通过命令`git branch`查看分支情况：

![4](http://img.blog.csdn.net/20170403171630900)

在这里，我们还有一个更简单的方法来查看当前的分支，即通过观察上图中用红色框圈起来的部分。此外，我们也可以在创建分支的同时，直接切换到新分支，命令为`git checkout -b`，例如输入`git checkout -b b`命令：

![5](http://img.blog.csdn.net/20170403172440371)

如上图所示，我们在`a`分支下创建`b`分支（`b`为`a`的分支），并直接切换到`b`分支。

**第 8 个命令：`git merge`**

切换到`master`分支，然后输入`git merge a`命令，将`a`分支合并到`master`分支：

![6](http://img.blog.csdn.net/20170403173100208)

如上图所示，我们已经将`a`分支合并到主分支啦！此外，在这里需要注意一点，那就是：**在合并分支的时候，要考虑到两个分支是否有冲突，如果有冲突，则不能直接合并，需要先解决冲突；反之，则可以直接合并**。

**第 9 个命令：`git branch -d` & `git branch -D` **

在命令行窗口的光标处，输入`git branch -d a`命令，删除`a`分支：

![7](http://img.blog.csdn.net/20170403173835177)

如上图所示，我们已经将分支`a`删除啦！不过有的时候，通过`git branch -d`命令可以出现删除不了现象，例如分支`a`的代码没有合并到主分支等，这时如果我们一定要删除该分支，那么我们可以通过命令`git branch -D`进行强制删除。

**第 10 个命令：`git tag` **

在命令行窗口的光标处，输入`git tag v1.0`命令，为当前分支添加标签：

![8](http://img.blog.csdn.net/20170403175319193)

如上图所示，我们为当前所在的`a`分支添加了一个`v1.0`标签。通过命令`git tag`即可查看标签记录：

![9](http://img.blog.csdn.net/20170403175530508)

如上图所示，显示了我们添加标签的记录。通过命令`git checkout v1.0`即可切换到该标签下的代码状态：

![10](http://img.blog.csdn.net/20170403180132750)

如上图所示，我们已经成功切换到`a`分支的`v1.0`标签啦！

通过「[Git 初体验及其常用命令介绍](https://github.com/guobinhit/cg-blog/blob/master/articles/github/GITHUB_README.md)」两篇博文的内容，我们已经了解了一些 Git 的常用命令啦，但还有很多命令我们没有进行演示，例如`clone`、`rm`、`grep`、`pull`和`push`等，Git 的魅力也并不止于此，还有更多的精彩等待大家探索。

此外，对于前一篇博文中遗留的问题，即“提交内容”中的`Author`和`Email`，可以用如下命令进行设置：

```
git config --global user.name "名字"
git config --global user.email "邮箱"
```

其中，`global`表示设置为全局可用，如果想设置局部可用，删除`global`即可。


----------
———— ☆☆☆ —— [返回 -> 史上最简单的 GitHub 教程 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/github/GITHUB_README.md) —— ☆☆☆ ————
