package com.nbt.hudat;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nbt.hudat.identity_classes.GroupLocation;
import com.nbt.hudat.identity_classes.userLocation;

import org.w3c.dom.Text;

import me.anwarshahriar.calligrapher.Calligrapher;

import static android.R.attr.x;

public class Join extends AppCompatActivity {
    private EditText passcode;
    private TextView hint;
    private String ans;
    private Button x;
    private String groupId;
    private boolean inputValid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        //font customisation
        Calligrapher cl = new Calligrapher(this);
        cl.setFont(this, "pnsb.ttf", true);

        x = (Button) findViewById(R.id.button4);
        hint = (TextView) findViewById(R.id.hint);
        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");
        inputValid = false;

        FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("hint").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    hint.setText("");
                    return;
                }
                hint.setText("Hint: "+ dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals("")) {
                    x.setBackground(getDrawable(R.drawable.round));
                    inputValid = true;
                }
                else {
                    x.setBackground(getDrawable(R.drawable.round_grey));
                    inputValid = false;
                }
            }
        };

        passcode = (EditText) findViewById(R.id.passcode);
        passcode.addTextChangedListener(tw);

    }

    protected void join(View view) {
        if(inputValid) {
            FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("passcode").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ans = dataSnapshot.getValue(String.class);
                    if(passcode.getText().toString().equals(ans)) {
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("members").child(uid).setValue(new userLocation(uid));
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("groups").child(groupId).setValue(new GroupLocation(groupId));
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Passcode wrong!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
