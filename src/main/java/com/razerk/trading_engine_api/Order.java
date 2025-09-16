package com.razerk.trading_engine_api;

public class Order {
    private long orderId;
    private String stockSymbol;
    private Side side;
    private double price;
    private int quantity;
    private long timestamp;
    private OrderType orderType;

    public Order(long orderId, String stockSymbol, Side side, OrderType orderType, double price, int quantity) {
        this.orderId = orderId;
        this.stockSymbol = stockSymbol;
        this.side = side;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.nanoTime();
    }

    private Order() {
        // Jackson needs this for deserialization
    }

    //Getters for each field
    public long getOrderId(){ return orderId;}
    public String getStockSymbol(){ return stockSymbol;}
    public Side getSide(){ return side;}
    public double getPrice(){ return price;}
    public int getQuantity(){ return quantity;}
    public long getTimestamp(){ return timestamp;}
    public OrderType getOrderType() {
        return orderType;
    }

    //Adding setters
    public void setOrderId(long orderId){ this.orderId = orderId;}
    public void setStockSymbol(String stockSymbol){ this.stockSymbol = stockSymbol;}
    public void setSide(Side side){ this.side = side;}
    public void setPrice(double price){ this.price = price;}
    public void setQuantity(int q){
        this.quantity = q;
    }
    public void setTimestamp(long timestamp){ this.timestamp = timestamp;}
    public void setOrderType(OrderType orderType){ this.orderType = orderType;}

    @Override
    public String toString() {
        return "Order{" + "id=" + orderId + ", side=" + side + ", price=" + price + ", qty=" + quantity + '}';
    }

    //We overrode the toString method to convert our order's details into a single string for easy interpretation
}
