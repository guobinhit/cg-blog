# 解决 axios 提交时间类型参数遇到的时区自动转换问题

## 问题描述

在使用`axios`向后端异步发送时间类型（`date`）数据的时候，遇到了时间参数自动转换时区的问题。

![console-log](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/axios-post-date-error/console-log.png)

如上图所示，通过时间组件选定时间之后，打印出了时间。

![inspect-network](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/axios-post-date-error/inspect-network.png)

但是，在数据向后端传输的时候，通过 Chrome 浏览器的`Inspect`功能，查看`Network`，发现时间参数被自动修改了，我们选定的时间是`2019-07-12 00:00:00`，在传输的时候却被修改为`2019-07-11 16:00:00`，导致时间传到后端的参数值与我们期望的参数值不一致，两者相差 8 个小时，也就是从东 8 区（中国北京）的时间自动转换到 0 时区（格林威治）的时间。前端与后端交互的代码，如下所示：

```js
handleSelectCondition(startDate, endDate) {
    this.axios
        .post('/notify/history/select/byCondition', {
            startDate: startDate,
            endDate: endDate
        })
        .then(response => {
            console.log('response: ' + response)
        })
        .catch(error => console.warn(error))
        .finally(() => (this.loading = false))
}
```

## 解决方法
为了解决时区自动转换的问题，我们使用`moment`组件，在传输参数之前，先对参数进行格式化。如果我们还没有安装`moment`组件，则需要先安装`moment`组件，其命令为：

```npm
npm install --save moment
```

执行成功后，其会自动在`package.json`和`package-lock.json`这两个文件中添加对`moment`组件的依赖，类似：

```js
<!-- package.json -->
"dependencies": {
    "moment": "^2.24.0"
}

<!-- package-lock.json -->
"moment": {
      "version": "2.24.0",
      "resolved": "https://registry.npmjs.org/moment/-/moment-2.24.0.tgz",
      "integrity": "sha512-bV7f+6l2QigeB*SM/6y87A8e7*/34/2ky5Vw4B9*dQg=="
}
```

安装完成`moment`组件，修改前端与后端交互的代码：

```js
handleSelectCondition(startDate, endDate) {

	const moment = require('moment')

    const startDateStr = moment(startDate).format('YYYY-MM-DD HH:mm:ss')
    const endDateStr = moment(endDate).format('YYYY-MM-DD HH:mm:ss')
    
    this.axios
        .post('/notify/history/select/byCondition', {
            startDate: startDateStr,
            endDate: endDateStr
        })
        .then(response => {
            console.log('response: ' + response)
        })
        .catch(error => console.warn(error))
        .finally(() => (this.loading = false))
}
```

对比修改前与修改后的代码可见，在通过`axios`向后端传输时间类型的参数前，我们先对其进行了格式化操作：

```js
const moment = require('moment')

const startDateStr = moment(startDate).format('YYYY-MM-DD HH:mm:ss')
const endDateStr = moment(endDate).format('YYYY-MM-DD HH:mm:ss')
```

修改后，进行测试，观察`console.log`的日志输出：

![console-log-2](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/axios-post-date-error/console-log-2.png)

如上图所示，我们选定的两个时间分别为`2019-07-21 00:00:00`和`2019-07-31 00:00:00`：

![inspect-network-2](https://github.com/guobinhit/cg-blog/blob/master/images/solutioncase/part-3-others/axios-post-date-error/inspect-network-2.png)

最后，我们在来观察`Network`里面显示的实际传输的值，显然两者相同。至此，问题解决！


----------
———— ☆☆☆ —— [返回 -> 超实用的「Exception」和「Error」解决案例 <- 目录](https://github.com/guobinhit/cg-blog/blob/master/articles/solutioncase/README.md) —— ☆☆☆ ————
