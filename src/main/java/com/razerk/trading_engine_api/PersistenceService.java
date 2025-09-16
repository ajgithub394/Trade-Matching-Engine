package com.razerk.trading_engine_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceService {
    private static final String FILE_PATH = "orderbooks.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveState(Map<String, OrderBook> orderBooks) {
        try {
            // Write the entire map of order books to the JSON file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), orderBooks);
            System.out.println("Engine state saved to " + FILE_PATH);
        } catch (IOException e) {
            System.err.println("Error saving state: " + e.getMessage());
        }
    }

    public Map<String, OrderBook> loadState() {
        File stateFile = new File(FILE_PATH);
        if (stateFile.exists()) {
            try {
                // Read the JSON file and convert it back into our Map
                Map<String, OrderBook> loadedBooks = objectMapper.readValue(stateFile,
                        objectMapper.getTypeFactory().constructMapType(ConcurrentHashMap.class, String.class, OrderBook.class));
                System.out.println("Engine state loaded from " + FILE_PATH);
                return loadedBooks;
            } catch (IOException e) {
                System.err.println("Error loading state: " + e.getMessage());
            }
        }
        // If file doesn't exist or there was an error, return a new empty map
        return new ConcurrentHashMap<>();
    }
}
