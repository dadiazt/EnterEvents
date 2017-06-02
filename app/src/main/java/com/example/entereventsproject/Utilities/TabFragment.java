package com.example.entereventsproject.Utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.entereventsproject.Activities.ListaEntradas;
import com.example.entereventsproject.Adapters.MyTicketsAdapter;
import com.example.entereventsproject.Adapters.TicketAdapter;
import com.example.entereventsproject.Activities.PaypalActivity;
import com.example.entereventsproject.R;
import com.example.entereventsproject.Models.Entrada;
import com.example.entereventsproject.Models.Entradas;
import com.example.entereventsproject.Models.SecurityCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.entereventsproject.Activities.ListaEntradas.fam;


/**
 * Created by Jordi Campoy, Kilian Henares y Dante Diaz 05/05/17.
 */

public class TabFragment extends Fragment  {

    public static final String ARG_SECTION_NUMBER = "section_number";
    public static TicketAdapter tad;

    // Constructor que crea un fragment para el tab
    public static TabFragment newInstance(int sectionNumber) {
        TabFragment fragment = new TabFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public TabFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int section_number = getArguments().getInt(ARG_SECTION_NUMBER);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView list = (ListView) rootView.findViewById(R.id.list);
        final ListView myticketlist = (ListView) rootView.findViewById(R.id.list);
        final ArrayList<SecurityCode> Entradas_Compradas= new ArrayList<>();

        // Preparamos la vista según el tab seleccionado
        switch (section_number) {
            case 1:
                tad=new TicketAdapter(getActivity(), Entradas.getEntradas());
                list.setAdapter(tad);

                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
                    {
                        Entrada ent = (Entrada)adapter.getItemAtPosition(position);
                        String hora = ent.getHora().split("-")[0];
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH");
                        Date data = null;
                        try {
                            data=sdf.parse(ent.getDate()+" "+hora);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (data.after(new Date())) {
                            Intent payp = new Intent(v.getContext(), PaypalActivity.class);
                            payp.putExtra("fecha", (Entrada) adapter.getItemAtPosition(position));
                            startActivity(payp);
                        } else {
                            Toast.makeText(getContext(), "Fecha expirada", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                // Control para esconder el floatingactionmenu
                OnScrollUpDownListener.Action scrollAction = new OnScrollUpDownListener.Action() {

                    @Override
                    public void up() {
                        fam.hideMenu(false);
                    }

                    @Override
                    public void down() {
                        fam.showMenu(true);
                    }

                };
                list.setOnScrollListener(new OnScrollUpDownListener(list, 1, scrollAction));
                tad.notifyDataSetChanged();
                break;
            case 2:
                // Codigo de la entrada sacado de la base de datos y pasado a items
                        FirebaseDatabase.getInstance().getReference("Sales/").addValueEventListener(
                                new ValueEventListener() {

                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot single : dataSnapshot.getChildren()) {
                                            SecurityCode sec = single.getValue(SecurityCode.class);
                                            String hora = sec.getHora().split("-")[1];
                                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH");
                                            Date data = null;
                                            try {
                                                data=sdf.parse(sec.getDate()+" "+(hora));
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                            if (sec !=null && sec.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && data.after(new Date())) {
                                                Entradas_Compradas.add(sec);
                                            }
                                        }
                                        SecurityCode[] bought_array = Entradas_Compradas.toArray(new SecurityCode[Entradas_Compradas.size()]);
                                        MyTicketsAdapter Mtad = new MyTicketsAdapter(getActivity(), bought_array);
                                        myticketlist.setAdapter(Mtad);
                                        Entradas_Compradas.clear();

                                        // Mostramos un QR con los datos de la compra
                                        myticketlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
                                            {
                                                SecurityCode code = (SecurityCode) adapter.getItemAtPosition(position);
                                                Dialog dialog = new Dialog(getActivity());
                                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                                                LayoutInflater inflaterr = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                View  viewtemplelayout= inflaterr.inflate(R.layout.qr_dialog, null);
                                                ImageView i = (ImageView) viewtemplelayout.findViewById(R.id.qr);
                                                i.setImageBitmap(generateQRBitMap(code.getCode()));
                                                dialog.setContentView(viewtemplelayout);
                                                dialog.show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                break;
            case 3:
                rootView=inflater.inflate(R.layout.information, container, false);

                break;
        }
        return rootView;
    }

    private Bitmap generateQRBitMap(final String content) {

        Map<EncodeHintType, ErrorCorrectionLevel> hints;
        hints = new HashMap<>();

        hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.H);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {

                    bmp.setPixel(x , y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }
}