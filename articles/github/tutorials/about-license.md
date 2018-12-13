# 详述 GitHub 中声明 LICENSE 的方法

当我们在 GitHub 浏览一些开源项目时，我们经常会看到这样的标志：

![alibab-dubbo](https://github.com/guobinhit/cg-blog/blob/master/images/github/about-license/alibab-dubbo.png)

如上图所示，`Apache-2.0`，我们可以将其称之为「开源许可证」，那么到底开源许可证是什么呢？

 - **开源许可证即授权条款**。开源软件并非完全没有限制，最基本的限制就是开源软件强迫任何使用和修改该软件的人承认发起人的著作权和所有参与人的贡献。任何人拥有可以自由复制、修改、使用这些源代码的权利，不得设置针对任何人或团体领域的限制；不得限制开源软件的商业使用等。而许可证就是这样一个保证这些限制的法律文件。

常见的开源许可证包括：

 - `Apache License 2.0`
 - `GNU General Public License v3.0`
 - `MIT License`

开源许可证种类很多，以上三个许可证是比较常用的。至于 GitHub 都允许什么类型的许可证，以博主的项目`cg-favorite-list`为例：

![cg-favorite-list](https://github.com/guobinhit/cg-blog/blob/master/images/github/about-license/cg-favorite-list.png)

如上图所示，在项目首页，点击`Create new file`，创建名为`LICENSE`文件：

![choose-a-license](https://github.com/guobinhit/cg-blog/blob/master/images/github/about-license/choose-a-license.png)

实际上，当我们键入`LICENSE`文件名的时候，GitHub 就已经自动提示`Choose a license template`选项啦，点击进入：

![mit-license](https://github.com/guobinhit/cg-blog/blob/master/images/github/about-license/mit-license.png)

如上图所示，最左侧展示了 GitHub 可以选择的开源许可证名称，以`MIT License`为例，点击之后，中间部分显示具体开源许可证的内容。在此处，我们可以自由选择自己想要的许可证，然后点击`Review and submit`：

![commit-license](https://github.com/guobinhit/cg-blog/blob/master/images/github/about-license/commit-license.png)
![commit-license-2](https://github.com/guobinhit/cg-blog/blob/master/images/github/about-license/commit-license-2.png)

 - **标注 1**：`Commit directly to the master branch.`
 - **标注 2**：`Create a new branch for this commit and start a pull request.`

如上图所示，在这里，我们有两个选择。如果我们选择 **标注 1** 所示的内容，则直接将此许可证提交到`master`分支；如果我们选择 **标注 2** 所示的内容，则是新建立一个分支，然后我们可以提`PR`到`master`，再进行合并。在此，我们选择  **标注 1** 所示的内容，直接将`MIT License`提交到`master`分支：

![set-mit-license](https://github.com/guobinhit/cg-blog/blob/master/images/github/about-license/set-mit-license.png)

如上图所示，我们已经为`cg-favorite-list`项目创建了一个开源许可证。那么，你还在等什么？赶紧为你的项目创建开源许可证吧！

----------
———— ☆☆☆ —— [返回 -> 史上最简单的 GitHub 教程 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/github/README.md) —— ☆☆☆ ————




