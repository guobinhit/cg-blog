# Hello World · GitHub 指南

简介
--

Hello World 项目是计算机编程历史中悠久的传统。在我们学习新知识的时候，她也是一个简单的练习。现在，就让我们一起了解 GitHub 吧！

你将学会，如何：

 - 创建和使用仓库；
 - 启用和管理一个新的分支；
 - 修改一个文件并将其提交到 GitHub；
 - 打开并合并一个 Pull 请求。

GitHub 是什么？
-----------

GitHub 是一个用于版本控制和协作的代码托管平台，她都能够让你和任何地方的其他工作者一起做项目。

本教程将告诉你 GitHub 的主要内容，包括仓库、分支、提交代码和 Pull 请求。在这里，你将会用一个流行的方式创建和检查代码、创建你自己的 Hello World 仓库和学习 GitHub 的 Pull 请求工作流。

**不需要编程**：


为了完成这个教程，你需要一个 GitHub.com 账号并且联网，而不需要知道如何编程、使用命令行或者安装 Git（GitHub 就基于这个版本控制软件）。

> **提示**：在单独的浏览器窗口（或选项卡）中打开此指南，这样你就可以边看边实践。

Step 1. 创建仓库
-----------
通常，一个仓库用于构建一个项目，仓库可以包含你项目所需要的任何东西，例如文件夹、文件、图片、视频、电子表格和数据集等。我们建议仓库中包含了一个`README`，或者一个描述你项目信息的文件。在你创建新仓库的时候，GitHub 可以很容易的将它添加进来。GitHub 也提供了其他常见的选项，例如*许可证文件*。

你的`Hello World`库可以作为一个存储你的想法、资源，甚至与他人共享和讨论事情的地方。

**创建新的仓库**：

 - 在右上角，你的头像旁边，点击`+`，然后选择`New repository`；
 - 将你的仓库命名为`Hello World`；
 - 写一个简短的描述；
 - 选择`Initialize this repository with a README`.

