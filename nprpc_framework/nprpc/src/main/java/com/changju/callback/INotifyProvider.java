package com.changju.callback;

/**
 * @program: nprpc_framework
 * @description:
 * @author: Chang Ju
 * @create: 2021-08-22 13:36
 **/
public interface INotifyProvider {
    /**
     * 回调操作，RpcServer给RpcProvider上报接收到的rpc服务调用相关参数信息
     * @param serviceName
     * @param methodName
     * @param args
     * @return
     */
    byte[] notify(String serviceName,String methodName,byte[] args);
}
