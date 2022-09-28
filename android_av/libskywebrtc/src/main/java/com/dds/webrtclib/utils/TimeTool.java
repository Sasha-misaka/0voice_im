package com.dds.webrtclib.utils;

import java.util.Date;

public class TimeTool {
    public static String getTime() {
        return Long.toString(new Date().getTime()/1000);
    }

    /**
     * 返回当前毫秒时间
     * @return
     */
    public static long getCurrentTimeMillis() {
        return  System.currentTimeMillis();
    }
}
