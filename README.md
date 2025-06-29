# Crypto Trading Simulator

A Java desktop application that simulates crypto trading using real BTC/USDC price data from Binance API.

## Features

- **Real-time Price Data**: Fetches 4-hour BTC/USDC price data from Binance API
- **Trading Simulation**: Place buy/sell orders with price and amount specifications
- **Order Management**: View open orders and track executed trades
- **Balance Tracking**: Monitor USDC and BTC balances in real-time
- **Historical Data**: View price charts with historical data
- **Game Progression**: Advance through days to see how your trades perform
- **AI-Powered News**: Get real-time crypto market news analysis using OpenAI API

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- Internet connection (for Binance API access)
- OpenAI API key (optional, for news feature)

## Setup

### OpenAI News Feature (Optional)

To enable the AI-powered news feature:

1. Get an OpenAI API key from [OpenAI Platform](https://platform.openai.com/)
2. Set the environment variable:
   ```bash
   # Windows
   set OPEN_API_KEY=your_api_key_here
   
   # Linux/Mac
   export OPEN_API_KEY=your_api_key_here
   ```

If the API key is not set, the news panel will display: "Not available. Specify the OPEN_API_KEY to have this feature."

## Building the Application

1. Clone or download the project
2. Open a terminal/command prompt in the project directory
3. Run the following command to build the application:

```bash
mvn clean package
```

## Running the Application

After building, run the application using:

```bash
java -jar target/crypto-trading-simulator-1.0.0.jar
```

Or use Maven to run directly:

```bash
mvn exec:java -Dexec.mainClass="com.tradinggame.Main"
```

## How to Play

1. **Start the Game**: Enter your player name, select a date range, and set your initial USDC balance
2. **View Price Chart**: The application loads BTC/USDC prices for the first day and displays them on a chart
3. **Read News**: The news panel shows AI-generated crypto market analysis for the current game date
4. **Place Orders**: 
   - Select order type (Buy/Sell)
   - Enter price in USDC
   - Enter amount in BTC
   - Select the date for the order
   - Click "Place Order"
5. **Advance Days**: Click "Next Day" to load the next day's prices, execute any matching orders, and get new news updates
6. **Monitor Performance**: Watch your balance and open orders update in real-time
7. **Game End**: When you reach the end date, view your final balance and PnL

## Game Rules

- **No Past Orders**: You cannot place orders for dates that have already passed
- **Balance Requirements**: You must have sufficient USDC for buy orders or BTC for sell orders
- **Order Execution**: Orders are executed when the market price reaches your specified price
- **Real Data**: Uses actual historical BTC/USDC price data from Binance
- **Daily News**: News updates automatically when advancing to the next day

## Technical Details

- **Frontend**: Java Swing GUI
- **Charts**: JFreeChart for price visualization
- **API**: Binance REST API for price data
- **AI News**: OpenAI GPT-3.5-turbo for market analysis
- **HTTP Client**: OkHttp for API requests
- **JSON Parsing**: Gson for API response parsing

## Project Structure

```
src/main/java/com/tradinggame/
├── Main.java                 # Application entry point
├── TradingGameFrame.java     # Main GUI window
├── GameState.java           # Game logic and state management
├── Order.java               # Order data model
├── OrderType.java           # Order type enum
├── PriceData.java           # Price data model
├── BinanceApiClient.java    # Binance API integration
├── OpenAiNewsService.java   # OpenAI API integration for news
├── NewsPanel.java           # News display panel
├── StartupDialog.java       # Game initialization dialog
├── PriceChartPanel.java     # Price chart component
├── OrderPanel.java          # Order placement panel
├── BalancePanel.java        # Balance display panel
├── OrdersListPanel.java     # Open orders list panel
└── GameStateListener.java   # Event listener interface
```

## Dependencies

- **OkHttp**: HTTP client for API requests
- **Gson**: JSON parsing library
- **JFreeChart**: Charting library for price visualization
- **OpenAI Java Client**: OpenAI API integration for news generation
- **JUnit**: Testing framework (for development)

## Troubleshooting

- **API Errors**: If Binance API is unavailable, the application will use mock data
- **OpenAI API Errors**: If OpenAI API is unavailable or key is invalid, the news panel will show an error message
- **Date Range**: Ensure your selected date range is valid and not in the future
- **Balance Issues**: Make sure you have sufficient balance for your orders

## License

This project is for educational purposes. Please ensure compliance with Binance API and OpenAI API terms of service when using this application. 