package com.trackyourstuff.mota.trackyourstuff.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.trackyourstuff.mota.trackyourstuff.IProgressService;
import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.adapters.CatalogAdapter;
import com.trackyourstuff.mota.trackyourstuff.objects.Client;
import com.trackyourstuff.mota.trackyourstuff.objects.Order;
import com.trackyourstuff.mota.trackyourstuff.objects.Pizza;
import com.trackyourstuff.mota.trackyourstuff.services.NotificationService;
import com.trackyourstuff.mota.trackyourstuff.services.ProgressService;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class CatalogActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private DatabaseReference productsRef;
    private DatabaseReference ordersRef;
    private StorageReference storageRef;
    ArrayList<Pizza> data = new ArrayList<Pizza>();
    CatalogAdapter adapter;
    ArrayList<Pizza> cart = new ArrayList<Pizza>();
    Iterator<Pizza> cart_iterator;
    Bitmap bitmap;
    public final static int QRcodeWidth = 500 ;
    private Client my_client;
    protected ClientHomeActivity home_activity;

    boolean mBound;
    IProgressService mIProgressService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_catalog);

        home_activity = (ClientHomeActivity) this.getParent();

        // Getting client
        my_client = getIntent().getExtras().getParcelable("Client");

        // Views
        Button checkOut = (Button) findViewById(R.id.check_out);
        Button clear = (Button) findViewById(R.id.clear);
        final TextView totalPrice = (TextView) findViewById(R.id.totalPrice);

        // Cart buttons listener
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Adding new order to database
                DatabaseReference newRef = ordersRef.push();

                try {
                    bitmap = TextToImageEncode(newRef.getKey());
                } catch (WriterException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                Order newOrder = new Order(cart, newRef.getKey(), my_client.getId(), new Date().toString());
                newOrder.setStatus("unassigned");
                newRef.setValue(newOrder);

                // Adding QR code to the storage
                StorageReference orderRef = storageRef.child("images/" + newRef.getKey() + ".jpg");

                orderRef.putBytes(data)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                            }
                        });


                totalPrice.setText("PAID!!");
                Intent serviceIntent =new Intent(CatalogActivity.this, ProgressService.class);
                startService(serviceIntent);
                bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cart.clear();
                totalPrice.setText("Total Cost: " + getTotalCost());
            }
        });

        storageRef = FirebaseStorage.
                getInstance().
                getReference();


        // Get orders Ref
        ordersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("orders/");

        // Get products Ref
        productsRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("products/pizzas/");

        adapter = new CatalogAdapter(this, data);

        productsRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // Get the value from the DataSnapshot and add it to the teachers' list
                Pizza obj = (Pizza) dataSnapshot.getValue(Pizza.class);
                data.add(obj);

                // Notify the ArrayAdapter that there was a changes
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ListView listView = (ListView) findViewById(R.id.list_of_products);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pizza item = (Pizza) adapter.getItem(position);

                cart.add(item);
                totalPrice.setText("Total Cost: " + getTotalCost());
                Toast.makeText(getBaseContext(),"Added to the cart", Toast.LENGTH_SHORT).show();
            }
        });
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

    private double getTotalCost(){
        double totalCost = 0;
        cart_iterator = cart.iterator();

        while (cart_iterator.hasNext()) {
            Pizza element = cart_iterator.next();
            totalCost += element.getCost();
        }

        return totalCost;
    }

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.qr_b):getResources().getColor(R.color.qr_w);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }


    ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mIProgressService = IProgressService.Stub.asInterface(service);
            mBound = true;
            try {
                Log.d("Service","--->" +  mIProgressService.getProgress());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            mIProgressService = null;
            mBound = false;
        }
    };
}