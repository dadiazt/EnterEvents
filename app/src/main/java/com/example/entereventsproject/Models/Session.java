package com.example.entereventsproject.Models;

/**
 * Created by Jordi Campoy, Kilian Henares y Dante Diaz 04/05/17.
 */

public class Session {
    private int count=0;

    public Session(int count){
        this.count=count;
    }

    public Session() {

    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
