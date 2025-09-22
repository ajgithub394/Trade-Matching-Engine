# High-Performance Order Matching Engine

A real-time, concurrent, multi-stock order matching engine built with Java and Spring Boot. This application simulates the core functionality of a financial exchange, featuring a REST API for interaction and a live Server-Sent Events (SSE) stream for trade notifications.

## Features

- **Concurrent Matching:** Thread-safe engine capable of handling simultaneous order requests.
- **Multiple Order Types:** Supports both LIMIT and MARKET orders.
- **Multi-Stock Support:** Manages separate, independent order books for multiple stock symbols.
- **Real-Time Trade Stream:** Uses Server-Sent Events (SSE) to push executed trades to connected clients instantly.
- **Database Persistence:** Utilizes an H2 file-based database to ensure order book state survives restarts.
- **REST API:** A clean API for submitting orders, viewing order books, and managing the application lifecycle.

## Technologies Used

- Java 17
- Spring Boot
- Spring Web & Spring Data JPA
- Maven
- H2 Database
- Jackson (for JSON processing)

## API Endpoints

| Method | Path                | Body         | Description                      |
| :----- | :------------------ | :----------- | :------------------------------- |
| `POST` | `/orders`           | `Order` JSON | Submits a new order to the engine. |
| `GET`  | `/books/{symbol}`   | (none)       | Gets the current order book for a symbol. |
| `GET`  | `/trades/stream`    | (none)       | Connects to the live SSE trade feed. |
| `POST` | `/shutdown`         | (none)       | Shuts down the server gracefully.  |


Example of a LIMIT type order body :

{
    "stockSymbol": "AAPL",
    "side": "BUY",
    "orderType": "LIMIT",
    "price": 150.75,
    "quantity": 100
}

Example of a MARKET type order body :

{
    "stockSymbol": "GOOG",
    "side": "SELL",
    "orderType": "MARKET",
    "price": 0.0,
    "quantity": 50
}
