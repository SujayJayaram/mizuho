package com.mizuho;

public class Order {
    private long id; // id of order
    private double price;
    private char side; // B "Bid" or O "Offer"
    private long size;

    public Order(long id, double price, char side, long size) {
        this.id = id ;
        this.price = price ;
        this.size = size ;
        this.side = side ;
    }

    public long getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public long getSize() {
        return size;
    }

    public char getSide() {
        return side;
    }
}