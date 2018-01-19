package com.flipkart.springyheads.demo;

import android.content.Context;
import android.os.AsyncTask;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class PremiumAssistant {


    public static final PremiumAssistant INSTANCE;

    static {
        INSTANCE = new PremiumAssistant();
    }

    private final AIConfiguration config = new AIConfiguration("e33b384a6e5d49a3abb419c6f7b6c34f",
            AIConfiguration.SupportedLanguages.English, AIConfiguration.RecognitionEngine.System);

    public void talk(final Context context, String username, final String message, final ReceiveMessageCallback callback) {

        final AIService aiService = AIService.getService(context, config);

        new AsyncTask<Object, Void, AIResponse>() {

            @Override
            protected AIResponse doInBackground(Object... params) {
                AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(message);
                try {
                    return aiService.textRequest(aiRequest);
                } catch (AIServiceException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                callback.receive(aiResponse.getResult().getFulfillment().getSpeech());
            }
        }.execute(new Object[0]);
    }

    public interface ReceiveMessageCallback {

        void receive(String message);

    }
}
