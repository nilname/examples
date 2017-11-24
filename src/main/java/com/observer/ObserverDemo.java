package com.observer;

/**
 * Created by fangqing on 11/24/17.
 */
public class ObserverDemo {
    public static void main(String[] args) {

        ObserverMan observerMan = new ObserverMan();
        ObserverObject observerObjecta=new ObserverObject();
        ObserverObject observerObjectb=new ObserverObject(" object b");
        ObserverObject observerObjectc=new ObserverObject(" object c");
        ObserverObject observerObjectd=new ObserverObject(" object d");
        observerMan.registerObject(observerObjecta);
        observerMan.registerObject(observerObjectb);
        observerMan.registerObject(observerObjectc);
        observerMan.registerObject(observerObjectd);
        observerMan.setConfs(new Confs("10.10.144.123",44444));
    }
}
