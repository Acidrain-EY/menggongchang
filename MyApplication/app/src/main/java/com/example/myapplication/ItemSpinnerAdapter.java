package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ItemSpinnerAdapter extends ArrayAdapter<Item> {

    private final LayoutInflater inflater;

    public ItemSpinnerAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView textView = (TextView) convertView;
        Item item = getItem(position);
        if (item != null) {
            textView.setText(item.getId() + " - " + item.getName());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = (TextView) convertView;
        Item item = getItem(position);
        if (item != null) {
            textView.setText(item.getId() + " - " + item.getName());
        }

        return convertView;
    }
}

