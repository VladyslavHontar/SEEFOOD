package com.example.seefood;

import android.os.Build;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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"; // Replace with your OpenAI API key

    public static void main(String[] args) {
        try {
            String imagePath = "path_to_your_image.jpg"; // Replace with the path to your image
            String base64Image = encodeImageToBase64(imagePath);

            JSONObject messageContent = new JSONObject();
            messageContent.put("type", "text");
            messageContent.put("text", "Is this hot-dog or not hot-dog? (answer with 'hot-dog' or 'not hot-dog')");

            JSONObject imageContent = new JSONObject();
            imageContent.put("type", "image_url");
            JSONObject imageUrl = new JSONObject();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
            imageContent.put("image_url", imageUrl);

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "user").put("content", messageContent));
            messages.put(new JSONObject().put("role", "user").put("content", imageContent));

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-4o-mini");
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 300);

            String response = sendPostRequest(API_URL, requestBody.toString());
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encodeImageToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[(int) file.length()];
        fis.read(byteArray);
        fis.close();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(byteArray);
        }
        return null;
    }

    private static String sendPostRequest(String apiUrl, String jsonInputString) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
}