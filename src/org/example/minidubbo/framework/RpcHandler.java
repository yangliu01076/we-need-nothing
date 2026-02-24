package org.example.minidubbo.framework;

import org.example.mininetty.core.ChannelHandler;
import org.example.mininetty.core.MiniChannel;
import org.example.minispring.core.MiniApplicationContext;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @author duoyian
 * @date 2026/2/14
 */
public class RpcHandler implements ChannelHandler {

    private final MiniChannel channel;

    private final MiniApplicationContext context;

    public RpcHandler(MiniChannel channel, MiniApplicationContext context) {
        this.channel = channel;
        this.context = context;
    }


    @Override
    public void channelRead(Object msg) {
        String request = (String) msg;
        System.out.println("Server received request: " + request);

        try {
            // 1. 解析协议 "interface:method:paramType,paramValue"
            String[] parts = request.split(":");
            if (parts.length < 3) {
                channel.write("Error: Invalid Protocol\n");
                return;
            }

            String interfaceName = parts[0];
            String methodName = parts[1];
            // "Ljava.lang.String;dubbo"
            String paramInfo = parts[2];

            // 解析参数 (简易版：只处理 String 参数)
            String[] paramParts = paramInfo.split(";", 2);
            String paramTypeStr = paramParts[0].replace("L", "").replace(";", "");
            String paramValue = (paramParts.length > 1) ? paramParts[1] : "";

            // 2. 查找服务实现
//            Object serviceImpl = MapService.get(interfaceName);
            Object serviceImpl = context.getBean(interfaceName);
            if (serviceImpl == null) {
                channel.write("Error: Service Not Found\n");
                return;
            }

            // 3. 反射调用方法
            Method method = serviceImpl.getClass().getMethod(methodName);
            Object result = method.invoke(serviceImpl);

            // 4. 返回结果
            channel.write("Result: " + result + "\n");

        } catch (Exception e) {
            e.printStackTrace();
            try {
                channel.write("Error: " + e.getMessage() + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void channelActive() {

    }
}
