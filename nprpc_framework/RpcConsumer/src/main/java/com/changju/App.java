package com.changju;

import com.changju.consumer.RpcConsumer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        // 代理对象，要传入一个实现RpcChannel接口的对象
        UserServiceProto.UserServiceRpc.Stub stub = UserServiceProto.UserServiceRpc.newStub(new RpcConsumer());
        UserServiceProto.LoginRequest.Builder login_builder = UserServiceProto.LoginRequest.newBuilder();
        login_builder.setName("zhangsan");
        login_builder.setPwd("123456");

        stub.login(null,login_builder.build(),response -> {
            // 这里response是rpc方法调用完成以后的返回值
            System.out.println("receive rpc call response");
            if(response.getErrno()==0){
                System.out.println(response.getResult());
            }else{  // 发生错误
                System.out.println(response.getErrinfo());
            }
        });

    }
}
