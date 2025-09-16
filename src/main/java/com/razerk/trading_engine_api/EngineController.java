package com.razerk.trading_engine_api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class EngineController {
    private final TradeEventService tradeEventService;
    private final MatchingEngine matchingEngine;

    public EngineController(TradeEventService tradeEventService){
        this.tradeEventService = tradeEventService;
        this.matchingEngine = new MatchingEngine(tradeEventService);
    }

    @PostMapping("/orders")
    public ResponseEntity<String> processOrder(@RequestBody Order newOrder){
        matchingEngine.processOrder(newOrder);
        return ResponseEntity.ok("Order recieved and is being processed");
    }

    @GetMapping("/books/{symbol}")
    public ResponseEntity<OrderBook> viewBook(@PathVariable String symbol){
        OrderBook obook = matchingEngine.getOrderBooks().get(symbol.toUpperCase());
        if(obook != null){
            return ResponseEntity.ok(obook);
        }
        else{
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/trades/stream")
    public SseEmitter streamTrades() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        tradeEventService.addEmitter(emitter);
        return emitter;
    }

    @PostMapping("/shutdown")
    public ResponseEntity<String> shutdown() {
        // A small delay can be added to allow the HTTP response to be sent before shutdown
        new Thread(() -> {
            try {
                Thread.sleep(500); // Wait 500ms
                TradingEngineApiApplication.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        return ResponseEntity.ok("Engine is shutting down gracefully...");
    }
}
