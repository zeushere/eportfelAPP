package com.example.eportfelapp;

public class Data {

    String item,date,id;
    int amount,week,month;
    String notes;

    public Data() {
    }

    public Data(String item, String date, String id, int amount, int week, int month, String notes) {
        this.item = item;
        this.date = date;
        this.id = id;
        this.amount = amount;
        this.week = week;
        this.month = month;
        this.notes = notes;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getNotes() {
        return notes;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }


    public void setNotes(String notes) {
        this.notes = notes;
    }
}
