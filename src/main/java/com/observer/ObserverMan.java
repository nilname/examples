package com.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fangqing on 11/24/17.
 */
public class ObserverMan implements Iobserver {
    Confs confs;
    List<Iobserver> list;

    public ObserverMan() {
    list=new ArrayList<Iobserver>();
    confs=new Confs();
    }

    public ObserverMan(List<Iobserver> list) {
        this.list = list;
        confs=new Confs();
    }
    public void registerObject(Iobserver iobserver){
        this.list.add(iobserver);
    }

    public void  removeObject(Iobserver iobserver){
        this.list.remove(iobserver);
    }

    public Confs getConfs() {
        return confs;
    }

    public void setConfs(Confs confs) {
        this.confs = confs;
        update();
    }

    @Override
    public void update() {

        for (Iobserver e : list) {
            e.update();
        }

    }
}
