package com.trackyourstuff.mota.trackyourstuff.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.Order;
import java.util.ArrayList;;

/**
 * Created by mota on 10/3/17.
 */

public class OrdersAdapter extends BaseAdapter {
    ArrayList<Order> list;
    Activity activity;
    DatabaseReference clientsRef;

    public OrdersAdapter(Activity activity, ArrayList<Order> list) {
        super();
        this.activity = activity;
        this.list = list;

        // clients reference
        clientsRef = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users/clients");
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.order_list_item, parent, false);
        }
        TextView date = (TextView) convertView.findViewById(R.id.date);
        final TextView address = (TextView) convertView.findViewById(R.id.address);
        TextView order_id = (TextView) convertView.findViewById(R.id.order_id);
        TextView cost = (TextView) convertView.findViewById(R.id.cost);

        final Order s = (Order) this.getItem(position);

        date.setText(s.getCreated_at().toString());
        order_id.setText(s.getId());
        cost.setText(""+s.getCost()+"€");

        final com.google.firebase.database.Query clientQuery = clientsRef.child("users/clients").orderByChild("id").equalTo(s.getClient_id());
        clientQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        address.setText(issue.child("address").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        return convertView;
    }
}
