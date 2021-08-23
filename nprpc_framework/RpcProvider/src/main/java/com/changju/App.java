package com.changju;

import com.changju.provider.RpcProvider;

/**
 * Hello world!
 *
 */
public class App 
{
    /**
     * 启动一个可以提供rpc远程方法的server
     * 1. 需要一个RpcProvider对象(nprpc提供的)
     * 2. 向RpcProvider上面注册rpc方法  UserServiceImpl.login UserServiceImpl.reg
     * 3. 启动RpcProvider这个站点 等待远程rpc方法调用请求
     * @param args
     */
    public static void main( String[] args )
    {
        RpcProvider.Builder builder = RpcProvider.newBuilder();
        RpcProvider rpcProvider = builder.build("config.properties");
//        System.out.println(rpcProvider.getServerIp());
//        System.out.println(rpcProvider.getServerPort());
        rpcProvider.registerRpcService(new UserServiceImpl()); // 注册

        rpcProvider.start(); //启动rpc站点，阻塞等待远程rpc请求

    }
}
