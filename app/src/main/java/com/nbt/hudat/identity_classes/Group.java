package com.nbt.hudat.identity_classes;

import android.net.Uri;

import java.sql.Date;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by user on 09-Aug-17.
 */

public class Group {
    public String name;
    public HashMap<String, userLocation> members;
    public HashMap<String, userLocation> admins;
    public Long creationDate;
    public String description;
    public String groupImageUrl;
    public String passcode;
    public String hint;

    public Group() {

    }

    public Group(String name, String description, String groupImageUrl, String passcode, String hint) {
        this.name = name;
        this.description = description;
        this.groupImageUrl = groupImageUrl;
        this.passcode = passcode;
        members = new HashMap<String, userLocation>();
        admins = new HashMap<String, userLocation>();
        creationDate = Calendar.getInstance().getTimeInMillis()/1000;
        this.hint = hint;
    }

    public boolean containsMember(String uid) {
       if(members == null)
           return false;
        else if(members.containsKey(uid))
           return true;
        else
            return false;
    }

    public void addMember(String uid) {
        if(members == null)
            members = new HashMap<String, userLocation>();
        if(!containsMember(uid))
            members.put(uid, new userLocation(uid));

    }

    public void addAdmin(String uid) {
        if(admins == null)
            admins = new HashMap<String, userLocation>();
        if(!admins.containsKey(uid))
            admins.put(uid, new userLocation(uid));
    }

    public String getName() {
        return name;
    }

    public HashMap<String, userLocation> getMembers() {
        return members;
    }

    public HashMap<String, userLocation> getAdmins() {
        return admins;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupImageUrl() {
        return groupImageUrl;
    }

    public String getPasscode() {
        return passcode;
    }
}
