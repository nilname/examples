package com.rpc.demo;

/**
 * Created by fangqing on 12/23/17.
 */
public class SquerServiceImpl implements SquerService {
    @Override
    public Long squere(long arg) {
        return arg*arg;
    }
}
