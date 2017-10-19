package com.example.weaterversionone.json;

/**
 * Created by Administrator on 2017/10/16 0016.
 */

public class AQI {
    public AQICity city;
    public class AQICity {
        public String pm25;
        public String qlty;
    }
}
