package com.razerk.trading_engine_api; // Or your package name

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MatchingEngine implements DisposableBean {
    private final AtomicLong orderIdGenerator = new AtomicLong(0);
    private final Map<String, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final TradeEventService tradeEventService;
    private final OrderRepository orderRepository;

    public MatchingEngine(TradeEventService tradeEventService, OrderRepository orderRepository) {
        this.tradeEventService = tradeEventService;
        this.orderRepository = orderRepository;
        loadOrdersFromDatabase();
    }

    private void loadOrdersFromDatabase() {
        System.out.println("Loading resting orders from Database....");
        // FIX: Removed the old package name. This should just be List<Order>.
        List<com.razerk.trading_engine_api.Order> restingOrders = orderRepository.findAll();
        for (Order order : restingOrders) {
            OrderBook relevantBook = orderBooks.computeIfAbsent(order.getStockSymbol(), k -> new OrderBook());
            relevantBook.addOrder(order);
        }
        System.out.println("Loaded " + restingOrders.size() + " orders.");
    }

    @Override
    public void destroy() {
        System.out.println("Matching engine is shutting down. Database state is persistent.");
    }

    public Map<String, OrderBook> getOrderBooks() {
        return this.orderBooks;
    }

    // FIX: Removed the old package name from the parameter.
    public void processOrder(Order newOrder) {
        newOrder.setOrderId(orderIdGenerator.incrementAndGet());
        newOrder.setTimestamp(System.nanoTime());
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

                Trade trade = new Trade(buyOrder.getStockSymbol(), tradeQuantity, bestAskPrice, buyOrder.getOrderId(), sellOrder.getOrderId());
                System.out.println(trade);
                tradeEventService.sendTradeEvent(trade);

                buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
                sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);

                // FIX: Added persistence logic back
                if (sellOrder.getQuantity() == 0) {
                    ordersAtBestAsk.poll();
                    orderRepository.delete(sellOrder); // <-- DELETE from DB
                } else {
                    orderRepository.save(sellOrder); // <-- UPDATE in DB
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
            orderRepository.save(buyOrder); // <-- SAVE to DB
        }
    }

    private void processLimitSellOrder(Order sellOrder, OrderBook orderBook) {
        while (sellOrder.getQuantity() > 0 && !orderBook.getBids().isEmpty()) {
            double bestBidPrice = orderBook.getBids().firstKey();
            if (sellOrder.getPrice() <= bestBidPrice) {
                Queue<Order> ordersAtBestBid = orderBook.getBids().get(bestBidPrice);
                Order buyOrder = ordersAtBestBid.peek();
                int tradeQuantity = Math.min(sellOrder.getQuantity(), buyOrder.getQuantity());

                Trade trade = new Trade(sellOrder.getStockSymbol(), tradeQuantity, bestBidPrice, buyOrder.getOrderId(), sellOrder.getOrderId());
                System.out.println(trade);
                tradeEventService.sendTradeEvent(trade);

                sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);
                buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);

                // FIX: Added persistence logic back
                if (buyOrder.getQuantity() == 0) {
                    ordersAtBestBid.poll();
                    orderRepository.delete(buyOrder); // <-- DELETE from DB
                } else {
                    orderRepository.save(buyOrder); // <-- UPDATE in DB
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
            orderRepository.save(sellOrder); // <-- SAVE to DB
        }
    }

    private void processMarketBuyOrder(Order buyOrder, OrderBook orderBook) {
        while (buyOrder.getQuantity() > 0 && !orderBook.getAsks().isEmpty()) {
            double bestAskPrice = orderBook.getAsks().firstKey();
            Queue<Order> ordersAtBestAsk = orderBook.getAsks().get(bestAskPrice);
            Order sellOrder = ordersAtBestAsk.peek();
            int tradeQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());

            Trade trade = new Trade(buyOrder.getStockSymbol(), tradeQuantity, bestAskPrice, buyOrder.getOrderId(), sellOrder.getOrderId());
            System.out.println(trade);
            tradeEventService.sendTradeEvent(trade);

            buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);
            sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);

            // FIX: Added persistence logic back
            if (sellOrder.getQuantity() == 0) {
                ordersAtBestAsk.poll();
                orderRepository.delete(sellOrder); // <-- DELETE from DB
            } else {
                orderRepository.save(sellOrder); // <-- UPDATE in DB
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

            Trade trade = new Trade(sellOrder.getStockSymbol(), tradeQuantity, bestBidPrice, buyOrder.getOrderId(), sellOrder.getOrderId());
            System.out.println(trade);
            tradeEventService.sendTradeEvent(trade);

            sellOrder.setQuantity(sellOrder.getQuantity() - tradeQuantity);
            buyOrder.setQuantity(buyOrder.getQuantity() - tradeQuantity);

            // FIX: Added persistence logic back
            if (buyOrder.getQuantity() == 0) {
                ordersAtBestBid.poll();
                orderRepository.delete(buyOrder); // <-- DELETE from DB
            } else {
                orderRepository.save(buyOrder); // <-- UPDATE in DB
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