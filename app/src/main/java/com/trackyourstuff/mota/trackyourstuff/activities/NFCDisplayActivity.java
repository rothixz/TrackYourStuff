package com.trackyourstuff.mota.trackyourstuff.activities;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.trackyourstuff.mota.trackyourstuff.R;

import java.util.ArrayList;


public class NFCDisplayActivity extends AppCompatActivity{

    TextView mTextView;
    DatabaseReference transportersRef;
    DatabaseReference ordersRef;
    DatabaseReference histOrdersRef;
    ArrayList<String> orders;
    Parcelable[] rawMessages;
    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    com.google.firebase.database.Query user_transporter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfcdisplay);

        mAuth = FirebaseAuth.getInstance();

        mTextView = (TextView) findViewById(R.id.nfcview);

        // Get transporters Ref
        transportersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users/transporters/");

        // Get orders Ref
        ordersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("orders/");

        // Get orders Ref
        histOrdersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("hist_orders/");

        Log.d("Teste", "onCreate");

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Return button listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onResume(){
        super.onResume();
        Log.d("Teste", "onResume");

        String transporter_id = mAuth.getCurrentUser().getUid();

        Log.d("Teste", "transporter ID" + transporter_id);
        user_transporter = transportersRef.orderByChild("id").equalTo(transporter_id);

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            user_transporter.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("Teste", "onResume - onDataChange");
                    if (dataSnapshot.exists()) {
                        Log.d("Teste", "onResume - snapshot exists");
                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            orders = (ArrayList<String>) issue.child("orders").getValue();
                            Log.d("Teste", "ordem - " + orders.get(0));
                            handleMessage();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else
            mTextView.setText(R.string.nfc_staff);
    }

    public void handleMessage(){

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred

            Log.d("Teste", "action discovered");
            final String order_id = new String(message.getRecords()[0].getPayload());
            Log.d("Teste", "received - " + order_id);
            if(orders.get(0).equals(order_id)){
                final Query order_query = ordersRef.orderByChild("id").equalTo(order_id);
                order_query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                histOrdersRef.child(order_id).child("details").setValue(issue.getValue());
                                Log.d("Teste", order_id);
                            }
                            ordersRef.child(order_id).removeValue();
                            histOrdersRef.child(order_id).child("details").child("status").setValue("delivered");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                mTextView.setText("Delivered");
            }
            else{
                mTextView.setText("Incorrect package");
            }
    }
}