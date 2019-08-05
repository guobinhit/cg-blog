# 详述 GitHub 如何将代码从原分支合并到 fork 分支


在使用 GitHub 的过程中，我们可能会遇到这样的问题，即：

- 如何将原分支的代码合并到`fork`的分支？

这个问题其实很常见。当我们`fork`别人代码的时候，实际上是对原项目当时状态以及进度进行了一个快照，其随后发生的改变，并不会自动同步到我们的`fork`分支！但是为了保证我们`fork`的分支状态与原分支同步，这就需要我们主动将原分支的代码合并到我们`fork`的分支了。现在，以博主`fork`的`akka`项目为例，就让我们一起看看，将原分支代码合并到`fork`分支的具体操作步骤：

![guobinhit-akka](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/guobinhit-akka.png)

- **标注 1**：`New pull request`，新建拉请求按钮；
- **标注 2**： 显示`fork`分支与原分支相差的提交次数。

如上图所示，**标注 2** 显示了我们已经向`fork`的分支进行了 6 次提交以及在我们`fork`原分支或者上一次合并之后，原分支已经进行了 160 次提交。为了原分支的代码，点击 **标注 1** 所示的`New pull request`按钮。

![comparing-changes](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/comparing-changes.png)

如上图所示，默认是从我们`fork`的分支向原分支合并，**标注 1** 左边的箭头表示合并的方向，点击 **标注 1** 所示的位置，选择 **标注 2** 所示的`akka/akka`，也就是原分支。

![compare-across-forks](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/compare-across-forks.png)

点击原分支之后，会自动跳转到如上界面，点击`compare across forks`：

![akka-to-fork-akka](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/akka-to-fork-akka.png)

点击`compare across forks`之后，会再次显示出两个分支，点击 **标注 1** 所示的位置，选择 **标注 2** 所示的`guobinhit/akka`，也就是我们`fork`的分支。

![master-changes-log](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/master-changes-log.png)

如上图所示，显示出了原分支的提交记录，点击`Create pull request`按钮：

![merge-log](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/merge-log.png)

- **标注 1**：显示分支合并方向；
- **标注 2**：合并记录标题，必填项；
- **标注 3**：合并记录信息，选填项；
- **标注 4**： `Create pull request`，创建拉请求按钮。

如上图所示，填写完 **标注 2** 和 **标注 3** 所需的内容之后，点击 **标注 4** 所示的`Create pull request`按钮：

![pull-requests](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/pull-requests.png)

如上图所示，我们成功创建了一个`PR`，其中醒目的绿色`Open`标识，表示有待处理的拉请求。继续向下滑动页面，可以按时间顺序查阅原分支的提交记录，当页面滑动至底部的时候，会出现一个`Merge pull request`按钮：

![merge-pull-request](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/merge-pull-request.png)

如上图所示，点击`Merge pull request`按钮：

![confirm-merge](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/confirm-merge.png)

如上图所示，点击`Merge pull request`按钮之后，继续点击`Confrim merge`按钮：

![merged](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/merged.png)

如上图所示，合并完成！特别地，当合并操作完成之后，先前绿色的`Open`标识，变为紫色的`Merged`标识。

![merge-over](https://github.com/guobinhit/cg-blog/blob/master/images/github/merge-from-master/merge-over.png)

最后，回到项目主页面，如上图所示，其展示了我们刚刚完成的合并操作记录。



----------
———— ☆☆☆ —— [返回 -> 史上最简单的 GitHub 教程 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/github/README.md) —— ☆☆☆ ————
