package com.wmjmc.reactspeech;

import android.content.Intent;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class VoiceModule extends ReactContextBaseJavaModule {

    private SpeechRecognizer speechRecognizer;
    private final ReactApplicationContext reactContext;

    public VoiceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "SpeechAndroid";
    }

    @Override
    public Map<String, Object> getConstants() {
        return Constants.getConstants();
    }

    @ReactMethod
    public void startSpeech(final String prompt, final String locale) {
        reactContext.getCurrentActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLocale(locale));
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getPrompt(prompt));
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                CustomRecognitionListener listener = new CustomRecognitionListener();
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(reactContext);
                speechRecognizer.setRecognitionListener(listener);
                speechRecognizer.startListening(intent);
            }
        });
    }

    private String getPrompt(String prompt){
        if(prompt != null && !prompt.equals("")){
            return prompt;
        }

        return "Say something";
    }

    private String getLocale(String locale){
        if(locale != null && !locale.equals("")){
            return locale;
        }

        return Locale.getDefault().toString();
    }

    class CustomRecognitionListener implements RecognitionListener {

        public void onReadyForSpeech(Bundle params) {

        }

        public void onBeginningOfSpeech() {

        }

        public void onRmsChanged(float rmsdB) {

        }

        public void onBufferReceived(byte[] buffer) {

        }

        public void onEndOfSpeech() {

        }

        public void onError(int error) {
            WritableMap params = Arguments.createMap();
            if(error == 7) {
                params.putString("text", "");
                params.putBoolean("isFinal", true);
            } else {
                params.putString("text", "");
                params.putBoolean("isFinal", true);
                params.putInt("error", error);
            }

            sendEvent(reactContext, "onSpeechAndroidResults", params);
            speechRecognizer.destroy();
        }

        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            WritableMap params = Arguments.createMap();
            params.putString("text", matches.get(0));
            params.putBoolean("isFinal", true);
            sendEvent(reactContext, "onSpeechAndroidResults", params);
            speechRecognizer.destroy();
        }

        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            WritableMap params = Arguments.createMap();
            params.putString("text", matches.get(0));
            params.putBoolean("isFinal", false);
            sendEvent(reactContext, "onSpeechAndroidResults", params);
        }

        public void onEvent(int eventType, Bundle params) {

        }
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
}
