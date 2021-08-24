package com.changju.util;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @program: nprpc_framework
 * @description: 和zookeeper通信的辅助工具类
 * @author: Chang Ju
 * @create: 2021-08-23 21:22
 **/
public class ZkClientUtils {
    private ZkClient zkClient;
    private static final String rootPath = "/nprpc";
    private Map<String,String> ephemeralMap = new HashMap<>();
    /**
     * 防止出现provider关闭后立即启动，由于没有超过3秒session还没过期，
     * 因此节点信息依然存在，新启动之后不会再添加节点，但3秒之后节点信息就会删除
     *
     * description:给zk上指定的znode添加watcher监听
     * @param path
     */
    public void addWatcher(String path){
        this.zkClient.subscribeDataChanges(rootPath + path, new IZkDataListener() {

            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                System.out.println("watcher -> handleDataDelted: "+ path);
                createEphemeral(path, ephemeralMap.get(path));

            }
        });
    }

    /**
     * 关闭和zKserver的连接
     */
    public void close(){
        this.zkClient.close();
    }


    /**
     * 通过字符串信息连接server
     * @param serverList "192.168.100.2:8080;192.168.100.3:6000;"
     */
    public ZkClientUtils(String serverList){
        this.zkClient = new ZkClient(serverList,1000);// client和server 3秒心跳检查
        if(!zkClient.exists(rootPath))
            zkClient.createPersistent(rootPath, null);
    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    /**
     * 在Zk上创建临时节点，当zkclient断了之后节点自动释放掉
     * @param path
     * @param data
     */
    public void createEphemeral(String path,String data){
        ephemeralMap.put(path,data);
        path = rootPath + path;
        if(!zkClient.exists(path))// 节点不存在才创建
            this.zkClient.createEphemeral(path, data);

    }

    /**
     * zk上创建永久性节点
     * @param path
     * @param data
     */
    public void createPersistent(String path,String data){
        path = rootPath + path;
        if(!zkClient.exists(path))
            this.zkClient.createPersistent(path, data);
    }

    public String readData(String path){
        return zkClient.readData(rootPath+path,null);
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {
        ZkClientUtils zkClientUtils = new ZkClientUtils("127.0.0.1:2181");
        zkClientUtils.createPersistent("/ProductService", "12345");
        zkClientUtils.close();
    }
}
