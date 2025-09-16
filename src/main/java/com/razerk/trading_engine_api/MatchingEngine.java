package com.razerk.trading_engine_api;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MatchingEngine {
    private final Map<String, OrderBook> orderBooks;
    private final PersistenceService persistenceService = new PersistenceService();
    private final TradeEventService tradeEventService;
    public MatchingEngine(TradeEventService tradeEventService) {
        this.tradeEventService = tradeEventService;
        this.orderBooks = persistenceService.loadState();
    }

    public Map<String, OrderBook> getOrderBooks() {
        return this.orderBooks;
    }

    public void processOrder(Order newOrder) {
        String sym = newOrder.getStockSymbol();
        OrderBook relevantBook = orderBooks.computeIfAbsent(sym, k -> new OrderBook());

        synchronized (relevantBook) {
            if (newOrder.getOrderType() == OrderType.LIMIT) {
                if (newOrder.getSide() == Side.BUY) {
                    processLimitBuyOrder(newOrder, relevantBook);
                } else {
                    processLimitSellOrder(newOrder, relevantBook);
                }
            } else { // It's a MARKET order
                if (newOrder.getSide() == Side.BUY) {
                    processMarketBuyOrder(newOrder, relevantBook);
                } else {
                    processMarketSellOrder(newOrder, relevantBook);
                }
            }
        }
    }

    private void processLimitBuyOrder(Order buyOrder, OrderBook orderBook) {
        while (buyOrder.getQuantity() > 0 && !orderBook.getAsks().isEmpty()) {
            double bestAskPrice = orderBook.getAsks().firstKey();
            if (buyOrder.getPrice() >= bestAskPrice) {
                Queue<Order> ordersAtBestAsk = orderBook.getAsks().get(bestAskPrice);
                Order sellOrder = ordersAtBestAsk.peek();
                int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

                // --- For streaming the trade ---
                Trade trade = new Trade(buyOrder.getStockSymbol(), tradeQuantity, bestAskPrice, buyOrder.getOrderId(), sellOrder.getOrderId());
                System.out.println(trade);
                tradeEventService.sendTradeEvent(trade);
                // -------------------------

                buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
                sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);

                if (sellOrder.getQuantity() == 0) {
                    ordersAtBestAsk.poll();
                }
                if (ordersAtBestAsk.isEmpty()) {
                    orderBook.getAsks().remove(bestAskPrice);
                }
            } else {
                break;
            }
        }
        if (buyOrder.getQuantity() > 0) {
            orderBook.addOrder(buyOrder);
        }
    }

    private void processLimitSellOrder(Order sellOrder, OrderBook orderBook) {
        while (sellOrder.getQuantity() > 0 && !orderBook.getBids().isEmpty()) {
            double bestBidPrice = orderBook.getBids().firstKey();
            if (sellOrder.getPrice() <= bestBidPrice) {
                Queue<Order> ordersAtBestBid = orderBook.getBids().get(bestBidPrice);
                Order buyOrder = ordersAtBestBid.peek();
                int tradeQuantity = Math.min(sellOrder.getQuantity(), buyOrder.getQuantity());

                // ------
                Trade trade = new Trade(sellOrder.getStockSymbol(), tradeQuantity, bestBidPrice, buyOrder.getOrderId(), sellOrder.getOrderId());
                System.out.println(trade);
                tradeEventService.sendTradeEvent(trade);
                // -------------------------

                sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);
                buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);

                if (buyOrder.getQuantity() == 0) {
                    ordersAtBestBid.poll();
                }
                if (ordersAtBestBid.isEmpty()) {
                    orderBook.getBids().remove(bestBidPrice);
                }
            } else {
                break;
            }
        }
        if (sellOrder.getQuantity() > 0) {
            orderBook.addOrder(sellOrder);
        }
    }

    private void processMarketBuyOrder(Order buyOrder, OrderBook orderBook) {
        while (buyOrder.getQuantity() > 0 && !orderBook.getAsks().isEmpty()) {
            double bestAskPrice = orderBook.getAsks().firstKey();
            Queue<Order> ordersAtBestAsk = orderBook.getAsks().get(bestAskPrice);
            Order sellOrder = ordersAtBestAsk.peek();
            int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

            // --- ---
            Trade trade = new Trade(buyOrder.getStockSymbol(), tradeQuantity, bestAskPrice, buyOrder.getOrderId(), sellOrder.getOrderId());
            System.out.println(trade);
            tradeEventService.sendTradeEvent(trade);
            // -------------------------

            buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
            sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);

            if (sellOrder.getQuantity() == 0) {
                ordersAtBestAsk.poll();
            }
            if (ordersAtBestAsk.isEmpty()) {
                orderBook.getAsks().remove(bestAskPrice);
            }
        }
    }

    private void processMarketSellOrder(Order sellOrder, OrderBook orderBook) {
        while (sellOrder.getQuantity() > 0 && !orderBook.getBids().isEmpty()) {
            double bestBidPrice = orderBook.getBids().firstKey();
            Queue<Order> ordersAtBestBid = orderBook.getBids().get(bestBidPrice);
            Order buyOrder = ordersAtBestBid.peek();
            int tradeQuantity = Math.min(sellOrder.getQuantity(), buyOrder.getQuantity());

            // --- ---
            Trade trade = new Trade(sellOrder.getStockSymbol(), tradeQuantity, bestBidPrice, buyOrder.getOrderId(), sellOrder.getOrderId());
            System.out.println(trade);
            tradeEventService.sendTradeEvent(trade);
            // -------------------------

            sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);
            buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);

            if (buyOrder.getQuantity() == 0) {
                ordersAtBestBid.poll();
            }
            if (ordersAtBestBid.isEmpty()) {
                orderBook.getBids().remove(bestBidPrice);
            }
        }
    }

    public void printCurrentOrderBook(String sym) {
        OrderBook bookToPrint = orderBooks.get(sym);
        if (bookToPrint != null) {
            bookToPrint.printOrderBook(sym);
        } else {
            System.out.println("--- NO ORDERS FOR SYMBOL: " + sym + " ---");
        }
    }
}