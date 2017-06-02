package com.example.entereventsproject.Activities;

import com.example.entereventsproject.Models.Entrada;
import com.example.entereventsproject.Models.SecurityCode;
import com.example.entereventsproject.Models.Session;
import com.example.entereventsproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.entereventsproject.Utilities.TabFragment.tad;

public class PaypalActivity extends Activity {
    private static final String TAG = "paymentExample";
    /**
     * - Set to PayPalConfiguration.ENVIRONMENT_PRODUCTION to move real money.
     * 
     * - Set to PayPalConfiguration.ENVIRONMENT_SANDBOX to use your test credentials
     * from https://developer.paypal.com
     * 
     * - Set to PayPalConfiguration.ENVIRONMENT_NO_NETWORK to kick the tires
     * without communicating to PayPal's servers.
     */
    private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_SANDBOX;

    private static final String CONFIG_CLIENT_ID = "ARNRhHaHeF--3OM9yb4Sdq4luIvCYusJZVqy8gYatJEBfFuYTv1MF2j-wHn-DqAsxzWPdcdtUlZwMMDm";

    private static final int REQUEST_CODE_PAYMENT = 1;

    private ProgressDialog mProgressDialog;
    private boolean selled;
    private boolean only1insert;
    private boolean new_insert;
    private int cantidad;

    // Declaración de nuestra configuración
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(CONFIG_ENVIRONMENT)
            .clientId(CONFIG_CLIENT_ID)
            .merchantName("EnterEvents")
            .merchantPrivacyPolicyUri(Uri.parse("https://www.example.com/privacy"))
            .merchantUserAgreementUri(Uri.parse("https://www.example.com/legal"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paypal_dialog);
        Entrada entrada = (Entrada) getIntent().getExtras().getSerializable("fecha");
        TextView fecha= (TextView) findViewById(R.id.fecha);
        TextView hora= (TextView) findViewById(R.id.hora);
        TextView precio= (TextView) findViewById(R.id.precio);

        fecha.setText(entrada.getDate());
        hora.setText(entrada.getHora());
        precio.setText(entrada.getPrice());

        // Inicio del pago con paypal
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Variables de control para no insertar varias veces o actualizar después de insertar
        only1insert=false;
        new_insert=false;
    }

    public void onBuyPressed(View pressed) {

        final PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);

        selled=false;

        final Entrada entrada = (Entrada) getIntent().getExtras().getSerializable("fecha");

        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference();

        // Comprobación de que la cantidad no excede el aforo máximo
        try {
            ref.child("Session/"+entrada.getDate() + "-" + entrada.getHora()).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Session ses = dataSnapshot.getValue(Session.class);
                            if (ses!=null && ses.getCount()+cantidad>80) {
                                Toast.makeText(PaypalActivity.this,"No hay suficientes entradas disponibles", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PaypalActivity.this, ListaEntradas.class);
                                startActivity(intent);
                            }

                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
        }catch(Exception e) {}

