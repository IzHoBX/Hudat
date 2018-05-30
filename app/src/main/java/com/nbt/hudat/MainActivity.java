package com.nbt.hudat;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nbt.hudat.identity_classes.Group;
import com.nbt.hudat.tools.ChatRecyclerAdapter;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import io.github.rockerhieu.emojiconize.Emojiconize;
import me.anwarshahriar.calligrapher.Calligrapher;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.R.attr.fragment;
import static android.R.attr.x;

public class MainActivity extends AppCompatActivity {

    private BottomBar bottomBar;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    //account details
    private String mUsername;
    private String mPhotoUrl;

    //final
    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";
    public boolean doneIntialLoadf;
    public boolean doneIntialLoadg;
    public boolean doneInitialLoadh;
    public Tab1Groups f;
    public Tab2Chats g;
    public Tab3Profile h;
    public ArrayList<Group> groups;
    public int initialLoadNumGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Emojiconize.activity(this).go();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("pnsb.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        doneIntialLoadf = false;
        doneInitialLoadh = false;
        doneIntialLoadg = false;

        BottomBar bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if(tabId == R.id.tab_groups) {
                    if(!doneIntialLoadf) {
                        f = new Tab1Groups();
                        f.setRetainInstance(true);
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, f).commit();
                    Log.i("finised", "commit()");
                }
                else if(tabId == R.id.tab_chats) {
                    if(!doneIntialLoadg) {
                        g = new Tab2Chats();
                        g.setRetainInstance(true);
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, g).commit();
                    Log.i("finised", "commit()");
                }
                else if (tabId == R.id.tab_profile) {
                        h = new Tab3Profile();
                        h.setRetainInstance(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, h).commit();
                    Log.i("finised", "commit()");
                }
            }
        });


        mUsername = ANONYMOUS;

        //to manage FirebaseAuth
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, welcome.class));
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("pnsb.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        groups = new ArrayList<Group>();

    }
    //deleted PlaceholderFragment class

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void create(View view) {
        startActivity(new Intent(this, createGroup.class));
    }

    protected void selectGroup(View view) {
        Intent i = new Intent(this, GroupDetails.class);
        i.putExtra("groupId", view.getTag().toString());
        startActivityForResult(i, 1);
    }

    protected void discover(View view) {
        startActivity(new Intent(this, Discover.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String backStateName = f.getClass().getName();
            boolean fragmentPopped = manager.popBackStackImmediate(backStateName, 0);

            if (!fragmentPopped) { //fragment not in back stack, create it.
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.frame, f);
                ft.addToBackStack(backStateName);
                ft.commit();

            }
        }
    }

    protected void selectChat(View view) {
        Intent i = new Intent(this, ChatRoom.class);
        i.putExtra("chatId", (String) view.getTag());
        i.putExtra("isOutgoing", true);
        i.putExtra("groupName", ((TextView) view.findViewById(R.id.group)).getText().toString());
        startActivity(i);
    }

    protected void selectIncomingChat(View view) {
        Intent i = new Intent(this, ChatRoom.class);
        i.putExtra("chatId", (String) view.getTag());
        i.putExtra("isOutgoing", false);
        i.putExtra("groupName", ((TextView) view.findViewById(R.id.group)).getText().toString());
        startActivity(i);
    }

    protected void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), welcome.class));
    }
}
