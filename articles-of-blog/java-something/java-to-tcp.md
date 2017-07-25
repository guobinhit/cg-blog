# 用 Java 模拟 TCP 传输的客户端和服务端

一、创建 TCP 传输的客户端
-------------

 - 建立 TCP 客户端的 Socket 服务，使用的是 Socket 对象，建议该对象一创建就明确目的地，即要连接的主机；
 - 如果连接建立成功，说明数据传输通道已建立，该通道就是 Socket 流，是底层建立好的，既然是流，说着这里既有输入流，又有输出流，想要输入流或者输出流对象，可以通过 Socket 来获取，可以通过`getOutputStream()`和`getInputStream()`来获取；
 - 使用输出流，将数据写出；
 - 关闭 Socket 服务。

```
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {

        // 1、创建客户端的 Socket 服务
        Socket socket = new Socket("192.168.1.100", 10002);

        // 2、获取 Socket 流中输入流
        OutputStream out = socket.getOutputStream();

        // 3、使用输出流将指定的数据写出去
        out.write("TCP is coming !".getBytes());

        // 4、关闭 Socket 服务
        socket.close();
    }
}
```

二、创建 TCP 传输的服务端
-------------

 - 建立 TCP 服务端的的 Socket 服务，通过 ServerSocket 对象；
 - 服务端必须对外提供一个端口，否则客户端无法连接；
 - 获取连接过来的客户端对象；
 - 通过客户端对象来获取 Socket 流，读取客户端发来的数据；
 - 关闭资源，关客户端，关服务端。

```
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws IOException {

        // 1、创建客户端对象
        ServerSocket ss = new ServerSocket(10002);

        // 2、获取连接过来的客户端对象
        Socket s = ss.accept();

        String ip = s.getInetAddress().getHostAddress();

        // 3、通过 Socket 对象获取输入流，读取客户端发来的数据
        InputStream in = s.getInputStream();

        byte[] buf = new byte[1024];

        int len = in.read(buf);
        String text = new String(buf, 0, len);
        System.out.println(ip + ":" + text);
	
    	// 4、关闭资源
        s.close();
        ss.close();
    }
}
```

三、优化 TCP 传输的客户端和服务端
-----------------

在本部分，我们对前两部分的内容进行优化，实现 TCP 传输模式下的客户端和服务端的交互功能。

```
/**
* 优化 TCP 传输的客户端
*/
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientUpdate {
    public static void main(String[] args) throws IOException {

        Socket socket = new Socket("192.168.1.100", 10002);

        OutputStream out = socket.getOutputStream();

        out.write("tcp!".getBytes());

        // 读取服务端返回的数据，使用 Socket 读取流
        InputStream in = socket.getInputStream();
        byte[] buf = new byte[1024];

        int len = in.read(buf);

        String text = new String(buf, 0, len);

        System.out.println(text);

        socket.close();
    }
}
```

```
/**
* 优化 TCP 传输的服务端
*/
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerUpdate {
    public static void main(String[] args) throws IOException {

        // 1、创建服务端对象
        ServerSocket ss = new ServerSocket(10002);

        // 2、获取连接过来的客户端对象
        Socket s = ss.accept();  // accept 方式为阻塞式方法

        String ip = s.getInetAddress().getHostAddress();

        // 3、通过 Socket 对象获取输入流，要读取客户端发来的数据
        InputStream in = s.getInputStream();

        byte[] buf = new byte[1024];

        int len = in.read(buf);
        String text = new String(buf, 0, len);
        System.out.println(ip + ":" + text);

        // 使用客户端的 Socket 对象的输出流给客户端返回数据
        OutputStream out = s.getOutputStream();
        out.write("收到".getBytes());

        s.close();
        ss.close();
    }
}
```

四、创建英文大写转换服务器
---------------

应用 TCP（Transmission Control Protocol，传输控制协议）的相关性质，创建一个基于 TCP 传输下的英文大写转换服务器，要求：客户端输入字母数据，发送给服务端；服务端收到数据后显示在控制台，并将该数据转成大写字母返回给客户端；直到客户端输入“over”为止，转换结束。

```
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TransClient {
    public static void main(String[] args) throws IOException {
        /**
         * 思路：创建客户端
         * 1、创建 Socket 客户端对象
         * 2、获取键盘录入的数据
         * 3、将录入的信息发送给 Socket 输出流
         * 4、读取服务端的数据并返回的大写数据
         */

        // 1、创建 Socket 客户端对象
        Socket s = new Socket("192.168.1.100", 10004);

        // 2、获取键盘录入
        BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in));

        // 3、Socket 输出流
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);

        // 4、Socket 输入流，读取服务端的数据并返回的大写数据
        BufferedReader bufIn = new BufferedReader(new InputStreamReader(s.getInputStream()));

        String line = null;

        while ((line = bufr.readLine()) != null) {

            if ("over".equals(line))
                break;
            out.println(line);

            // 读取服务端返回的一行大写数据
            String upperStr = bufIn.readLine();
            System.out.println(upperStr);
        }
        s.close();
    }
}
```

```
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TransServer {
    public static void main(String[] args) throws IOException {
        /**
         * 思路：创建服务端
         * 1、创建 SeverSocket 客户端对象
         * 2、获取 Socket 流
         * 3、通过 Socket，读取客户端发过来的需要转换的数据
         * 4、显示在控制台上
         * 5、将数据转换成大写返回给客户端
         */

        // 1、创建 SeverSocket 对象
        ServerSocket ss = new ServerSocket(10004);

        // 2、获取 Socket 对象
        Socket s = ss.accept();

        // 获取 IP 地址
        String ip = s.getInetAddress().getHostAddress();
        System.out.println(ip + "......connected");

        // 3、获取 Socket 读取流，并装饰
        BufferedReader bufIn = new BufferedReader(new InputStreamReader(s.getInputStream()));

        // 4、获取 Socket 的输出流，并装饰
        PrintWriter out = new PrintWriter(s.getOutputStream(), true);

        String line = null;
        while ((line = bufIn.readLine()) != null) {
            System.out.println(line);
            out.println(line.toUpperCase());
        }

        s.close();
        ss.close();
    }
}
```


