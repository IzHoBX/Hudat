package com.nbt.hudat;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import me.anwarshahriar.calligrapher.Calligrapher;

public class invite extends AppCompatActivity {
    private String name;
    private String passcode;
    private Boolean justCreated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        //font customisation
        Calligrapher cl = new Calligrapher(this);
        cl.setFont(this, "pnsb.ttf", true);

        Intent i = getIntent();
        name = i.getStringExtra("name");
        passcode = i.getStringExtra("passcode");
        justCreated = i.getBooleanExtra("justCreated", false);
    }

    protected void whatsapp(View view) {
        PackageManager pm=getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Yo! I would like to invite you to this Hudat Group. The group name is '" + name + "' and the passcode is '" + passcode + "'.";

            PackageInfo info=pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.whatsapp");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    protected void messenger(View view) {
        PackageManager pm=getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Yo! I would like to invite you to this Hudat Group. The group name is '" + name + "' and the passcode is '" + passcode + "'.";

            PackageInfo info=pm.getPackageInfo("com.facebook.orca", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("com.facebook.orca");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Messenger not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    protected void telegram(View view) {
        PackageManager pm=getPackageManager();
        try {

            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            String text = "Yo! I would like to invite you to this Hudat Group. The group name is '" + name + "' and the passcode is '" + passcode + "'.";

            PackageInfo info=pm.getPackageInfo("org.telegram.messenger", PackageManager.GET_META_DATA);
            //Check if package exists or not. If not then code
            //in catch block will be called
            waIntent.setPackage("org.telegram.messenger");

            waIntent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(waIntent, "Share with"));

        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Telegram not Installed", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    protected void done(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onBackPressed() {
        if(justCreated)
            startActivity(new Intent(this, MainActivity.class));
        else
            super.onBackPressed();
    }

}
