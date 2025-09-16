package com.razerk.trading_engine_api;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
@Service
public class TradeEventService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public void addEmitter(SseEmitter emitter) {
        this.emitters.add(emitter);
        // Remove the emitter when it's closed (e.g., client closes browser)
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
    }

    public void sendTradeEvent(Trade trade) {
        // Send the event to all connected clients
        for (SseEmitter emitter : this.emitters) {
            try {
                emitter.send(SseEmitter.event().name("trade").data(trade));
            } catch (IOException e) {
                // Remove the emitter if it's dead
                this.emitters.remove(emitter);
            }
        }
    }
}
