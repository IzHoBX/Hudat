package com.nbt.hudat.identity_classes;

import java.util.HashMap;

/**
 * Created by user on 10-Aug-17.
 */

public class User {
    public String email;
    public String intro;
    public String name;
    public String profileImageUrl;
    public HashMap<String, HashMap<String, ChatUnit>> chats;
    public HashMap<String, GroupLocation> groups;
    public HashMap<String, Integer> rating;

    public User(String email, String intro, String name, String url) {
        this.email = email;
        this.intro = intro;
        this.name = name;
        profileImageUrl = url;
    }

    public User() {

    }

    public void addGroup(String groupId) {
        if(groups == null)
            groups = new HashMap<String, GroupLocation>();
        groups.put(groupId, new GroupLocation(groupId));
    }

    public void addRating(String userId, int rating) {
        if(this.rating == null)
            this.rating = new HashMap<String, Integer>();
        this.rating.put(userId, rating);
    }
    
    public void addIncomingChat(String chatId, String groupId) {
        if(chats == null)
            chats = new HashMap<String, HashMap<String, ChatUnit>>();
        if(!chats.containsKey("incoming")) {
            HashMap<String, ChatUnit> incoming = new HashMap<>();
            incoming.put(chatId, new ChatUnit(chatId, groupId));
            chats.put("incoming", incoming);
        }
    }

    public void addOutgoingChat(String chatId, String groupId) {
        if (chats == null)
            chats = new HashMap<String, HashMap<String, ChatUnit>>();
        if (!chats.containsKey("outgoing")) {
            HashMap<String, ChatUnit> outgoing = new HashMap<>();
            outgoing.put(chatId, new ChatUnit(chatId, groupId));
            chats.put("outgoing", outgoing);
        }
    }

    public void leaveGroup(String groupId) {
        groups.remove(groupId);
    }

    public void leaveIncomingChat(String chatId) {
        chats.get("incoming").remove(chatId);
    }

    public void leaveOutgoingChat(String chatId) {
        chats.get("outgoing").remove(chatId);
    }
}
