package com.changju;

import com.changju.consumer.RpcConsumer;
import com.changju.controller.NrpcController;

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

        NrpcController con = new NrpcController();
        stub.login(con,login_builder.build(), response -> {
            // 这里response是rpc方法调用完成以后的返回值
            System.out.println("receive rpc call response");
            // 表示rpc方法没有调用成功
            if(con.failed()){
                System.out.println(con.errorText());

            }else{
                if(response.getErrno()==0){
                    System.out.println(response.getResult());
                }else{  // 发生错误
                    System.out.println(response.getErrinfo());
                }
            }
        });

    }
}
