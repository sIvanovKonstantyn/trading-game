package com.tradinggame;

import java.util.Random;

public class NewsService {
    private static final String[] NEWS_TEMPLATES = {
        "Bitcoin reaches new milestone as institutional adoption continues to grow. Major financial institutions are increasingly recognizing the potential of cryptocurrency as a store of value.",
        
        "Market analysts predict increased volatility in the crypto market following recent regulatory announcements. Traders are advised to monitor key support and resistance levels.",
        
        "Ethereum network upgrade shows promising results with improved transaction speeds and reduced gas fees. This development could positively impact the broader crypto ecosystem.",
        
        "Central bank digital currency (CBDC) developments in major economies are creating both opportunities and challenges for traditional cryptocurrencies. Market sentiment remains mixed.",
        
        "Crypto mining operations are adapting to new environmental regulations. Green energy initiatives in the sector are gaining momentum as sustainability becomes a priority.",
        
        "DeFi protocols continue to innovate with new yield farming opportunities and lending platforms. However, users are cautioned to conduct thorough research before investing.",
        
        "Technical analysis suggests Bitcoin is forming a bullish pattern on the daily chart. Key indicators point to potential upward momentum in the coming weeks.",
        
        "Market sentiment analysis reveals growing interest from retail investors. Social media activity around cryptocurrency has increased by 25% in the last month.",
        
        "Regulatory clarity in major markets is expected to provide a more stable foundation for crypto trading. Industry leaders welcome the development of clear guidelines.",
        
        "Institutional investment in cryptocurrency has reached new heights, with major hedge funds allocating significant portions of their portfolios to digital assets.",
        
        "The correlation between traditional markets and cryptocurrency appears to be decreasing, suggesting crypto is becoming a more independent asset class.",
        
        "New blockchain technologies are emerging that promise to solve scalability issues. These developments could lead to wider adoption of cryptocurrency for everyday transactions.",
        
        "Market volatility has created opportunities for both long-term investors and day traders. Risk management strategies are more important than ever in the current environment.",
        
        "Crypto exchanges are reporting record trading volumes as market activity increases. User registrations have surged by 40% compared to the previous quarter.",
        
        "Environmental concerns about cryptocurrency mining are being addressed through innovative solutions. Renewable energy sources are becoming more prevalent in mining operations."
    };
    
    private static final String[] MARKET_SENTIMENTS = {
        "Bullish sentiment dominates the market as Bitcoin shows strong momentum.",
        "Bearish pressure continues as traders remain cautious about market direction.",
        "Neutral market conditions prevail with mixed signals from technical indicators.",
        "High volatility expected as key resistance levels are tested.",
        "Market consolidation phase continues with limited price movement."
    };
    
    private final Random random = new Random();
    
    public String getRandomNews() {
        String baseNews = NEWS_TEMPLATES[random.nextInt(NEWS_TEMPLATES.length)];
        String sentiment = MARKET_SENTIMENTS[random.nextInt(MARKET_SENTIMENTS.length)];
        
        return baseNews + "\n\n" + sentiment;
    }
    
    public String getMarketUpdate() {
        return "Market Update: " + getRandomNews();
    }
    
    public String getTechnicalAnalysis() {
        String[] analysis = {
            "RSI indicates oversold conditions, potential reversal expected.",
            "Bollinger Bands show price approaching upper band, resistance likely.",
            "Moving averages suggest bullish crossover, upward momentum building.",
            "Volume analysis shows decreasing participation, consolidation phase.",
            "Support levels holding strong, bullish continuation pattern forming."
        };
        
        return "Technical Analysis: " + analysis[random.nextInt(analysis.length)];
    }
} 