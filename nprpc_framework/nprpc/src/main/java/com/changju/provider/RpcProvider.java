package com.changju.provider;

import com.changju.callback.INotifyProvider;
import com.google.protobuf.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @program: nprpc_framework
 * @description: Rpc方法发布的站点，只需要一个站点就能发布当前主机上所有rpc方法，用单例模式设计
 * @author: Chang Ju
 * @create: 2021-08-22 01:41
 **/
public class RpcProvider implements INotifyProvider{
    /**
     * 封装rpc对象创建的细节
     */
    private static final String SERVER_IP="ip";
    private static final String SERVER_PORT="port";
    private String serverIp;
    private int serverPort;
    private ThreadLocal<byte[]> responsebuf;

    /**
     * 服务方法的类型信息，只是读，不会有线程安全问题，所以HashMap也可以
     */
    private class ServiceInfo{
        Service service;
        Map<String, Descriptors.MethodDescriptor> methodMap;
        public ServiceInfo(){
            this.service = null;
            this.methodMap = new HashMap<>();
        }
    }

    /**
     * 包含所有的对象和服务方法
     */
    private Map<String,ServiceInfo> serviceMap;
    private RpcProvider(){
        this.serviceMap = new HashMap<>();
        this.responsebuf = new ThreadLocal<>();
    }

    /**
     * 启动rpc站点提供服务
     */
    public void start() {

        /*serviceMap.forEach((k,v)->{
            System.out.println(k);
            v.methodMap.forEach((a,b) -> {
                System.out.println(a);
            });
        });*/
        System.out.println("rpc start at "+"IP: "+serverIp+" "+"Port: "+serverPort);
        // 启动rpc server网络服务，等待远程rpc操作
        RpcServer server = new RpcServer(this);
        server.start(serverIp, serverPort);
    }


    /**
     * 注册rpc服务，只要是rpc方法都实现了com.google.protobuf.Service这个接口
     * 需要注册服务对象（UserServiceImpl）和服务方法（login、reg......）
     * @param service
     */
    public void registerRpcService(Service service){
        Descriptors.ServiceDescriptor sd = service.getDescriptorForType();
        String serviceName = sd.getName();//获取服务对象名称

        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.service = service;

        List<Descriptors.MethodDescriptor> md = sd.getMethods();//获取对象的所有rpc方法
        md.forEach(method->{
            String methodName = method.getName();
            System.out.println(methodName);
            serviceInfo.methodMap.put(methodName, method);
        });
        serviceMap.put(serviceName, serviceInfo);
    }
    /**
     * 回调操作，接收RpcServer上报的rpc服务调用相关参数信息，实现相关方法并返回调用结果
     * RpcProvider只有一个，但notify方法是在多线程环境中被调用的，
     * @param serviceName
     * @param methodName
     * @param args
     * @return
     */
    @Override
    public byte[] notify(String serviceName, String methodName, byte[] args) {
        ServiceInfo serviceInfo = serviceMap.get(serviceName);
        Service service = serviceInfo.service;
        Descriptors.MethodDescriptor md = serviceInfo.methodMap.get(methodName);
        //为对象里的方法的参数创建一个实例对象request
        Message request = service.getRequestPrototype(md).toBuilder().build();
        try {
            request = request.getParserForType().parseFrom(args);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        service.callMethod(md, null, request, response -> responsebuf.set(response.toByteArray()));
        return responsebuf.get();
    }





    public static class Builder{

        private static RpcProvider INSTANCE=new RpcProvider();
        /**
         * 从配置文件中读取server的IP和Port，给INSTANCE对象初始化数据
         * @param file
         */

        public RpcProvider build(String file){
            Properties pro = new Properties();
            try {
                pro.load(Builder.class.getClassLoader().getResourceAsStream(file));
                INSTANCE.setServerIp(pro.getProperty(SERVER_IP));
                INSTANCE.setServerPort(Integer.parseInt(pro.getProperty(SERVER_PORT)) );
                return INSTANCE;
            } catch (IOException e) {
                e.printStackTrace();

            }
            return null;
        }
    }
    /**
     * 返回一个对象构建器
     * @return
     */
    public static Builder newBuilder(){
        return new Builder();
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }
}
