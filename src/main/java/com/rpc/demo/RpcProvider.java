package com.rpc.demo;

/**
 * Created by fangqing on 12/23/17.
 */
public class RpcProvider {

    public static void main(String[] args) throws Exception {
        SquerService service = new SquerServiceImpl();
        RpcFramework.export(service, 8000);
    }

}
