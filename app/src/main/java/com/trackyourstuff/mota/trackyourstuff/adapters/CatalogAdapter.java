package com.trackyourstuff.mota.trackyourstuff.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.trackyourstuff.mota.trackyourstuff.R;
import com.trackyourstuff.mota.trackyourstuff.objects.Pizza;

import java.util.ArrayList;

/**
 * Created by mota on 10/3/17.
 */

public class CatalogAdapter extends BaseAdapter {
    ArrayList<Pizza> list;
    Activity activity;
    CatalogAdapter adapter;
    public CatalogAdapter(Activity activity, ArrayList<Pizza> list){
        super();
        this.activity=activity;
        this.list=list;
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
        if(convertView==null)
        {
            convertView= LayoutInflater.from(activity).inflate(R.layout.catalog_list_item ,parent,false);
        }
        TextView name = (TextView) convertView.findViewById(R.id.pizza_name);
        TextView description = (TextView) convertView.findViewById(R.id.pizza_description);
        TextView ingredients = (TextView) convertView.findViewById(R.id.pizza_ingredients);
        TextView cost = (TextView) convertView.findViewById(R.id.pizza_cost);
        final Pizza s = (Pizza) this.getItem(position);
        name.setText(s.getName());
        description.setText(s.getDescription());
        ingredients.setText(s.getIngredients());
        cost.setText("" + s.getCost()+"€");

        return convertView;
    }

}
