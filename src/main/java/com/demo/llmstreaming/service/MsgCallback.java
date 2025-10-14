package com.demo.llmstreaming.service;

/**
 * 消息回调接口
 */
public interface MsgCallback {
    
    /**
     * 消息回调
     * @param message 当前消息内容
     * @param lines 累积的消息内容
     * @param isSuccess 是否成功
     * @param isDone 是否完成
     */
    void msgCallback(String message, StringBuffer lines, boolean isSuccess, boolean isDone);
    
    /**
     * 是否被中断
     * @return true表示被中断
     */
    boolean isInterrupted();
}

