package com.changju.consumer;

import com.changju.RpcMetaProto;
import com.google.protobuf.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @program: nprpc_framework
 * @description:
 * @author: Chang Ju
 * @create: 2021-08-22 20:29
 **/
public class RpcConsumer implements RpcChannel {

    /**
     * stub代理对象需要接收一个实现了RpcChannel的对象，当用Stub调用任意rpc方法的时候，
     * 全部都调用了当前这个RpcChannel的callMethod方法
     * @param methodDescriptor
     * @param rpcController
     * @param message
     * @param message1
     * @param rpcCallback
     */
    @Override
    public void callMethod(Descriptors.MethodDescriptor methodDescriptor,
                           RpcController rpcController,
                           Message message, // request
                           Message message1, // response
                           RpcCallback<Message> rpcCallback) {
        /**
         * 打包参数，递交网络发送
         * rpc调用参数格式：headersize+service_name+method_name+参数
         */
        Descriptors.ServiceDescriptor sd = methodDescriptor.getService();
        String service_name = sd.getName();
        String method_name = methodDescriptor.getName();

        RpcMetaProto.RpcMeta.Builder metabuilder = RpcMetaProto.RpcMeta.newBuilder();
        metabuilder.setServiceName(service_name);
        metabuilder.setMethodName(method_name);
        byte[] metabuf = metabuilder.build().toByteArray();

        byte[] argsbuf = message.toByteArray();

        ByteBuf buf = Unpooled.buffer(4+metabuf.length+argsbuf.length);
        buf.writeByte(metabuf.length);
        buf.writeBytes(metabuf);
        buf.writeBytes(argsbuf);

        // 待发送的数据
        byte[] sendbuf = buf.array();

        //通过网络发送调用信息
        Socket client = null;
        OutputStream out = null;
        InputStream in = null;


        try {
            client = new Socket();
            client.connect(new InetSocketAddress("127.0.0.1",6000));

            out = client.getOutputStream();
            in = client.getInputStream();

            // 发送数据
            out.write(sendbuf);
            out.flush();

            //等待rpc结果
            ByteArrayOutputStream recvbuf = new ByteArrayOutputStream();
            byte[] rbuf = new byte[1024];
            int size = in.read(rbuf);
            /**
             * 这里的size有可能是0，因为rpcProvider封装Response响应的成员变量值可能为默认值
             */
            if(size>0){
                System.out.println(">0");
                recvbuf.write(rbuf,0,size);
                rpcCallback.run(message1.getParserForType().parseFrom(recvbuf.toByteArray()));
            }else{
                System.out.println("==0");
                rpcCallback.run(message1.getParserForType().parseFrom(new byte[0]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(out!=null) out.close();
                if(in!=null) in.close();
                if(client!=null) client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
