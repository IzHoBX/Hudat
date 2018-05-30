package com.nbt.hudat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.luolc.emojirain.EmojiRainLayout;
import com.nbt.hudat.tools.EmojiGetter;

import java.util.Timer;
import java.util.TimerTask;

import io.github.rockerhieu.emojiconize.Emojiconize;
import me.anwarshahriar.calligrapher.Calligrapher;

public class welcome extends AppCompatActivity {
    public EmojiRainLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Emojiconize.activity(this).go();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Calligrapher cl = new Calligrapher(this);
        cl.setFont(this, "pnsb.ttf", true);

        // bind view
         mContainer = (EmojiRainLayout) findViewById(R.id.group_emoji_container);

        // add emoji sources
        mContainer.addEmoji(R.drawable.emoji_ios_1f31a);

        // set emojis per flow, default 6
        mContainer.setPer(10);

        // set average drop duration in milliseconds, default 2400
        mContainer.setDropDuration(2400);

        // set drop frequency in milliseconds, default 500
        mContainer.setDropFrequency(500);

        mContainer.startDropping();


    }

    protected void signUp(View view) {
        startActivity(new Intent(this, SignInActivity.class));
    }
}
