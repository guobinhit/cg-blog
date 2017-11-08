# 详述 iTerm2 配色及免密登录 SSH 的方法

> **博主说**：iTerm2 是一个 Mac 版的类似于 Xshell 的终端工具，虽然很多同学说其功能并没有 Xshell 那么强大，但它仍然能够满足我们的大部分需求了。在此文中，我们将详细介绍 iTerm2 的配色方案及免密登录 SSH 的方法。

![1](http://img.blog.csdn.net/20171108094317501)

首先，给出 iTerm2 的相关链接，

- iTerm2 客户端下载地址：[iTerm2 - macOS Terminal Replacement](http://www.iterm2.com/).
- iTerm2 客户端颜色主题：[iTerm Themes - Color Schemes and Themes for iTerm2](http://iterm2colorschemes.com/).

接下来，进入本文的正题。

### Section 1：iTerm2 的配色方法

自定义 iTerm2 的配色，具体步骤如下：

**1. 设置终端和`ls`可配色**

登录 Mac 终端，输入`vim ~/.bash_profile`，即用 vim 文本编辑器打开`bash_profile`文件，然后添加如下内容，

```
#enables colorin the terminal bash shell export
export CLICOLOR=1

#setsup thecolor scheme for list export
export LSCOLORS=gxfxcxdxbxegedabagacad

#sets up theprompt color (currently a green similar to linux terminal)
export PS1='\[\033[01;32m\]\u@\h\[\033[00m\]:\[\033[01;36m\]\w\[\033[00m\]\$'
#enables colorfor iTerm
export TERM=xterm-256color

```

![1](http://img.blog.csdn.net/20171108095725707)

**2. 设置 vim 可配色**

终端输入`vim .vimrc`，设置内容如下，

```
syntax on
set number
set ruler
```
![2](http://img.blog.csdn.net/20171108100154521)

截止到此步骤，我们就会发现，终端及 vim 的颜色已经修改完成了。接下来，配置 iTerm2 颜色主题。

**3. 配置 iTerm2 颜色主题**

先进入「[iTerm2 客户端颜色主题](http://iterm2colorschemes.com/)」下载 iTerm2 主题，

![3](http://img.blog.csdn.net/20171108101400206)

如上图所示，点击红色标记处，下载 iTerm2 主题，然后依次选择`iTerm2 -> Preferences -> Profiles -> Colors -> import`，

![4](http://img.blog.csdn.net/20171108102115711)

点击`import`之后，选择我们想要的主题，由于 iTerm2 提供的主题超过 150 多种，我们就不一一介绍了，在此以 Solarized Dark Higher Contrast 主题为例进行演示，

![5](http://img.blog.csdn.net/20171108102927204)

在 iTerm2 的颜色主题导入成功之后，我们需要手动勾选，使其生效，

![6](http://img.blog.csdn.net/20171108103433351)

在我们勾选`Solarized Dark Higher Contrast`主题之后，其效果如下图所示：

![7](http://img.blog.csdn.net/20171108103714441)

至此， iTerm2 的颜色主题配置完成。

### Section 2：iTerm2 免密登录 SSH 的方法

现在，我们来实现 iTerm2 免密登录 SSH，具体步骤如下：

**1. 创建`expect`脚本**

在 Mac 终端或者 iTerm2 中，输入`vim iterm2login.sh`，创建名为「[iterm2login](https://github.com/guobinhit/cg-blog/blob/master/resources/iTerm2/iterm2login.sh)」的脚本，内容为

```
#!/usr/bin/expect

set timeout 30
spawn ssh -p [lindex $argv 0] [lindex $argv 1]@[lindex $argv 2]
expect {
        "(yes/no)?"
        {send "yes\n";exp_continue}
        "password:"
        {send "[lindex $argv 3]\n"}
}
interact
```

如上面的代码所示，其含有四个参数，分别为

- `[lindex $argv 0]`，表示服务器端口号；
- `[lindex $argv 1]`，表示服务器名称；
- `[lindex $argv 2]`，表示服务器 IP 地址；
- `[lindex $argv 3]` ，表示服务器密码。

在这里，Shell 文件是用`expect`命令书写的脚本，其可以自动和网络进行交互，基本原理就是先解析 SSH 的命令，然后在根据文本内容进行匹配，执行对应的操作，`send`则是模拟人工输入的过程。

**2. 在 iTerm2 中配置新的`Profile`**

在这里，我们依次选择`iTerm2 -> Preferences -> Profiles`，进入如下界面：

![9](http://img.blog.csdn.net/20171108155342252)

- **标注 1**：`+`表示新增`Profile`，`-`表示移除`Profile`；
- **标注 2**：表示我们自定义的`Profile`名称，并显示在 **标注 3** 处；
- **标注 3**：`Profile Name`，显示所有的`Profile`列表；
- **标注 4**：`Send text at start`，发送服务器相关信息至`expect`脚本。

其中，**标注 4** 的内容是非常重要的，其必须按顺序包括：**`expect`脚本的全路径、端口号、服务器名称及服务器密码**。如果 **标注 4** 的内容书写错误或者弄混顺序，则必然导致 SSH 免密登录失败。此外，服务器默认端口为`22`，具体按实际情况而定。在此给出一个 **标注 4** 的内容实例，

- /Users/bin.guo/Documents/RESOURCES/iterm2login.sh 22 guest 10.11.12.13 password

其中，

- `/Users/bin.guo/Documents/RESOURCES/iterm2login.sh`，表示`expect`脚本的全路径；
- `22`，表示服务器端口号；
- `guest`，表示服务器名称；
- `10.11.12.13`，表示服务器地址；
- `password`，表示服务器密码。

![10](http://img.blog.csdn.net/20171108161022719)

如上图所示，通过此选项，即可免密登录服务器。至此，iTerm2 免密登录 SSH 设置完成。


----------


> **参考文献**：
> 
> [1] [ITerm2配色方案](http://www.jianshu.com/p/33deff6b8a63).

> [2] [iterm2如何不用每次输入密码登录ssh](http://www.jianshu.com/p/17e06b3887ae).
