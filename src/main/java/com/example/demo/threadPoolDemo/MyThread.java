package com.example.demo.threadPoolDemo;

/**
 * Created by fangqing on 11/20/17.
 */
public class MyThread extends Thread {

    private int i = 0;

    @Override
    public void run() {
        for (i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() +MyThread.class.getName()+ " " + i);
        }
    }
}