package com.trackyourstuff.mota.trackyourstuff.objects;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by mota on 10/16/17.
 */

public class Order {
    private ArrayList<Pizza> products;
    private String id;
    private String client_id;
    private String created_at;
    private String transporter_id;
    private String status;

    public Order(ArrayList<Pizza> products, String id, String client_id, String created_at) {
        this.id = id;
        this.products = products;
        this.client_id = client_id;
        this.created_at = created_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransporter_id() {
        return transporter_id;
    }

    public void setTransporter_id(String transporter_id) {
        this.transporter_id = transporter_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getId() {
        return id;
    }

    public String getClient_id() {
        return client_id;
    }

    public double getCost() {
        double totalCost = 0;
        for (Pizza element : products) {
            totalCost += element.getCost();
        }

        return totalCost;
    }

    public ArrayList<Pizza> getProducts() {
        return products;
    }
}


