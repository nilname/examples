package com.example.demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by fangqing on 1/31/18.
 */
public class LogDemo {
    static final Logger logger = LoggerFactory.getLogger(LogDemo.class);
    public static void main(String[] args) {
        long t1=System.currentTimeMillis();
        for(int i=0;i<100;i++)
            logger.info("pring -->"+i);

        long t2=System.currentTimeMillis();
        System.out.println(t2-t1);
    }
}
