package com.trackyourstuff.mota.trackyourstuff.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.TransactionRecord;

import java.util.ArrayList;

/**
 * Created by mota on 10/3/17.
 */

public class RecordAdapter extends BaseAdapter {
    ArrayList<TransactionRecord> list;
    Activity activity;

    public RecordAdapter(Activity activity, ArrayList<TransactionRecord> list) {
        super();
        this.activity = activity;
        this.list = list;
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
            convertView = LayoutInflater.from(activity).inflate(R.layout.record_list_item, parent, false);
        }
        TextView date = (TextView) convertView.findViewById(R.id.textRecord);
        TextView product = (TextView) convertView.findViewById(R.id.textRecord2);
        TextView cost = (TextView) convertView.findViewById(R.id.textRecord3);
        final TransactionRecord s = (TransactionRecord) this.getItem(position);
        date.setText(s.getDate());
        product.setText(s.getProduct());
        cost.setText(s.getCost());

        return convertView;
    }
}
