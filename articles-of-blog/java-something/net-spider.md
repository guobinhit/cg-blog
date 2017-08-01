# 网络爬虫二三事儿

1、网络爬虫简介
--------

网络爬虫（又被称为网页蜘蛛，网络机器人，在 FOAF 社区中间，更经常的称为网页追逐者），是一种按照一定的规则，自动地抓取万维网信息的程序或者脚本。另外一些不常使用的名字还有蚂蚁、自动索引、模拟程序或者蠕虫。

2、网络爬虫分类
--------

网络爬虫按照系统结构和实现技术，大致可以分为以下几种类型：

 - 深层网络爬虫（Deep Web Crawler）
 - 聚焦网络爬虫（Focused Web Crawler）
 - 增量式网络爬虫（Incremental Web Crawler）
 - 通用网络爬虫（General Purpose Web Crawler）

在实际的网络爬虫系统中，通常是几种爬虫技术相结合实现的。

3、聚焦网络爬虫
--------

网络爬虫是一个自动提取网页的程序，它为搜索引擎从万维网上下载网页，是搜索引擎的重要组成。传统爬虫从一个或若干初始网页的 URL 开始，获得初始网页上的 URL，在抓取网页的过程中，不断从当前页面上抽取新的 URL 放入队列,直到满足系统的一定停止条件。聚焦爬虫的工作流程较为复杂，需要根据一定的网页分析算法过滤与主题无关的链接，保留有用的链接并将其放入等待抓取的 URL 队列。然后，它将根据一定的搜索策略从队列中选择下一步要抓取的网页 URL，并重复上述过程，直到达到系统的某一条件时停止。另外，所有被爬虫抓取的网页将会被系统存贮，进行一定的分析、过滤，并建立索引，以便之后的查询和检索；对于聚焦爬虫来说，这一过程所得到的分析结果还可能对以后的抓取过程给出反馈和指导。相对于通用网络爬虫，聚焦爬虫还需要解决三个主要问题：

 1. 对抓取目标的描述或定义；
 2. 对网页或数据的分析与过滤；
 3. 对 URL 的搜索策略。

4、网络爬虫示例
--------

在本部分中，演示简单的网络爬虫过程，分别爬取本地和网页中“邮箱地址”信息，其中通过`getMails()`爬取本地邮箱地址，通过`getMailsByWeb()`爬取网页邮箱地址。

```
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InfoSpider {
    public static void main(String[] args) throws IOException {

        List list = getMailsByWeb();

        // 创建迭代器对象
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String mail = (String) it.next();
            System.out.println(mail);
        }
    }

    /**
     * 爬取网络邮箱地址
     */
    public static List<String> getMailsByWeb() throws IOException {

        // 创建 URL 对象
        URL url = new URL("http://blog.csdn.net/qq_35246620/article/details/");

        // 通过 BufferedReader 读取网页数据
        BufferedReader bufIn = new BufferedReader(new InputStreamReader(url.openStream()));

        // 对读取的数据进行规制的匹配，从中获取符合规制的数据
        String mail_regex = "\\w+@\\w+(\\.\\w+)+";

        // 创建 list 集合存储数据
        ArrayList list = new ArrayList();

        // 将正则表达式封装成对象
        Pattern p = Pattern.compile(mail_regex);

        String line = null;
        while ((line = bufIn.readLine()) != null) {
            Matcher m = p.matcher(line);
            while (m.find()) {
                list.add(m.group());
            }
        }
        return list;
    }

    /**
     * 爬取本地邮箱地址
     */
    public static List<String> getMails() throws IOException {

        // 读取源文件
        BufferedReader bufr = new BufferedReader(new FileReader("d:\\mail.html"));

        // 对读取的数据进行规制的匹配，从中获取符合规制的数据
        String mail_regex = "\\w+@\\w+(\\.\\w+)+";

        // 创建 list 集合存储数据
        ArrayList list = new ArrayList();

        // 将正则表达式封装成对象
        Pattern p = Pattern.compile(mail_regex);

        String line = null;
        while ((line = bufr.readLine()) != null) {
            Matcher m = p.matcher(line);
            while (m.find()) {
                // 将符合规制的数据存储到集合中
                list.add(m.group());
            }
        }
        return list;
    }
}
```