![create-repository](https://github.com/guobinhit/cg-blog/blob/master/images/github/hello-world-github-guide/create-repository.png)

点击 `Create repository`.

Step 2. 创建分支
------------

分支是某一时刻对同一个仓库（我感觉说是项目更加适合）在不同版本上进入工作的方法。在默认情况下，你的仓库有一个名为`master`的分支，它被公认为主分支。我们使用分支进行工作，然后再将其提交到`master`上。

当你从`master`中创建一个分支时，也可以说，你正在制作一个副本，或者快照，就像在那个时间点的`master`一样。当你在你的分支上工作的时候，如果其他人对你的`master`分支进行了修改，你可以`Pull`这些更新。

下图展示了：

 - `master`分支；
 - 一个名为`feature`的新分支（因为我们在这个分支上做“特别的工作”）；
 - `feature`分支合并到`master`分支的过程。

![create-branch](https://github.com/guobinhit/cg-blog/blob/master/images/github/hello-world-github-guide/create-branch.png)

你保存过不同版本的文件吗？像：

 - `story.txt`
 - `story-joe-edit.txt`
 - `story-joe-edit-reviewed.txt`

在 GitHub 仓库中，分支完成了类似的目标。

在 GitHub 上，我们开发人员、作家、设计师使用从`master`分支创建的其他分支修改 bug 以及完成特定的工作。当修改完成的时候，我们就可以将其合并到`master`分支啦！

**创建新的分支**：

 - 进入你的`hello-world`仓库；
 - 单击顶部名为`branch: master`的文件列表；
 - 在新分支文本框中键入分支名称`readme-edits`；
 - 选择蓝色的`Create branch`框或者敲键盘上的`Enter`键。

![readme-edits](https://github.com/guobinhit/cg-blog/blob/master/images/github/hello-world-github-guide/readme-edits.png)

现在，你就有两个分支了，分别为`master`和`readme-edits`，两者的内容看起来完全一起，但是很快就不一样啦！接下来，我们就在新分支中添加一些改变。

Step 3. 编辑和提交修改的内容
---------------
好极了！现在，你的代码视图中已经有`readme-edits`分支了，她是`master`分支的一个副本。下面，我们对她做一些编辑。

在 GitHub 上，保存修改被称之为`commit`，即提交。每次提交都有一个相关的提交消息，用来说明为什么进行特定的修改。提交消息保存了你修改的历史，因此其他贡献者能够通过“提交信息”了解你做了什么修改和为什么这么做。

 **编辑和提交修改内容**：

 - 点击`readme.md`文件；
 - 单击文件视图右上角的铅笔图标进行编辑；
 - 在编辑器中，写一点关于你自己的东西；
 - 写一个提交消息，描述你的修改；
 - 单击`Commit changes`按钮。

![hello-world](https://github.com/guobinhit/cg-blog/blob/master/images/github/hello-world-github-guide/hello-world.png)

这些修改仅仅在你的`readme-edits`分支中的`README`中有所体现，因此这个分支就包含与`master`分支不同的内容啦！

Step 4. 提出 Pull 请求
------------------
编辑的很好！现在你已经有了一个与`master`内容不同的分支了，接下来，可以提出 Pull 请求啦！

Pull 请求是 GitHub 协同工作的核心。当你提出一个 Pull 请求的时候，你就已经默认允许其他人审查和合并你贡献的代码到他们的分支啦！Pull 请求展示了两个分支内容上的差异。这些修改，添加和删除的内容将分别用绿色和红色标记出来。

只要你提交过修改的内容，你就可以提出 Pull 请求，同时开启一个讨论，甚至在你的代码完成之前你就可以提出 Pull 请求。

在你 Pull 请求的“请求信息”中，通过使用 GitHub 的`@mention system`，你可以向特定的人或团队反馈问题，无论他们在你身边还是在 10 个时区之外。

你也可以在自己的仓库中提出 Pull 请求，并将其合并。在开发大型项目之前，这是学习 GitHub Flow 非常好的方法。

当你完成你的信息之后，点击`Create pull request`！

Step 5. 合并你的 Pull 请求
--------------------
在这最后一步中，是时候将你在`readme-edits`分支中的修改一起合并到`master`分支即主分支中啦！

 - 单击绿色`Merge pull request`按钮将修改的内容合并到`master`分支；
 - 单击`Confirm merge`按钮；
 - 删除该分支，因为它修改的内容已经合并了，在“紫色”框中点击`Delete branch`按钮。

![pull-request](https://github.com/guobinhit/cg-blog/blob/master/images/github/hello-world-github-guide/pull-request.png)

庆祝吧！
----

通过完成本教程，你已经学会了在 GitHub 上创建项目并提出 Pull 请求啦！

以下是你在本教程中完成的内容，包括：

 - 创建一个开源库；
 - 开始并管理一个新的分支；
 - 修改文件并将这些修改的内容提交到 GitHub；
 - 提出及合并 Pull 请求。

在你的 GitHub 简介（Profile）上，你可以看到自己的贡献标记。

想要学习更多关于 Pull 请求的知识，我们推荐你阅读「[GitHub Flow Guide](https://github.com/guobinhit/cg-blog/blob/master/articles/github/tutorials/understand-github-flow.md)」，你也可以通过访问 「[GitHub Explore](https://github.com/explore)」了解更多的开源项目。

> **提示**：可以通过「[Guides](https://guides.github.com/)」和「[On-Demand Training](https://services.github.com/on-demand/)」了解更多关于 GitHub 的内容。

----------
**原文链接**：[Hello World · GitHub Guides](https://guides.github.com/activities/hello-world/#branch)

----------
———— ☆☆☆ —— [返回 -> 史上最简单的 GitHub 教程 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/github/README.md) —— ☆☆☆ ————
