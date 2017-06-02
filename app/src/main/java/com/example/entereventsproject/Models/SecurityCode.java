package com.example.entereventsproject.Models;

/**
 * Created by Jordi Campoy, Kilian Henares y Dante Diaz 25/05/17.
 */

public class SecurityCode {
    private String code;
    private String userId;
    private String ticket_quantity;
    private String date;
    private String hora;

    public SecurityCode() {
    }

    public SecurityCode(String code, String userId, String ticket_quantity, String date, String hora) {
        this.code = code;
        this.userId = userId;
        this.ticket_quantity = ticket_quantity;
        this.date = date;
        this.hora = hora;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTicket_quantity() {
        return ticket_quantity;
    }

    public void setTicket_quantity(String ticket_quantity) {
        this.ticket_quantity = ticket_quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
