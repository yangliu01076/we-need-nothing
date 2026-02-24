package minidubbo.framework;

//import com.minidubbo.core.RpcRequest;
//import com.mininetty.MiniChannel;
//import com.mininetty.core.EventLoopGroup;
//import minispring.beans.ApplicationContext;
import mininetty.core.EventLoopGroup;
import minispring.core.MiniApplicationContext;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

/**
 * RPC 服务端：串联 Spring、Netty 和业务逻辑
 * @author duoyian
 * @date 2026/2/24
 */
public class RpcServer {

    // Mini Spring 容器
    private MiniApplicationContext applicationContext;
    private int port;

    public RpcServer(MiniApplicationContext applicationContext, int port) {
        this.applicationContext = applicationContext;
        this.port = port;
    }

    // 启动 Mini Netty 和 Dubbo 服务，直接抛出异常，实际需要处理异常
    public void start() throws IOException {
        // 1. 启动 Mini Netty
        EventLoopGroup bossGroup = EventLoopGroup.createBossGroup();
        // 2个 Worker 线程
        EventLoopGroup workerGroup = new EventLoopGroup(2);
        bossGroup.next().setWorkerGroup(workerGroup);

        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));

            // 2. 注册 ServerSocket
            bossGroup.register(serverChannel,applicationContext);
            System.out.println("[Mini Dubbo] Server started on port: " + port);

            // 3. 这里是关键：我们需要修改 Mini Netty 的 EventLoop，让它在处理读事件时调用我们的 handler
            // 假设你的 MiniChannel 构造函数允许传入一个回调
            // 或者我们在 Spring 启动时初始化 Netty，并设置全局处理器

            // (伪代码示意：实际需要修改 EventLoop 的逻辑来支持外部 Handler 注入)
            // setupGlobalHandler(applicationContext);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 静态处理器方法：供 Mini Netty 的 IO 线程回调
     * 这里实现了 "Mini Dubbo" 的核心逻辑：协议解析 -> 服务查找 -> 反射调用
     */
    public static byte[] handleRequest(byte[] data, MiniApplicationContext context) {
        try {
            // A. 反序列化 (假设使用简单的字符串分割或 Java 序列化，这里简化处理)
            // String reqJson = new String(data);
            // RpcRequest request = JSON.parseObject(reqJson, RpcRequest.class);

            // 为了演示，假设我们已经解析出了 RpcRequest 对象
            // RpcRequest request = deserialize(data);
            // 这里模拟一个 request：
            String interfaceName = "com.example.UserService";
            String methodName = "sayHello";
            String arg = "World";

            // B. 服务查找
            // 根据 interfaceName 从 Mini Spring 容器中获取 Bean
            Object serviceBean = context.getBean(interfaceName);
            if (serviceBean == null) {
                return ("Service not found: " + interfaceName).getBytes();
            }

            // C. 反射调用
            Method method = serviceBean.getClass().getMethod(methodName, String.class);
            Object result = method.invoke(serviceBean, arg);

            // D. 返回结果
            return result.toString().getBytes();

        } catch (Exception e) {
            e.printStackTrace();
            return ("Error: " + e.getMessage()).getBytes();
        }
    }
}
