package com.example.administrator.niuapplication;

import com.pili.pldroid.player.AVOptions;

/**
 * ----------------------------------------------------
 * ※ Author :  GaoFei
 * ※ Date : 2019/3/6 0006
 * ※ Time : 下午 5:22
 * ※ Project : feimuAndroid
 * ※ Package : com.qiniu.player
 * ----------------------------------------------------
 */
public class PlayerOption {

    private static PlayerOption playerOption = null;

    private AVOptions avOptions;

    public static PlayerOption create(){
        if(playerOption == null){
            synchronized (PlayerOption.class){
                if(playerOption == null){
                    playerOption = new PlayerOption();

                }
            }
        }
        return playerOption;
    }


    public PlayerOption() {
        avOptions = new AVOptions();
        avOptions.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        avOptions.setInteger(AVOptions.KEY_LIVE_STREAMING,  0);//不是直播
        avOptions.setInteger(AVOptions.KEY_FAST_OPEN, 1);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        avOptions.setInteger(AVOptions.KEY_MEDIACODEC, 0);
//                    avOptions.setInteger(AVOptions.KEY_SEEK_MODE, 0);
        avOptions.setInteger(AVOptions.KEY_PREFER_FORMAT, AVOptions.PREFER_FORMAT_MP4);
        avOptions.setInteger(AVOptions.KEY_START_POSITION, 0);
        avOptions.setString(AVOptions.KEY_DNS_SERVER, "127.0.0.1");
        avOptions.setStringArray(AVOptions.KEY_DOMAIN_LIST, new String[]{"cdn.tvsonar.com","127.0.0.1"});
        avOptions.setInteger(AVOptions.KEY_MP4_PRELOAD , 1);
        avOptions.setInteger(AVOptions.KEY_LOG_LEVEL, 5);
//                    avOptions.setInteger(AVOptions.KEY_OPEN_RETRY_TIMES, 2);
    }


    public AVOptions build(){
        return avOptions;
    }




}
