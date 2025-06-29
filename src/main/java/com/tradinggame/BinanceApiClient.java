package com.tradinggame;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class BinanceApiClient {
    private static final String BASE_URL = "https://api.binance.com/api/v3";
    private static final String SYMBOL = "BTCUSDC";
    private static final String INTERVAL = "4h";
    
    private final OkHttpClient client;
    private final Gson gson;

    public BinanceApiClient() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public List<PriceData> getHistoricalPrices(LocalDate date) throws IOException {
        // Convert date to start and end timestamps (UTC)
        long startTime = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
        long endTime = date.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
        
        String url = String.format("%s/klines?symbol=%s&interval=%s&startTime=%d&endTime=%d&limit=6",
                BASE_URL, SYMBOL, INTERVAL, startTime, endTime);
        
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }

            String responseBody = response.body().string();
            JsonArray klines = gson.fromJson(responseBody, JsonArray.class);
            
            List<PriceData> prices = new ArrayList<>();
            for (JsonElement element : klines) {
                JsonArray kline = element.getAsJsonArray();
                long timestamp = kline.get(0).getAsLong();
                double openPrice = Double.parseDouble(kline.get(1).getAsString());
                double highPrice = Double.parseDouble(kline.get(2).getAsString());
                double lowPrice = Double.parseDouble(kline.get(3).getAsString());
                double closePrice = Double.parseDouble(kline.get(4).getAsString());
                
                // Use close price for simplicity
                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC);
                prices.add(new PriceData(dateTime, closePrice));
            }
            
            return prices;
        }
    }

    public double getCurrentPrice() throws IOException {
        String url = String.format("%s/ticker/price?symbol=%s", BASE_URL, SYMBOL);
        
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }

            String responseBody = response.body().string();
            JsonObject ticker = gson.fromJson(responseBody, JsonObject.class);
            return Double.parseDouble(ticker.get("price").getAsString());
        }
    }
} 