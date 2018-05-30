package com.nbt.hudat.identity_classes;

import java.util.HashMap;

/**
 * Created by user on 19-Aug-17.
 */

public class Chat {
    public String group;
    public HashMap<String, Boolean> incomingUnreadMessages;
    public String incomingUserId;
    public Boolean isChatDeleted;
    public Boolean isIdRevealed;
    public Boolean isUserRated;
    public String lastMessage;
    public Long lastMessageDate;
    public String outgoingUserId;
    public String outgoingUserProfileImageUrl;
    public String outgoingUsername;

    public Chat(String groupId, String memberId, String uid, String url, String name, Message m) {
        group = groupId;
        incomingUnreadMessages = new HashMap<String, Boolean>();
        incomingUserId = memberId;
        isChatDeleted = false;
        isUserRated = false;
        isIdRevealed = false;
        outgoingUserId = uid;
        outgoingUserProfileImageUrl = url;
        outgoingUsername = name;
        lastMessage = m.content;
        lastMessageDate = m.sentDate;
    }

    public Chat() {

    }
}