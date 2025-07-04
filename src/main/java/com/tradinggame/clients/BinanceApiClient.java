package com.tradinggame.clients;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.tradinggame.dtos.PriceData;

public class BinanceApiClient {
    private static final String BASE_URL = "https://api.binance.com/api/v3";
    private static final String INTERVAL = "4h";
    
    private final OkHttpClient client;
    private final Gson gson;
    private final String symbol;

    public BinanceApiClient() {
        this("BTCUSDC");
    }

    public BinanceApiClient(String symbol) {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.symbol = symbol;
    }

    public List<PriceData> getHistoricalPrices(LocalDate date) throws IOException {
        // File-based cache directory per symbol
        Path cacheDir = Paths.get("cache", symbol);
        if (!Files.exists(cacheDir)) {
            Files.createDirectories(cacheDir);
        }
        Path cacheFile = cacheDir.resolve(date.toString() + ".json");
        List<PriceData> prices = new ArrayList<>();
        if (Files.exists(cacheFile)) {
            // Load from cache
            String cachedJson = new String(Files.readAllBytes(cacheFile));
            JsonArray klines = gson.fromJson(cachedJson, JsonArray.class);
            for (JsonElement element : klines) {
                JsonArray kline = element.getAsJsonArray();
                long timestamp = kline.get(0).getAsLong();
                double openPrice = Double.parseDouble(kline.get(1).getAsString());
                double highPrice = Double.parseDouble(kline.get(2).getAsString());
                double lowPrice = Double.parseDouble(kline.get(3).getAsString());
                double closePrice = Double.parseDouble(kline.get(4).getAsString());
                double volume = Double.parseDouble(kline.get(5).getAsString());
                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC);
                prices.add(new PriceData(dateTime, openPrice, highPrice, lowPrice, closePrice, volume));
            }
            return prices;
        }
        // Convert date to start and end timestamps (UTC)
        long startTime = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
        long endTime = date.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000;
        
        String url = String.format("%s/klines?symbol=%s&interval=%s&startTime=%d&endTime=%d&limit=6",
                BASE_URL, symbol, INTERVAL, startTime, endTime);
        
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response);
            }

            String responseBody = response.body().string();
            // Save to cache
            Files.write(cacheFile, responseBody.getBytes());
            JsonArray klines = gson.fromJson(responseBody, JsonArray.class);
            
            for (JsonElement element : klines) {
                JsonArray kline = element.getAsJsonArray();
                long timestamp = kline.get(0).getAsLong();
                double openPrice = Double.parseDouble(kline.get(1).getAsString());
                double highPrice = Double.parseDouble(kline.get(2).getAsString());
                double lowPrice = Double.parseDouble(kline.get(3).getAsString());
                double closePrice = Double.parseDouble(kline.get(4).getAsString());
                double volume = Double.parseDouble(kline.get(5).getAsString());
                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC);
                prices.add(new PriceData(dateTime, openPrice, highPrice, lowPrice, closePrice, volume));
            }
            
            return prices;
        }
    }

    public double getCurrentPrice() throws IOException {
        String url = String.format("%s/ticker/price?symbol=%s", BASE_URL, symbol);
        
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