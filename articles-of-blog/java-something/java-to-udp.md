# 用 Java 语言模拟 UDP 传输的发送端和接收端

一、创建 UDP 传输的发送端
----------------

 - 建立 UDP 的 Socket 服务；
 - 将要发送的数据封装到数据包中；
 - 通过 UDP 的 Socket 服务将数据包发送出去；
 - 关闭 Socket 服务。

```
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSend {

    public static void main(String[] args) throws IOException {
        
        System.out.println("发送端启动......");

        // 1、创建 UDP 的 Socket，使用 DatagramSocket 对象
        DatagramSocket ds = new DatagramSocket();

        // 2、将要发送的数据封装到数据包中
        String str = "UDP传输演示：I'm coming!";

        byte[] buf = str.getBytes();  //使用 DatagramPacket 将数据封装到该对象的包中

        DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.191.1"), 10000);

        // 3、通过 UDP 的 Socket 服务将数据包发送出去，使用 send 方法
        ds.send(dp);

        // 4、关闭 Socket 服务
        ds.close();
    }
}
```

二、创建 UDP 传输的接收端
-------------

 - 建立 UDP 的 Socket 服务，因为要接收数据，所以必须明确一个端口号；
 - 创建数据包，用于存储接收到的数据，方便用数据包对象的方法解析这些数据；
 - 使用 UDP 的 Socket 服务的 receive 方法接收数据并存储到数据包中；
 - 通过数据包的方法解析这些数据；
 - 关闭 Socket 服务。

```
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReceive {
    public static void main(String[] args) throws IOException {

        System.out.println("接收端启动......");

        // 1、建立 UDP 的 Socket 服务
        DatagramSocket ds = new DatagramSocket(10000);

        // 2、创建数据包
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);

        // 3、使用接收方法将数据存储到数据包中
        ds.receive(dp);  // 该方法为阻塞式的方法

        // 4、通过数据包对象的方法解析这些数据，例如：地址、端口、数据内容等
        String ip = dp.getAddress().getHostAddress();
        int port = dp.getPort();
        String text = new String(dp.getData(), 0, dp.getLength());

        System.out.println(ip + ":" + port + ":" + text);

        // 5、关闭 Socket 服务
        ds.close();
    }
}
```

三、优化 UDP 传输的发送端和接收端
-----------------

由于在前两部分中，我们一次只能发送（或接收）一条消息，然后就关闭服务啦！因此如果我们想要发送多条消息，则需要不断的在发送端修改发送的内容，并且还需要重新启动服务器，比较麻烦。为了克服以上的缺点，我们可以对其进行优化，即：

 - 在发送端，创建`BufferedReader`，从键盘录入内容；
 - 在接收端，添加`while(ture)`循环，不断的循环接收内容。

```
/**
* 优化 UDP 传输的发送端
*/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSend {
    public static void main(String[] args) throws IOException {

        System.out.println("发送端启动......");

        // 创建 UDP 的 Socket，使用 DatagramSocket 对象
        DatagramSocket ds = new DatagramSocket();

        BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in));
        String line = null;
        while ((line = bufr.readLine()) != null) {
            // 使用 DatagramPacket 将数据封装到该对象的包中
            byte[] buf = line.getBytes();
            DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.191.1"), 10000);
            // 通过 UDP 的 Socket 服务将数据包发送出去，使用 send 方法
            ds.send(dp);
            // 如果输入信息为 over，则结束循环
            if ("over".equals(line))
                break;
        }
        // 关闭 Socket 服务
        ds.close();
    }
}
```

```
/**
* 优化 UDP 传输的接收端
*/
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPReceive {
    public static void main(String[] args) throws IOException {

        System.out.println("接收端启动......");

        // 建立 UDP 的 Socket 服务
        DatagramSocket ds = new DatagramSocket(10000);

        while(true) {
            // 创建数据包
            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, buf.length);

            // 使用接收方法将数据存储到数据包中
            ds.receive(dp);  // 该方法为阻塞式的方法

            // 通过数据包对象的方法解析这些数据，例如：地址、端口、数据内容等
            String ip = dp.getAddress().getHostAddress();

            int port = dp.getPort();
            String text = new String(dp.getData(), 0, dp.getLength());
            System.out.println(ip + ":" + port + ":" + text);
        }
    }
}
```

四、创建聊天室
-------

根据 UDP（User Datagram Protocol， 用户数据报协议）的相关性质，我们可以进一步创建一个简单的基于 UDP 传输协议下的聊天室，实现互动聊天的功能。

```
/**
* 创建 UDP 传输下的聊天室发送端
*/
package chat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Send implements Runnable {

    private DatagramSocket ds;

    public Send(DatagramSocket ds) {
        this.ds = ds;
    }

    public void run() {
        try {
            BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            while ((line = bufr.readLine()) != null) {
                byte[] buf = line.getBytes();
                DatagramPacket dp = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.191.255"), 10001);
                ds.send(dp);
                if ("886".equals(line))
                    break;
            }
            ds.close();
        } catch (Exception e) {
            System.out.println("对不起，发生错误啦！");
        }
    }
}
```

```
/**
* 创建 UDP 传输下的聊天室接收端
*/
package chat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Rece implements Runnable {

	private DatagramSocket ds;

	public Rece(DatagramSocket ds) {
		this.ds = ds;
	}

	public void run() {
		try {
			while (true) {
				byte[] buf = new byte[1024];
				DatagramPacket dp = new DatagramPacket(buf, buf.length);
				ds.receive(dp);
				String ip = dp.getAddress().getHostAddress();
				String text = new String(dp.getData(), 0, dp.getLength());
				System.out.println(ip + ":::" + text);
				if(text.equals("886")){
					System.out.println(ip+"......退出聊天室！");
				}
			}
		} catch (Exception e) {
			System.out.println("对不起，发生错误啦！");
		}
	}
}
```

```
/**
* 创建 UDP 传输下的聊天室
*/
package chat;

import java.io.IOException;
import java.net.DatagramSocket;

public class ChatRoom {
	public static void main(String[] args) throws IOException {
		DatagramSocket send = new DatagramSocket();
		DatagramSocket rece = new DatagramSocket(10001);
		new Thread(new Send(send)).start();
		new Thread(new Rece(rece)).start();
	}
}
```
