package com.changju.provider;

import com.changju.RpcMetaProto;
import com.changju.callback.INotifyProvider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.nio.ByteBuffer;

/**
 * @program: nprpc_framework
 * @description: Rpc 服务端，使用netty开发
 * @author: Chang Ju
 * @create: 2021-08-22 08:32
 **/
public class RpcServer {
    private INotifyProvider notifyProvider;
    public RpcServer(INotifyProvider notifyProvider){
        this.notifyProvider = notifyProvider;
    }
    public void start(String ip,Integer port){
        // 创建主事件循环，对应I/O线程，主要用来处理用户的连接事件
        EventLoopGroup mainGroup = new NioEventLoopGroup(1);
        // 创建worker工作线程事件循环，主要用来处理已连接用户的可读写事件
        EventLoopGroup workGroup = new NioEventLoopGroup(3);
        // netty网络服务的启动辅助类
        ServerBootstrap b = new ServerBootstrap();
        b.group(mainGroup,workGroup)
                .channel(NioServerSocketChannel.class) // 底层使用Java Nio Selector模型
                .option(ChannelOption.SO_BACKLOG,1024)// 设置TCP参数
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        /**
                         * 1. 设置数据的编码器和解码器，网络的字节流 《=》业务要处理的数据类型
                         * 2. 设置具体的处理器回调
                         */
                        channel.pipeline().addLast(new ObjectEncoder()); //编码
                        channel.pipeline().addLast(new RpcServerChannel()); //设置事件回调处理器
                    }
                });// 注册事件回调，把业务层的代码和网络层的代码区分来
        try {
            // 阻塞，开启网络服务
            ChannelFuture f = b.bind(ip, port).sync();

            // 关闭网络服务
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            mainGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }


    }

    /**
     * 继承自netty的ChannelInboundHandlerAdapter适配器类，主要提供相应的回调操作
     */
    private class RpcServerChannel extends ChannelInboundHandlerAdapter{
        /**
         * 处理接收到的事件
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            // 远端发送过来的rpc请求的所有参数：head_size(表示服务对象和服务方法的长度)+UserServiceRpclogin+zhangsan123456(LoginRequest)
            ByteBuf request = (ByteBuf)msg;
            // 1. 先读取头部信息的长度
            int headSize = request.readInt();

            // 2. 读取头部信息（对象名称+方法名称）
            byte[] metabuf = new byte[headSize];
            request.readBytes(metabuf);

            // 3. 反序列化RpcMeta
            RpcMetaProto.RpcMeta rpcMeta = RpcMetaProto.RpcMeta.parseFrom(metabuf);
            String serverName = rpcMeta.getServiceName();
            String methodName = rpcMeta.getMethodName();

            // 4. 读rpc方法的参数
            byte[] argbuf = new byte[request.readableBytes()];
            request.readBytes(argbuf);

            // 5. 上报给rpcProvider，回调
            byte[] response = notifyProvider.notify(serverName,methodName, argbuf);

            // 6. 把rpc方法的响应发送给rpc调用方
            ByteBuf buf = Unpooled.buffer(response.length);
            buf.writeBytes(response);
            ChannelFuture f = ctx.writeAndFlush(buf);

            // 7. 模拟http响应，完成后直接关闭连接
            if(f.sync().isSuccess()){
                ctx.close();
            }
        }

        /**
         * 连接异常处理
         * @param ctx
         * @param cause
         * @throws Exception
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}
