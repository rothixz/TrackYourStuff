package com.trackyourstuff.mota.trackyourstuff.objects;

/**
 * Created by mota on 10/3/17.
 */

public class TransactionRecord {
    String date, product, cost;
    public TransactionRecord() {
    }

    public TransactionRecord(String date, String product, String cost) {
        this.date = date;
        this.product = product;
        this.cost = cost;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String name) {
        this.date = name;
    }
    public String getProduct() {
        return product;
    }
    public void setProduct(String product) {
        this.product = product;
    }
    public String getCost() {
        return cost;
    }
    public void setCost(String cost) {
        this.cost = cost;
    }
}
