package com.nbt.hudat.identity_classes;

import static com.nbt.hudat.R.id.group;

/**
 * Created by user on 21-Aug-17.
 */

public class ChatUnit {
    public String chatLocation;
    public String groupLocation;

    public ChatUnit(String chatId, String groupId) {
        chatLocation = chatId;
        groupLocation = groupId;
    }

    public ChatUnit() {

    }
}
