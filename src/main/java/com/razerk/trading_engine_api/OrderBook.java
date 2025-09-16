package com.razerk.trading_engine_api;

import java.util.*;

public class OrderBook {
    // These should be private, the getters provide access.
    private TreeMap<Double, Queue<Order>> bids;
    private TreeMap<Double, Queue<Order>> asks;

    // Public constructor for when the engine creates a new book from scratch.
    public OrderBook() {
        this.bids = new TreeMap<>(Collections.reverseOrder());
        this.asks = new TreeMap<>();
    }

    // Now that the 'bids' field is a TreeMap, the cast is not needed.
    public synchronized TreeMap<Double, Queue<Order>> getBids() {
        return bids;
    }

    // Same for asks, the cast is not needed.
    public synchronized TreeMap<Double, Queue<Order>> getAsks() {
        return asks;
    }

    // Setters are needed for Jackson to populate the fields from the JSON file.
    public void setBids(TreeMap<Double, Queue<Order>> bids) {
        this.bids = bids;
    }

    public void setAsks(TreeMap<Double, Queue<Order>> asks) {
        this.asks = asks;
    }

    public synchronized void addOrder(Order order) {
        // Use the specific type TreeMap here for consistency.
        TreeMap<Double, Queue<Order>> bookSide = (order.getSide() == Side.BUY) ? bids : asks;

        Queue<Order> ordersAtPrice = bookSide.computeIfAbsent(order.getPrice(), k -> new LinkedList<>());
        ordersAtPrice.add(order);
    }

    public synchronized void printOrderBook(String sym) {
        System.out.println("\n=======================================");
        System.out.println("          ORDER BOOK: " + sym);
        System.out.println("=======================================");

        System.out.println("\n------------ ASKS (SELL) ------------");
        System.out.println("Price\t\t|\tQuantity");
        System.out.println("---------------------------------------");

        NavigableSet<Double> askPrices = asks.navigableKeySet().descendingSet();

        for (Double price : askPrices) {
            int totalQuantity = asks.get(price).stream()
                    .mapToInt(Order::getQuantity)
                    .sum();
            System.out.printf("%.2f\t\t|\t%d\n", price, totalQuantity);
        }

        System.out.println("\n------------- BIDS (BUY) --------------");
        System.out.println("Price\t\t|\tQuantity");
        System.out.println("---------------------------------------");

        for (Double price : bids.keySet()) {
            int totalQuantity = bids.get(price).stream()
                    .mapToInt(Order::getQuantity)
                    .sum();
            System.out.printf("%.2f\t\t|\t%d\n", price, totalQuantity);
        }
        System.out.println("=======================================\n");
    }
}