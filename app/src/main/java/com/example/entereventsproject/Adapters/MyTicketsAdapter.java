package com.example.entereventsproject.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.entereventsproject.R;
import com.example.entereventsproject.Models.SecurityCode;

/**
 * Created by tarda on 26/05/17.
 */

public class MyTicketsAdapter extends BaseAdapter {
    private Context mContext;
    private SecurityCode[] sec;

    public MyTicketsAdapter(Context mContext, SecurityCode[] sec) {
        this.mContext = mContext;
        this.sec = sec;
    }

    @Override
    public int getCount() {
        return sec.length;
    }

    @Override
    public SecurityCode getItem(int position) {
        return sec[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.entrada_item, parent, false);
        }

        SecurityCode item = getItem(position);

        TextView price = (TextView) convertView.findViewById(R.id.price);
        price.setText((8*Integer.parseInt(item.getTicket_quantity()))+"€");

        TextView fecha = (TextView) convertView.findViewById(R.id.fecha);
        fecha.setText(item.getDate());

        TextView hora = (TextView) convertView.findViewById(R.id.time);
        hora.setText(item.getHora());

        TextView tickets = (TextView) convertView.findViewById(R.id.tickets);
        tickets.setText(item.getTicket_quantity());

        return convertView;
    }
}