package com.example.demo.threadPoolDemo;

/**
 * Created by fangqing on 11/20/17.
 */
class MyRunable implements Runnable {
    private int i = 0;

    @Override
    public void run() {
        for (i = 0; i < 10; i++) {
            System.out.println(Thread.currentThread().getName() + MyRunable.class.getName()+" " + i);
        }
    }
}
