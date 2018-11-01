# Git 进阶之「设置别名」

在「[Git 初体验及其常用命令介绍](https://github.com/guobinhit/cg-blog/blob/master/articles/github/GITHUB_README.md)」一文中，我们已经接触了不少常用的命令，包括：

 - `git status`，查询仓库状态；
 - `git init`，初始化仓库；
 - `git add`，添加文件；
 - `git commit`，提交文件；
 - `git log`，查询提交日志；
 - `git branch`，拉分支；
 - `git checkout`，切换分支或者标签；
 - `git merge`，合并分支；
 - `git branch -d & git branch -D`，删除或者强制删除分支；
 - `git tag`，添加标签。

对于上述的 Git 命令，我们使用的频繁特别高，虽然这些单词都不算长，但是我们敲上十次、百次，甚至千次呢？敲一个`git checkout`和敲一个`git co`，哪一个更省时省力呢？显然是后者。这时，就体现了别名的作用啦！也就是`alias`.

还记得我们设置`Author`和`Email`时的操作吗？设置别名也类似，输入：

- `git config --global alias.co check`

如上所示，这样我们就设置`checkout`的别名为`co`啦！也就是说，以后我们直接输入`git co`，就表示`git checkout`啦，特别是对于一些组合操作，例如：

- `git config --global alias.psm 'push origin master'`
- `git config --global alias.plm 'pull origin master'`

显然方便了很多。在这里，各种命令的别名我们可以顺便的起，只要我们能记住就 OK 啦！

此外，我们再了解一个比较`diǎo`的命令。正常情况下，我们输入`git log`查询日志，结果如下图所示：

![1](http://img.blog.csdn.net/20170412165850132)

现在，我们输入命令：

```
git log --graph --pretty=format:'%Cred%h%Creset - %C(yellow)%d%Creset %s %Cgreen(%cr) 
%C(bold blue)<%an>%Creset' --abbrev-commit --date=relative
```

结果如下图所示：

![2](http://img.blog.csdn.net/20170412170833080)

显然，日志看着更加清楚啦！

最后，我们介绍一个查看本机 Git 配置的命令，即`git config -l`：

![config](http://img.blog.csdn.net/20170412172256377)

如上图所示，展示了`color.ui`、`core.repositoryformatversion`和`core.filemode`等配置信息。


----------
———— ☆☆☆ —— [返回 -> 史上最简单的 GitHub 教程 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/github/README.md) —— ☆☆☆ ————




