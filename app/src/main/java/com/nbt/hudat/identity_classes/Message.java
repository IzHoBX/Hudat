package com.nbt.hudat.identity_classes;

import android.icu.text.SimpleDateFormat;
import android.net.ParseException;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by user on 19-Aug-17.
 */

public class Message {
    public String content;
    public String contentType;
    public String fromId;
    public String groupName;
    public boolean isRead;
    public String messageId;
    public String messageType;
    public String name;
    public String profileImageUrl;
    public Long sentDate;
    public Long sentDay;

    public Message(String content, String contentType, String fromId, String messageId, String messageType, String name, String profileImageUrl, String groupName) {
        this.content = content;
        this.contentType = contentType;
        this.fromId = fromId;
        this.messageId = messageId;
        this.messageType = messageType;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.groupName = groupName;
        isRead = false;
        sentDate = Calendar.getInstance().getTimeInMillis()/1000;
        String timeInString = Long.toString(Calendar.getInstance().getTimeInMillis());
        sentDay = getStartOfDay(timeInString);
    }

    public Message() {

    }

    public long getStartOfDay(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(format.parse(date));
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis()/1000;
    }

    public void setSentDay(Long x) {
        this.sentDay = x;
    }

}
