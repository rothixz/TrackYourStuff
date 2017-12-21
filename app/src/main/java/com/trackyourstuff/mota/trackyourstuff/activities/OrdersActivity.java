package com.trackyourstuff.mota.trackyourstuff.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.adapters.OrdersAdapter;
import com.trackyourstuff.mota.trackyourstuff.objects.Order;
import com.trackyourstuff.mota.trackyourstuff.objects.Pizza;
import com.trackyourstuff.mota.trackyourstuff.objects.Transporter;

import java.util.ArrayList;

public class OrdersActivity extends AppCompatActivity {
    ArrayList<Order> data = new ArrayList<Order>();
    OrdersAdapter adapter;
    ArrayList<String> order_ids = new ArrayList<String>();
    private DatabaseReference ordersRef;
    private DatabaseReference transportersRef;
    private Transporter transporter;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        // Getting transporter
        transporter = getIntent().getExtras().getParcelable("Transporter");

        // Views
        Button checkOut = (Button) findViewById(R.id.check_out);
        Button clear = (Button) findViewById(R.id.clear);
        final TextView n_orders = (TextView) findViewById(R.id.n_orders);

        // Get orders Ref
        ordersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("orders/");

        // Get transporters Ref
        transportersRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users/transporters/");

        // Cart buttons listener
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final com.google.firebase.database.Query user_transporter = transportersRef.orderByChild("username").equalTo(transporter.getUsername());

                user_transporter.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                issue.child("orders").getRef().setValue(order_ids);
                                n_orders.setText("You have 10 minutes to pick the orders!");
                                for (String element : order_ids) {
                                    final com.google.firebase.database.Query update_order = ordersRef.orderByChild("id").equalTo(element);

                                    update_order.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                for (DataSnapshot issue : dataSnapshot.getChildren()) {
                                                    issue.child("status").getRef().setValue("assigned");
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                order_ids.clear();
                n_orders.setText("No orders selected");
            }
        });

        adapter = new OrdersAdapter(this, data);

        ordersRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Order obj = (Order) dataSnapshot.getValue(Order.class);
                String client_id = dataSnapshot.child("client_id").getValue().toString();
                adapter.notifyDataSetChanged();
                final ArrayList<Pizza> products_list = new ArrayList<>();
                dataSnapshot.child("products").getRef().addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Pizza elm = dataSnapshot.getValue(Pizza.class);
                        products_list.add(elm);
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

                if (dataSnapshot.child("status").getValue().toString().equalsIgnoreCase("unassigned")) {
                    Order obj = new Order(products_list, dataSnapshot.child("id").getValue().toString(), client_id, dataSnapshot.child("created_at").getValue().toString());
                    obj.setStatus(dataSnapshot.child("status").getValue().toString());
                    data.add(obj);

                    adapter.notifyDataSetChanged();
                }
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

        ListView listView = (ListView) findViewById(R.id.list_of_orders);

        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Order item = (Order) adapter.getItem(position);
                order_ids.add(item.getId());
                adapter.notifyDataSetChanged();
                n_orders.setText("Total n.orders: " + order_ids.size());
                Toast.makeText(getBaseContext(), "Added to the orders list", Toast.LENGTH_SHORT).show();
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
}