        // Limitamos la cantidad de entradas que se pueden comprar(entre 1 y 40)
        if (cantidad>40) {
            Toast.makeText(this,"Por favor, escoja una cantidad menor de 40 entradas", Toast.LENGTH_SHORT).show();
        } else if (cantidad<1) {
            Toast.makeText(this,"Por favor, escoja una cantidad mayor de 0 entradas", Toast.LENGTH_SHORT).show();
        } else {
            if (!only1insert) {
                // Mostramos un Dialog cargando para indicar que el proceso de compra ha iniciado
                showProgressDialog();
                // Si la ssessión no existe en la base de datos la creamos
                try {
                    ref.child("Session/"+entrada.getDate() + "-" + entrada.getHora()).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Session ses = dataSnapshot.getValue(Session.class);
                                    if (ses==null) {
                                        Session new_ses=new Session(cantidad);
                                        ref.child("Session/"+entrada.getDate() + "-" + entrada.getHora()).setValue(new_ses);
                                        new_insert=true;
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                }catch(Exception e) {}

                only1insert=true;

                // Esperamos dos segundos para que termine la inserción e iniciamos la transacción de los datos
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FirebaseDatabase.getInstance().getReference("Session/" + entrada.getDate() + "-" + entrada.getHora()).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Session ses = mutableData.getValue(Session.class);
                                if (ses == null) {
                                    return Transaction.success(mutableData);
                                }
                                if (ses != null && ses.getCount() + cantidad <= 80 && !new_insert) {
                                    ses.setCount(ses.getCount() + cantidad);
                                }
                                mutableData.setValue(ses);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b,
                                                   DataSnapshot dataSnapshot) {
                            }
                        });
                    }
                }, 2000);
            }

            // Esperamos 3 segundos a que termine la transacción para preparar el intent que le pasaremos a paypal
            final Intent[] intent = {null};
                    final Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                    intent[0] = new Intent(PaypalActivity.this, PaymentActivity.class);

                    intent[0].putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

                    intent[0].putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
                        }
                    }, 3000);

            // Esperamos 5 segundos por seguridad e iniciamos el pago (los tiempos de espera se ejecutan independientemente, tardará 5 segundos en total)
            final Handler handler3 = new Handler();
            handler3.postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();
                    startActivityForResult(intent[0], REQUEST_CODE_PAYMENT);
                }
            }, 5000);


        }
    }

    private PayPalPayment getThingToBuy(String paymentIntent) {
        // Preparamos el pago que le pasaremos a Paypal
        EditText cant = (EditText) findViewById(R.id.cant);
        cantidad = (Integer.parseInt(String.valueOf(cant.getText())));

            return new PayPalPayment(new BigDecimal(cantidad * 8), "EUR", "Entrada Pista de hielo",
                    paymentIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Comprobamos que el pago se ha realizado con éxito
        final Entrada entrada = (Entrada) getIntent().getExtras().getSerializable("fecha");
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i(TAG, confirm.toJSONObject().toString(4));
                        Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));

                        // Añadimos la entrada a la base de datos
                        compraEntrada(entrada);
                        Toast.makeText(getApplicationContext(),"Compra de "+cantidad+" entradas realizada con éxito", Toast.LENGTH_LONG).show();

                        String buy_id = UUID.randomUUID().toString();

                        // Creamos un código que el usuario utilizará para entrar y lo añadimos a la base de datos
                        SecurityCode code = new SecurityCode(buy_id, FirebaseAuth.getInstance().getCurrentUser().getUid(),String.valueOf(cantidad),entrada.getDate(),entrada.getHora());
                        FirebaseDatabase.getInstance().getReference("Sales").child(code.getCode()).setValue(code);

                        // Preparamos un mail con los datos de la compra por si el cliente desea enviarlos a alguien
                        sendEmail(entrada,code);

                        selled=true;
                    } catch (JSONException e) {
                        Log.e(TAG, "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i(TAG, "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(TAG,"An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
        // Si el resultado del pago no es el deseado retiramos la cantidad añadida a la base de datos
        if (!selled) {
            try {
                FirebaseDatabase.getInstance().getReference().child("Session/" + entrada.getDate() + "-" + entrada.getHora()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            int actual_num = 0;

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Session ses = dataSnapshot.getValue(Session.class);
                                if (ses!=null) {
                                    actual_num = ses.getCount();
                                    Map<String, Object> upd = new HashMap<>();
                                    upd.put("count", actual_num - cantidad);
                                    FirebaseDatabase.getInstance().getReference().child("Session/" + entrada.getDate() + "-" + entrada.getHora()).updateChildren(upd);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
            }catch(Exception e) {}
        }
        updateitems();
    }

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        finish();
        super.onDestroy();
    }

    public void compraEntrada(Entrada entrada) {
        for(int i=0;i<cantidad;i++){
            String ticket_id = UUID.randomUUID().toString();
            FirebaseDatabase.getInstance().getReference("tickets")
                    .child(String.valueOf(ticket_id))
                    .setValue(entrada);
        }
    }

    public void updateitems() {
        tad.notifyDataSetChanged();
    }

    void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateitems();
    }

    protected void sendEmail(Entrada entrada, SecurityCode code) {
        Log.i("Send email", "");

        String[] TO = {FirebaseAuth.getInstance().getCurrentUser().getEmail()};
        String[] CC = {"entereventsapp@gmail.com"};

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:entereventsapp@gmail.com"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Compra Entrada EnterEvents");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "\nNombre: Pista de hielo EnterEvents Fecha: "+code.getDate()+"\n"+"Hora: "+code.getHora()+"\n"+"Precio/Unidad:"+entrada.getPrice()+"\n"+"Cantidad: "+code.getTicket_quantity()+"\nCodigo de entrada: "+code.getCode()+"\n\nAtención, guarde el contenido de este email ya que necesitara el codigo de la entrada para acceder al recinto.");

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "No hay ningún email logeado en el movil", Toast.LENGTH_SHORT).show();
        }
    }

}
