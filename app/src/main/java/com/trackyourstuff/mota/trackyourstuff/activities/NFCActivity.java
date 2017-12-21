package com.trackyourstuff.mota.trackyourstuff.activities;

import android.nfc.NfcEvent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.Client;

public class NFCActivity extends AppCompatActivity implements NfcAdapter.OnNdefPushCompleteCallback{
    private Toolbar toolbar;
    TextView textfield;
    String message;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;
    int rating;
    NfcAdapter mAdapter;
    private static final int MESSAGE_SENT = 1;

    DatabaseReference ordersRef;
    DatabaseReference histOrdersRef;
    Client client;
    com.google.firebase.database.Query order_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        textfield = (TextView) findViewById(R.id.text_field);

        // Getting client
        client = getIntent().getExtras().getParcelable("Client");

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

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            textfield.setText("Sorry this device does not have NFC.");
            return;
        }

        if (!mAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable NFC via Settings.", Toast.LENGTH_LONG).show();
        }

        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);


        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rating = 1;
                updateAfterUserChoice();
                histOrdersRef.child(message).child("rating").setValue(rating);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rating = 2;
                updateAfterUserChoice();
                histOrdersRef.child(message).child("rating").setValue(rating);
            }
        });
        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rating = 3;
                updateAfterUserChoice();
                histOrdersRef.child(message).child("rating").setValue(rating);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rating = 4;
                histOrdersRef.child(message).child("rating").setValue(rating);
                updateAfterUserChoice();
            }
        });
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rating = 5;
                histOrdersRef.child(message).child("rating").setValue(rating);
                updateAfterUserChoice();
            }
        });

        order_id = ordersRef.orderByChild("client_id").equalTo(client.getId());

        order_id.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("TESTE", "Existe snpashot");
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        message = issue.child("id").getValue().toString();

                        mAdapter.setNdefPushMessage(createNdefMessage(), NFCActivity.this);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAdapter.setOnNdefPushCompleteCallback(this, this);

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

    public NdefMessage createNdefMessage() {
        Log.d("TESTE", "Entrei em create message");
        NdefRecord ndefRecord = NdefRecord.createMime("text/plain", message.getBytes());
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);

        return ndefMessage;
    }

    private void updateAfterUserChoice() {
        ViewGroup layout = (ViewGroup) button1.getParent();
        if (null != layout) //for safety only  as you are doing onClick
            layout.removeAllViews();

        textfield = (TextView) findViewById(R.id.text_field);
        textfield.setText("You chose to rate the service with " + rating + " stars");


        TextView newText = new TextView(this);
        newText.setText(R.string.nfc_use);
        newText.setTextSize(25);
        newText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        newText.setTextColor(R.color.card_background);
        layout.addView(newText);
    }

    @Override
    public void onNdefPushComplete(NfcEvent nfcEvent) {
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
    }

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
                switch(msg.what){
                    case MESSAGE_SENT:
                        complete();
                        break;
                }
        }
    };

    public void complete(){
        LinearLayout layout = (LinearLayout) findViewById(R.id.suggestions);
        if(null != layout)
            layout.removeAllViews();

        TextView newText = new TextView(this);
        newText.setText(R.string.nfc_complete);
        newText.setTextSize(25);
        newText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        newText.setTextColor(R.color.card_background);
        layout.addView(newText);

        textfield = (TextView) findViewById(R.id.text_field);
        textfield.setText("Thank you!");
    }
}