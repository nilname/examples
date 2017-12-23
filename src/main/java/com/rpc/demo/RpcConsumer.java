package com.rpc.demo;

/**
 * Created by fangqing on 12/23/17.
 */
public class RpcConsumer {

    public static void main(String[] args) throws Exception {
        SquerService service = RpcFramework.refer(SquerService.class, "127.0.0.1", 8000);
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            Long square = service.squere(i);
            System.out.println(i + "x" + i + "=" + square);
            Thread.sleep(1000);
        }
    }

}
