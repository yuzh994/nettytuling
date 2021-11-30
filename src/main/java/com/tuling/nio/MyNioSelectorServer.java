package com.tuling.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yuzh
 * @date 2021/11/30 13:56
 */
public class MyNioSelectorServer {
    public static void main(String[] args) throws Exception {
        //创建NIO ServerSocketChannel
        ServerSocketChannel socketChannel = ServerSocketChannel.open();
        socketChannel.socket().bind(new InetSocketAddress(9001));
        //设置为 非阻塞
        socketChannel.configureBlocking(false);

        //打开Selector处理channel 既创建 epoll
        Selector selector = Selector.open();

        //把 serverSocketChannel注册到selector上，并绑定 accept事件
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务启动成功");
        while (true) {
            //阻塞等待需要处理的事件发生
            selector.select();

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel accept = server.accept();
                    accept.configureBlocking(false);

                    accept.register(selector, SelectionKey.OP_ACCEPT);
                    System.out.println("客户端连接成功");
                } else if (key.isReadable()) {
                    SocketChannel socketChannel1 = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(128);
                    int len = socketChannel1.read(byteBuffer);
                    if (len > 0) {
                        System.out.println("接收到消息" + new String(byteBuffer.array()));
                    } else if (len == -1) {
                        System.out.println("客户端端口连接");
                        socketChannel.close();
                    }
                }
                iterator.remove();

            }
        }
    }
}
