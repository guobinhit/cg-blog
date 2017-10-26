# 详述 Mac 系统下安装及卸载 Python 的方法

> **博主说**：对于 Mac 而言，其自带的 Python 2.6 和 Python 2.7 这两个版本已经能够满足我们的大部分需求了，因此除非对某个版本有强烈需求，否则不建议自己安装 Python，因为这是典型的安装容易，删除麻烦。

# 正文

> **安装 Python**

首先，登录 Python 官网，下载所需版本：[Welcome to Python](https://www.python.org/).

![0](http://img.blog.csdn.net/20171016181741228)

如上图所示，我们下载了`python-2.7.14`这个版本，双击打开：

![1](http://img.blog.csdn.net/20171016201729725)

如上图所示，依次进入`Introduction`、`Read Me`和`License`选项，对于这些，感兴趣的同学可以看看，否则的话，狂按`Continue`即可：

![2](http://img.blog.csdn.net/20171016201949269)

但是对于`License`，则会弹出一个“许可协议”提示框，毫无疑问，既然我们想使用 Python，自然要同意其许可协议。因此，依次点击`Agree`和`Continue`：

![3](http://img.blog.csdn.net/20171016202339187)

接下来，选择 Python 安装地址，默认是`Macintosh HD`，我们也可以自定义。然后，直到`Installation`，安装进行中，耐心等待：

![4](http://img.blog.csdn.net/20171016202548376)

如上图所示，至此 Python 安装成功。

> **卸载 Python**

正如开篇所言，Mac 自带的 Python 已经能够满足我们的需要了，因此很多同学在安装完 Python 之后，又想要将其删除，或者称之为卸载。对于删除 Python，我们首先要知道其具体都安装了什么，实际上，在安装 Python 时，其自动生成：

- Python framework，即 Python 框架；
- Python 应用目录；
- 指向 Python 的连接。

对于 Mac 自带的 Python，其框架目录为：

- `System/Library/Frameworks/Python.framework`

而我们安装的 Python，其（默认）框架目录为：

- `/Library/Frameworks/Python.framework`

接下来，我们就分别（在 Mac 终端进行）删除上面所提到的三部分。

**第 1 步，删除框架**：

- `sudo rm -rf /Library/Frameworks/Python.framework/Versions/x.x`

**第 2步，删除应用目录**：

- `sudo rm -rf "/Applications/Python x.x"`

**第 3 步，删除指向 Python 的连接**：
```
cd /usr/local/bin/
ls -l /usr/local/bin | grep '../Library/Frameworks/Python.framework/Versions/x.x' | awk '{print $9}' | tr -d @ | xargs rm
```
至此，我们已经成功删除 Python 的相关文件，其中`x.x`为 Python 的版本号。





