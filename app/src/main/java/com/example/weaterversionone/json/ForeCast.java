package com.example.weaterversionone.json;

/**
 * Created by Administrator on 2017/10/16 0016.
 */

public class ForeCast {

    public String date;
    public Cond cond;
    public Tmp tmp;
    public class Cond {
        public String txt_d;
    }
    public class Tmp {
        public String max;
        public String min;
    }
}
