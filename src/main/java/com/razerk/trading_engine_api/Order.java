package com.razerk.trading_engine_api;

// --- ADD THESE IMPORTS ---
import com.razerk.trading_engine_api.OrderType;
import com.razerk.trading_engine_api.Side;
import jakarta.persistence.*;

@Entity // <-- Tells JPA this class maps to a database table
@Table(name = "orders") //avoids the collision with the sql keyword order
public class Order {
    @Id // <-- Tells JPA that orderId is the primary key
    private long orderId;
    private String stockSymbol;
    @Enumerated(EnumType.STRING) // <-- Stores the enum as a readable string ("BUY" or "SELL")
    private Side side;
    private double price;
    private int quantity;
    private long timestamp;
    @Enumerated(EnumType.STRING) // <-- Stores the enum as a readable string ("LIMIT" or "MARKET")
    private OrderType orderType;

    // Your constructors, getters, and setters are perfect and do not need to change.
    public Order(long orderId, String stockSymbol, Side side, OrderType orderType, double price, int quantity) {
        this.orderId = orderId;
        this.stockSymbol = stockSymbol;
        this.side = side;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.nanoTime();
    }

    private Order() { }

    // Getters
    public long getOrderId() { return orderId; }
    public String getStockSymbol() { return stockSymbol; }
    public Side getSide() { return side; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }
    public OrderType getOrderType() { return orderType; }

    // Setters
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public void setStockSymbol(String stockSymbol) { this.stockSymbol = stockSymbol; }
    public void setSide(Side side) { this.side = side; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int q) { this.quantity = q; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    @Override
    public String toString() {
        return "Order{" + "id=" + orderId + ", side=" + side + ", price=" + price + ", qty=" + quantity + '}';
    }
}