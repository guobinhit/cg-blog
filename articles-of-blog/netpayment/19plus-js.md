# 19+ JavaScript 常用的简写技巧

> **博主说**：对于任何基于 JavaScript 的开发人员来说，这绝对是一篇必读的文章，乃提升开发效率之神器也。

## 正文


![js](http://img.blog.csdn.net/20170726195315209)


### 1. 三元操作符

当你想用一行代码来写`if...else`语句的时候，使用三元操作符是非常好的选择，例如：

```
const x = 20;
let answer;
if (x > 10) {
    answer = 'is greater';
} else {
	answer = 'is lesser';
}
```

可以简写为：

```
const answer = x > 10 ? 'is greater' : 'is lesser';
```

也可以嵌套`if`语句：

```
const big = x > 10 ? " greater 10" : x
```

### 2. 简写短路求值

当给一个变量分配另一个值的时候，你可能想确定初值不是`null`，`undefined`或空值。这时，你可以写一个多重条件的`if`语句：

```
if (variable1 !== null || variable1 !== undefined || variable1 !== '') {
     let variable2 = variable1;
}
```
或者可以使用短路求值的方法：

```
const variable2 = variable1  || 'new';
```

### 3. 简写变量声明

在定义函数的时候，你可能需要先声明多个变量，例如：

```
let x;
let y;
let z = 3;
```

这时，你可以使用简写的方式节省很多时间和空间，即同时声明多个变量：

```
let x, y, z=3;
```

### 4. 简写 if 执行条件

这可能微不足道，但值得一提。在你做`if`条件检查的时候，其赋值操作可以省略，例如：

```
if (likeJavaScript === true)
```

可以简写为：

```
if (likeJavaScript)
```

只有当`likeJavaScript`是真值的时候，以上两个语句才可以替换。如果判断假值，例如：

```
let a;
if ( a !== true ) {
	// do something...
}
```

可以简写为：

```
let a;
if ( !a ) {
	// do something...
}
```

### 5. 简写 JavaScript 循环方法

当你想使用纯 JavaScript 而不依赖外库（例如`JQuery`）的时候，这是非常有用的。

```
for (let i = 0; i < allImgs.length; i++)
```

可以简写为：

```
for (let index in allImgs)
```

也可以使用`Array.forEach`：

```
function logArrayElements(element, index, array) {
	console.log("a[" + index + "] = " + element);
}
[2, 5, 9].forEach(logArrayElements);
// logs:
// a[0] = 2
// a[1] = 5
// a[2] = 9
```

### 6. 短路求值


如果想通过判断参数是否为`null`或者`undefined`来分配默认值的话，我们不需要写六行代码，而是可以使用一个短路逻辑运算符，只用一行代码来完成相同的操作。例如：

```
let dbHost;
if (process.env.DB_HOST) {
	dbHost = process.env.DB_HOST;
} else {
	dbHost = 'localhost';
}
```

可以简写为：

```
const dbHost = process.env.DB_HOST || 'localhost';
```

### 7. 十进制指数

当数字的尾部为很多的零时（如`10000000`），咱们可以使用指数（`1e7`）来代替这个数字，例如：

```
for (let i = 0; i < 10000; i++) {}
```

可以简写为：

```
for (let i = 0; i < 1e7; i++) {}

// 下面都是返回 true
1e0 === 1;
1e1 === 10;
1e2 === 100;
1e3 === 1000;
1e4 === 10000;
1e5 === 100000;
```

### 8. 简写对象属性


在 JavaScript 中定义对象很简单，而且`ES6`提供了一个更简单的分配对象属性的方法。如果属性名与`key`值相同，例如：

```
const obj = { x:x, y:y };
```

则可以简写为：

```
const obj = { x, y };
```

### 9. 简写箭头函数

传统函数很容易让人理解和编写，但是当它嵌套在另一个函数中的时候，它就会变得冗长和混乱。例如：

```
function sayHello(name) {
	console.log('Hello', name);
}

setTimeout(function() {
	console.log('Loaded')
}, 2000);

list.forEach(function(item) {
	console.log(item);
});
```

这时，可以简写为：

```
sayHello = name => console.log('Hello', name);

setTimeout(() => console.log('Loaded'), 2000);

list.forEach(item => console.log(item));
```

### 10. 简写隐式返回值

我们经常使用`return`语句来返回函数最终结果，仅有一行声明语句的箭头函数能隐式返回其值（这时函数必须省略`{}`以便省略`return`关键字）。如果想要返回多行语句，则需要使用`()`包围函数体。例如：

```
function calcCircumference(diameter) {
  return Math.PI * diameter
}

var func = function func() {
  return { foo: 1 };
};
```

可以简写为：

```
calcCircumference = diameter => (
  Math.PI * diameter;
)

var func = () => ({ foo: 1 });
```

### 11. 默认参数值

我们经常可以使用`if`语句来为函数中的参数定义默认值。但是在`ES6`中，咱们可以在函数本身声明参数的默认值。例如：

```
function volume(l, w, h) {
	if (w === undefined)
	    w = 3;
    if (h === undefined)
	    h = 4;
	return l * w * h;
}
```

可以简写为：

```
volume = (l, w = 3, h = 4 ) => (l * w * h);

volume(2)   // output: 24
```

### 12. 字符串模板

你是不是厌倦了使用`+`将多个变量转换为字符串？有没有更简单的方法呢？如果你能够使用`ES6`，那么很幸运，你仅需使用反引号并将变量置于`${}`之中即可。例如：

```
const welcome = 'You have logged in as ' + first + ' ' + last + '.'

const db = 'http://' + host + ':' + port + '/' + database;
```
可以简写为：

```
const welcome = `You have logged in as ${first} ${last}`;

const db = `http://${host}:${port}/${database}`;
```

### 13. 简写赋值方法

如果你正在使用任何流行的 Web 框架，那么你很有可能使用数组或以对象本文的形式将数据在组件和 API 之间进行通信。一旦数据对象到达一个组件，你就需要解压它。例如：

```
const observable = require('mobx/observable');
const action = require('mobx/action');
const runInAction = require('mobx/runInAction');

const store = this.props.store;
const form = this.props.form;
const loading = this.props.loading;
const errors = this.props.errors;
const entity = this.props.entity;
```

可以简写为：

```
import { observable, action, runInAction } from 'mobx';

const { store, form, loading, errors, entity } = this.props;
```

也可以分配变量名：

```
// 最后一个变量名为 contact
const { store, form, loading, errors, entity:contact } = this.props;

```

### 14. 简写多行字符串

如果你曾发现自己需要在代码中编写多行字符串，那么这估计就是你编写它们的方法，即在输出的多行字符串间用`+`来拼接：

```
const lorem = 'Lorem ipsum dolor sit amet, consectetur\n\t'
    + 'adipisicing elit, sed do eiusmod tempor incididunt\n\t'
    + 'ut labore et dolore magna aliqua. Ut enim ad minim\n\t'
    + 'veniam, quis nostrud exercitation ullamco laboris\n\t'
    + 'nisi ut aliquip ex ea commodo consequat. Duis aute\n\t'
    + 'irure dolor in reprehenderit in voluptate velit esse.\n\t'
```

但是如果使用反引号，你就可以达到简写的目的：

```
const lorem = `Lorem ipsum dolor sit amet, consectetur
    adipisicing elit, sed do eiusmod tempor incididunt
    ut labore et dolore magna aliqua. Ut enim ad minim
    veniam, quis nostrud exercitation ullamco laboris
    nisi ut aliquip ex ea commodo consequat. Duis aute
    irure dolor in reprehenderit in voluptate velit esse.`
```

### 15. 扩展运算符

在`ES6`中，包括扩展运算符，它可以使你的操作更简单，例如：

```
// joining arrays
const odd = [1, 3, 5];
const nums = [2 ,4 , 6].concat(odd);

// cloning arrays
const arr = [1, 2, 3, 4];
const arr2 = arr.slice()
```

可以简写为：

```
// joining arrays
const odd = [1, 3, 5];
const nums = [2 ,4 , 6, ...odd];
console.log(nums);   // [2, 4, 6, 1, 3, 5]

// cloning arrays
const arr = [1, 2, 3, 4];
const arr2 = [...arr];
```

不像`concat()`函数，你可以使用扩展运算符在一个数组中任意处插入另一个数组，例如：

```
const odd = [1, 3, 5 ];
const nums = [2, ...odd, 4, 6];
```

也可以使用扩展运算符：

```
const { a, b, ...z } = { a: 1, b: 2, c: 3, d: 4 };
console.log(a)   // 1
console.log(b)   // 2
console.log(z)   // { c: 3, d: 4 }
```

### 16. 强制参数

默认情况下，如果不传递值，JavaScript 会将函数参数设置为`undefined`，而其他一些语言则会报出警告或错误。想要执行参数分配，则可以让`if`语句抛出`undefined`的错误，或者使用“强制参数”的方法。例如：

```
function foo(bar) {
	if(bar === undefined) {
	    throw new Error('Missing parameter!');
    }
    return bar;
}
```

可以简写为：

```
mandatory = () => {
    throw new Error('Missing parameter!');
}

foo = (bar = mandatory()) => {
    return bar;
}
```

### 17. Array.find 简写

如果你曾负责编写 JavaScript 中的`find`函数，那么你很有可能使用了`for`循环。在此，介绍`ES6`中一个名为`find()`的数组函数。

```
const pets = [
  { type: 'Dog', name: 'Max'},
  { type: 'Cat', name: 'Karl'},
  { type: 'Dog', name: 'Tommy'},
]

function findDog(name) {
  for(let i = 0; i<pets.length; ++i) {
    if(pets[i].type === 'Dog' && pets[i].name === name) {
      return pets[i];
    }
  }
}
```

可以简写为：

```
pet = pets.find(pet => pet.type ==='Dog' && pet.name === 'Tommy');
console.log(pet); // { type: 'Dog', name: 'Tommy' }
```

### 18. 简写 Object[key]

你知道`Foo.bar`也可以写成`Foo['bar']`吗？起初，似乎没有什么理由让你这样写。然而，这个符号给了你编写可重用代码的基础。考虑如下简化的验证函数示例：

```
function validate(values) {
  if(!values.first)
    return false;
  if(!values.last)
    return false;
  return true;
}

console.log(validate({first:'Bruce',last:'Wayne'})); // true
```
这个函数可以完美的完成它的任务。但是，考虑一个场景，你有很多表单，你需要进行验证，但有不同的字段和规则。那么，构建一个可以在运行时配置的通用验证函数不是很好吗？

```
// 对象验证规则
const schema = {
  first: {
    required:true
  },
  last: {
    required:true
  }
}

// 通用验证函数
const validate = (schema, values) => {
  for(field in schema) {
    if(schema[field].required) {
      if(!values[field]) {
        return false;
      }
    }
  }
  return true;
}


console.log(validate(schema, {first:'Bruce'})); // false
console.log(validate(schema, {first:'Bruce',last:'Wayne'})); // true
```
现在我们就有了一个可以在所有的`form`中重用的验证函数，而无需为每个`form`编写其自定义的验证函数啦！

### 19. 简写双重按位非运算符

按位运算符绝对是你初学 JavaScript 时了解的但一直没有用武之地的运算符。因为如果不处理二进制，谁会没事操作`0`和`1`呢？但是，双重按位非运算符非常实用，例如你可以使用它来替代`floor()`函数，而且与其他相同的操作相比，按位运算符的操作速度更快。

```
Math.floor(4.9) === 4  //true
```

可以简写为：

```
~~4.9 === 4  //true
```

### 20. Suggest One of U?


我真的很喜欢这些简化的方法，也希望能找到更多，请留下您的评论！


----------

**翻译声明**：本文源自「sitepoint」，[19+ JavaScript Shorthand Coding Techniques](https://www.sitepoint.com/shorthand-javascript-techniques/)。
