# Crypto Trading Simulator

A Java desktop application that simulates crypto trading using real price data from Binance API. Now with advanced technical indicators, leaderboard, open deals, and more!

## Features

- **Real-time Price Data**: Fetches 4-hour price data for BTC/USDC, ETH/USDC, BNB/USDC from Binance API (with file-based cache)
- **Trading Simulation**: Place buy/sell orders with price and amount (BTC or USDC) specifications
- **Order Management**: View, cancel ("X" button), and track open/executed orders
- **Open Deals Tab**: Track open buy deals, manually mark as completed, and see PnL for each deal
- **Balance Tracking**: Monitor USDC and crypto balances in real-time
- **Technical Indicators**: Toggle RSI, Bollinger Bands, and Volume charts above the price chart; consistent TradingView-like white theme
- **Historical Data**: View price charts with historical and indicator data
- **Game Progression**: Advance through days to see how your trades perform
- **Trading Fee**: Configurable trading fee (default 0.01%) applied to each transaction
- **Leaderboard**: Results saved to `leaderboard.txt` and shown in a sortable table by PnL
- **Loading Screen**: Fun loading messages and progress bar during data load
- **New Game Button**: Instantly restart the game from the results screen

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- Internet connection (for Binance API access)

## Setup

### Building the Application

1. Clone or download the project
2. Open a terminal/command prompt in the project directory
3. Run the following command to build the application:

```bash
mvn clean package
```

### Running the Application

After building, run the application using:

```bash
java -jar target/crypto-trading-simulator-1.0.0.jar
```

Or use Maven to run directly:

```bash
mvn exec:java -Dexec.mainClass="com.tradinggame.Main"
```

## How to Play

1. **Start the Game**: Enter your player name, select a date range, set your initial USDC balance, and configure the trading fee
2. **View Price Chart**: The application loads prices for the first day and displays them on a chart with selectable indicators
3. **Place Orders**: 
   - Select order type (Buy/Sell)
   - Enter price in USDC
   - Enter amount in BTC or USDC (auto-conversion)
   - Select the date for the order
   - Click "Place Order"
4. **Open Deals**: Track open buy deals, mark as completed, and enter close price to see PnL
5. **Advance Days**: Click "Next Day" to load the next day's prices, execute any matching orders
6. **Monitor Performance**: Watch your balance, open orders, and open deals update in real-time
7. **Game End**: When you reach the end date, view your final balance, PnL, completed deals, and the leaderboard
8. **Restart**: Use the "New Game" button to start over instantly

## Game Rules

- **No Past Orders**: You cannot place orders for dates that have already passed
- **Balance Requirements**: You must have sufficient USDC for buy orders or crypto for sell orders
- **Order Execution**: Orders are executed when the market price reaches your specified price
- **Trading Fee**: Each transaction applies the configured fee
- **Real Data**: Uses actual historical price data from Binance (with file-based cache)

## Technical Details

- **Frontend**: Java Swing GUI
- **Charts**: JFreeChart for price/indicator visualization
- **API**: Binance REST API for price data
- **HTTP Client**: OkHttp for API requests
- **JSON Parsing**: Gson for API response parsing
- **File Cache**: Price data cached in `cache/SYMBOL/YYYY-MM-DD.json`
- **Leaderboard**: Results saved to `leaderboard.txt` and loaded in the UI
- **Utils**: Common logic in `com.tradinggame.utils` (dialogs, file I/O, tables, order math)

## Project Structure

```
src/main/java/com/tradinggame/
├── Main.java                 # Application entry point
├── clients/                  # Binance API client
├── dtos/                     # Data transfer objects (Order, PriceData, etc.)
├── indicators/               # Technical indicators (RSI, Bollinger, etc.)
├── state/                    # Game and symbol state management
├── ui/                       # All Swing UI panels and dialogs
├── utils/                    # Utility classes (DialogUtils, FileUtils, TableUtils, OrderUtils)
```

## Dependencies

- **OkHttp**: HTTP client for API requests
- **Gson**: JSON parsing library
- **JFreeChart**: Charting library for price/indicator visualization
- **Apache Commons Math**: Technical indicator calculations

## Troubleshooting

- **API Errors**: If Binance API is unavailable, the application will use mock data
- **Date Range**: Ensure your selected date range is valid and not in the future
- **Balance Issues**: Make sure you have sufficient balance for your orders
- **Cache Issues**: If price data is missing, check the `cache/` directory for correct files
- **Leaderboard Issues**: Results are saved in `leaderboard.txt` in the project root

## License

This project is for educational purposes. Please ensure compliance with Binance API terms of service when using this application. 