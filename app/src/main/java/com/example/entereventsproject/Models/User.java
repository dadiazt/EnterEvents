package com.example.entereventsproject.Models;

/**
 * Created by Jordi Campoy, Kilian Henares y Dante Diaz 27/04/17.
 */

public class User {
    public String email;
    public String name;

    public User(){}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
