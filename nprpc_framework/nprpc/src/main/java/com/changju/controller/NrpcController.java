package com.changju.controller;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * @program: nprpc_framework
 * @description:
 * @author: Chang Ju
 * @create: 2021-08-23 19:55
 **/
public class NrpcController implements RpcController {
    private String errText;
    private boolean isFailed;

    @Override
    public void reset() {
        this.isFailed = false;
        this.errText = "";
    }

    @Override
    public boolean failed() {
        return isFailed;
    }

    @Override
    public String errorText() {
        return errText;
    }

    @Override
    public void startCancel() {

    }

    @Override
    public void setFailed(String s) {   // 设置错误信息
        errText = s;
        isFailed = true;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void notifyOnCancel(RpcCallback<Object> rpcCallback) {

    }
}
