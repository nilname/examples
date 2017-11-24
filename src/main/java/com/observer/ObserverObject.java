package com.observer;

/**
 * Created by fangqing on 11/24/17.
 */
public class ObserverObject implements Iobserver {
    String name;
    public ObserverObject(){
        this.name="observer object";
    }
    public ObserverObject(String name){
        this.name=name;
    }
    @Override
    public void update() {
        System.out.println(ObserverObject.class.getName()+name);
    }
}
