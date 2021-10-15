/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.mostinform.svgconverterservice;

/**
 *
 * @author Дмитрий
 */
public class Detail {
    
    private int id;
    private String articul;
    private int queue;
    private int position;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getArticul() {
        return articul;
    }

    public void setArticul(String articul) {
        this.articul = articul;
    }

    public int getQueue() {
        return queue;
    }

    public void setQueue(int queue) {
        this.queue = queue;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
    
    @Override
    public String toString(){
        return String.valueOf(getId())+" "+getArticul()+" pos:"+getPosition()+" queue:"+getQueue();
    }
    
}
