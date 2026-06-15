package com.chirp.backend.model;

public class ChatBot implements Ketik {

    private ChatbotMessage[] listPesan;
    private String[] input;

    public ChatBot() {
        this.listPesan = new ChatbotMessage[0];
        this.input = new String[0];
    }

    public ChatBot(ChatbotMessage[] listPesan, String[] input) {
        this.listPesan = listPesan;
        this.input = input;
    }

    public String send(String input) {
        return "";
    }

    @Override
    public String typing() {
        return "Chatbot is thinking...";
    }

    // Standard getters and setters
    public ChatbotMessage[] getListPesan() {
        return listPesan;
    }

    public void setListPesan(ChatbotMessage[] listPesan) {
        this.listPesan = listPesan;
    }

    public String[] getInput() {
        return input;
    }

    public void setInput(String[] input) {
        this.input = input;
    }
}
