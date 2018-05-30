package com.nbt.hudat.identity_classes;

import java.util.HashMap;

import static com.nbt.hudat.R.drawable.chat;

/**
 * Created by user on 11-Aug-17.
 */

public class Member {
    public HashMap<String, chatLocation> chatting;
    public userLocation userLocation;

    public Member() {

    }

    public Member(String uid) {
        userLocation = new userLocation(uid);
        chatting = new HashMap<String, chatLocation>();
    }

    public void chatWith(String anotherUid, chatLocation cl) {
        chatting.put(anotherUid, cl);
    }
}
