package com.changju;

import static org.junit.Assert.assertTrue;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * 测试Protobuf的序列化和反序列化
     */
    @Test
    public void test1()
    {
        //  建造者模式
        testProto.LoginRequest.Builder login_builder = testProto.LoginRequest.newBuilder();
        login_builder.setName("zhangsan");
        login_builder.setPwd("123456");

        testProto.LoginRequest request = login_builder.build();
        System.out.println(request.getName());
        System.out.println(request.getPwd());


        // 把loginRequest对象序列化成字节流，sendbuf就可以通过网络发送出去了
        byte[] sendbuf = request.toByteArray();

        // 反序列化字节流数组
        testProto.LoginRequest deseriable = null;
        try {
            deseriable = testProto.LoginRequest.parseFrom(sendbuf);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        System.out.println(deseriable.getName());
        System.out.println(deseriable.getPwd());
    }

    /**
     * 测试properties文件的加载
     */
    @Test
    public void test2(){
        Properties pro = new Properties();
        try {
            pro.load(AppTest.class.getClassLoader().getResourceAsStream("config.properties"));
            System.out.println(pro.getProperty("IP"));
            System.out.println(pro.getProperty("PORT"));
            System.out.println(pro.getProperty("ZOOKEEPER"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
