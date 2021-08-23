package com.changju;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * @description: 原来是本地服务方法，现在要发布成RPC方法
 * @author: Chang Ju
 * @create: 2021-08-22 00:42
 **/
public class UserServiceImpl extends UserServiceProto.UserServiceRpc {
    public boolean login(String name,String pwd){
        System.out.println("call userServiceImp -> login");
        System.out.println("name: "+name);
        System.out.println("pwd: "+pwd);
        return true;
    }

    public boolean reg(String name,String pwd,int age,String sex,String phone){
        System.out.println("call userServiceImpl -> register");
        System.out.println("name: "+ name);
        System.out.println("pwd: " + pwd);
        System.out.println("age: "+age);
        System.out.println("sex: "+sex);
        System.out.println("phone: " + phone);
        return true;
    }

    /**
     * <code>rpc login(.com.changju.LoginRequest) returns (.com.changju.Response);</code>
     * login的rpc代理方法
     * @param controller   可以接收方法执行状态
     * @param request
     * @param done
     */
    @Override
    public void login(RpcController controller, UserServiceProto.LoginRequest request, RpcCallback<UserServiceProto.Response> done) {
        System.out.println("receive remote callMethod");
        // 1. 解析参数
        String name = request.getName();
        String pwd = request.getPwd();

        // 2. 根据参数做本地业务
        boolean result = login(name, pwd);

        // 3. 对返回值进行打包
        UserServiceProto.Response.Builder builder = UserServiceProto.Response.newBuilder();
        builder.setErrno(0);
        builder.setErrinfo("");
        builder.setResult(result);


        // 4. 把response对象给rpc框架，由框架负责发送rpc调用
        done.run(builder.build());
    }

    /**
     * <code>rpc reg(.com.changju.RegRequest) returns (.com.changju.Response);</code>
     * reg的rpc代理方法
     * @param controller
     * @param request
     * @param done
     */
    @Override
    public void reg(RpcController controller, UserServiceProto.RegRequest request, RpcCallback<UserServiceProto.Response> done) {

    }
}
