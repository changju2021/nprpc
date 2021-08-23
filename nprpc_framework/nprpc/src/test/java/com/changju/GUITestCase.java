package com.changju;

/**
 * 事件回调
 *
 * 描述：模拟界面类 接收用户发送的时间，处理完成发送结果或者处理过程中显示进度
 * 需求：
 * 1. 显示下载进度
 * 2. 下载完成后显示信息
 *
 *
 */
public class GUITestCase implements INotifyCallBack{
    private DownLoad downLoad;
    public GUITestCase(){
        this.downLoad = new DownLoad(this);
    }
    public void downLoadFile(String file){
        System.out.println("begin download file:"+file);
        downLoad.start(file);
    }

    /**
     * 显示下载进度
     * @param file
     * @param progress
     */
    public void progress(String file,int progress){
        System.out.println("download file:"+file+" progress: +" + progress+"%");
    }

    /**
     * 显示文件下载完成
     * @param file
     */
    public void result(String file){
        System.out.println("download file:"+file+" over.");
    }

    public static void main(String[] args) {
        GUITestCase guiTestCase = new GUITestCase();
        guiTestCase.downLoadFile("homework");
    }
}

/**
 * 声明一个接口，把需要上报的时间都定义在接口内
 */
interface INotifyCallBack{
    void progress(String file,int progress);
    void result(String file);
}
// 底层的一个类，负责下载内容
class DownLoad{
    /**
     * 底层执行下载文件的方法
     */
    private INotifyCallBack cb;// 面向接口编程
    public DownLoad(INotifyCallBack cb){
        this.cb = cb;
    }
    public void start(String file){
        int count = 0;
        while(count<=100){
            try {
                cb.progress(file, count);
                Thread.sleep(1000);
                count+=20;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cb.result(file);
    }
}
