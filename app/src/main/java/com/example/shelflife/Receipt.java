package com.example.shelflife;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class Receipt {
    private String id;
    private Date date;
    private List<ShelfFragment.Item> expiredItems;

    public Receipt(){
    //empty constructor needed for firestore
    }
    public Receipt(List<ShelfFragment.Item> expiredItems) {
        this.id = java.util.UUID.randomUUID().toString();
        this.date = new Date();
        this.expiredItems = expiredItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<ShelfFragment.Item> getExpiredItems() {
        return expiredItems;
    }

    public void setExpiredItems(List<ShelfFragment.Item> expiredItems) {
        this.expiredItems = expiredItems;
    }

}
