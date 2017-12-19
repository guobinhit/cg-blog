# 通过 Git 将代码提交到 GitHub（下）

在「[通过 Git 将代码提交到 GitHub（上](https://github.com/guobinhit/cg-blog/blob/master/articles/github/tutorials/push-code-one.md)」一文中，我们已经介绍了向 GitHub 提交代码时的第一种情况，即：

- **第一种**：本地没有 Git 仓库，这时我们可以直接将远程仓库`clone`到本地。通过`clone`命令创建的本地仓库，其本身就是一个 Git 仓库了，不用我们再进行`init`初始化操作啦，而且自动关联远程仓库。我们只需要在这个仓库进行修改或者添加等操作，然后`commit`即可。

接下来，我们继续介绍向 GitHub 提交代码时可能遇到的第二种情况，即：

- **第二种**：本地有 Git 仓库，并且我们已经进行了多次`commit`操作。

仍然以博主的开源项目为例，不过这次换成`springmvc-tutorial`项目进行演示。首先，建立一个本地仓库，命名为`springmvc-tutorial`：

![1](http://img.blog.csdn.net/20170408150309128)

如上图所示，进入该仓库，进入`init`初始化操作：

![2](http://img.blog.csdn.net/20170408150323635)

然后，输入`git remote add origin https://github.com/guobinhit/springmvc-tutorial.git`命令，关联远程仓库（在此，默认大家都知道如何获取远程仓库的地址），其中`origin`为远程仓库的名字：

![1](http://img.blog.csdn.net/20170409103013402)

输入`git pull origin master`命令，同步远程仓库和本地仓库：

![2](http://img.blog.csdn.net/20170409103234701)

再回到本地`springmvc-tutorial`仓库，看看我们是否已经把远程仓库的内容同步到了本地：

![3](http://img.blog.csdn.net/20170409103609937)

如上图所示，显然我们已经把远程`springmvc-tutorial`仓库里面仅有的`README.md`文件同步到了本地仓库。接下来，在本地仓库新建一个名为`test.txt`的测试文件：

![4](http://img.blog.csdn.net/20170409103948647)

输入`git add`和`git commit`命令，将文件`test.txt`添加并提交到`springmvc-tutorial`仓库：

![3](http://img.blog.csdn.net/20170408151431666)

再输入`git push origin master`命令，将本地仓库修改（或者添加）的内容提交到远程仓库：

![5](http://img.blog.csdn.net/20170409104401489)

如上图所示，我们已经将本地仓库的内容同步到了远程仓库。下面，我们进入远程`springmvc-tutorial`仓库的页面，看看我们的提交结果：

![6](http://img.blog.csdn.net/20170409104756315)

如上图所示，我们已经将「通过 Git 将代码提交到 GitHub」的第二种情况演示完毕。

此外，在本篇博文中，我们将远程仓库命名为`origin`，本地仓库名为`springmvc-tutorial`，其实两者的名字咱们可以随意取，一般来说，我们习惯性将远程仓库命名为`origin`，不过在需要关联多个远程仓库的时候，就需要我们再取别的名字啦！

最后，再强调一遍：**在我们向远程仓库提交代码的时候，一定要先进行`pull`操作，再进行`push`操作，防止本地仓库与远程仓库不同步导致冲突的问题，尤其是第二种提交代码的情况，很容易就出现问题。**


----------
———— ☆☆☆ —— [返回 -> 史上最简单的 GitHub 教程 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/github/GITHUB_README.md) —— ☆☆☆ ————
