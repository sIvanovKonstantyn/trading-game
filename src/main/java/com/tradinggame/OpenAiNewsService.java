package com.tradinggame;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OpenAiNewsService {
    private final OpenAiService openAiService;
    private final boolean isApiKeyAvailable;
    
    public OpenAiNewsService() {
        String apiKey = System.getenv("OPEN_API_KEY");
        System.out.println("DEBUG: OPEN_API_KEY value: " + (apiKey != null ? "SET (length: " + apiKey.length() + ")" : "NULL"));
        this.isApiKeyAvailable = apiKey != null && !apiKey.trim().isEmpty();
        System.out.println("DEBUG: isApiKeyAvailable: " + this.isApiKeyAvailable);
        
        if (this.isApiKeyAvailable) {
            this.openAiService = new OpenAiService(apiKey);
            System.out.println("DEBUG: OpenAiService initialized successfully");
        } else {
            this.openAiService = null;
            System.out.println("DEBUG: OpenAiService not initialized - no API key");
        }
    }
    
    public boolean isApiKeyAvailable() {
        return isApiKeyAvailable;
    }
    
    public String generateCryptoNews(LocalDate currentDate) {
        if (!isApiKeyAvailable) {
            return "Not available. Specify the OPEN_API_KEY to have this feature.";
        }
        
        try {
            String formattedDate = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String prompt = String.format(
                "Make the news analyze that affect the cryptomarket and show the result with dates in the table view. Date: %s.",
                formattedDate
            );
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(new ChatMessage("user", prompt)))
                .maxTokens(500)
                .temperature(0.7)
                .build();
            
            String response = openAiService.createChatCompletion(request)
                .getChoices().get(0).getMessage().getContent();
            
            return response;
            
        } catch (Exception e) {
            System.err.println("Error calling OpenAI API: " + e.getMessage());
            return "Error generating news. Please check your API key and internet connection.";
        }
    }
} 