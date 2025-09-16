package com.razerk.trading_engine_api;

public class Trade {
    private String stockSymbol;
    private int quantity;
    private double price;
    private long buyOrderId;
    private long sellOrderId;

    // This constructor is for when your engine creates a new trade.
    public Trade(String stockSymbol, int quantity, double price, long buyOrderId, long sellOrderId) {
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
    }

    // This empty constructor is for Jackson to use when loading from a file.
    private Trade() {
    }

    // --- Getters ---
    public String getStockSymbol() { return stockSymbol; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public long getBuyOrderId() { return buyOrderId; }
    public long getSellOrderId() { return sellOrderId; }

    // --- Setters (Needed for Jackson) ---
    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setBuyOrderId(long buyOrderId) {
        this.buyOrderId = buyOrderId;
    }
    public void setSellOrderId(long sellOrderId) {
        this.sellOrderId = sellOrderId;
    }

    @Override
    public String toString() {
        return "TRADE EXECUTED : " + quantity + " shares of " + stockSymbol + " @ " + price;
    }
}