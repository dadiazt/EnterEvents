package com.example.entereventsproject.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.entereventsproject.Adapters.FragmentAdapter;
import com.example.entereventsproject.Utilities.TabFragment;
import com.example.entereventsproject.Firebase_utilities.GoogleApiActivity;
import com.example.entereventsproject.R;
import com.example.entereventsproject.Utilities.DatePickerFragment;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ListaEntradas extends GoogleApiActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ViewPager mViewPager;
    public static String selected_day;
    public static FloatingActionMenu fam = null;
    public com.github.clans.fab.FloatingActionButton floatingActionButton1,floatingActionButton2,floatingActionButton3;
    TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Función para comprobar si el usuario está conectado
        checkActivity();

        setContentView(R.layout.activity_lista_entradas);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(mViewPager);

        // Declaramos el layout para colocar los tabs
        tabs = (TabLayout) findViewById(R.id.tabs);

        tabs.setupWithViewPager(mViewPager);


        // Colocación de las fechas si el usuario no ha filtrado
        if (selected_day==null) {
            Calendar now=Calendar.getInstance();
            selected_day=now.get(Calendar.DAY_OF_MONTH)+"-"+(now.get(Calendar.MONTH)+1)+"-"+now.get(Calendar.YEAR);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH");
            Date data = null;
            try {
                data=sdf.parse(selected_day+" "+20);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (data.before(new Date())) {
                try {
                    now.setTime(sdf.parse(selected_day));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                now.add(Calendar.DATE, 1);
                selected_day=now.get(Calendar.DAY_OF_MONTH)+"-"+(now.get(Calendar.MONTH)+1)+"-"+now.get(Calendar.YEAR);
            }
        }

        // Declaración del floating action menu
        fam = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);
        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                OnCalendar();
                fam.close(true);
            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent map= new Intent(ListaEntradas.this,MapsActivity.class);
                fam.close(true);
                startActivity(map);

            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri= Uri.parse("https://es-es.facebook.com/EnterEventsBadalona/");
                Intent web= new Intent(Intent.ACTION_VIEW,uri);
                fam.close(true);
                startActivity(web);

            }
        });

        // Declaración del drawer (pestaña que se abre por la izquierda)
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView mail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.mail);
        mail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        TextView name = (TextView) navigationView.getHeaderView(0).findViewById(R.id.name);
        name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        ImageView photo = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.user);
        Uri photouser=FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        Picasso.with(this).load(photouser).into(photo);
    }

    public void logout() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                FirebaseAuth.getInstance().signOut();
                checkActivity();
            }
        });
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Ejecución de los diferentes items dentro del drawer
        int id = item.getItemId();

        if (id == R.id.mis_entradas) {
            TabLayout.Tab tab = tabs.getTabAt(1);
            tab.select();
        } else if (id == R.id.log_out) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void checkActivity() {
        if (FirebaseAuth.getInstance().getCurrentUser()==null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        // Inicio del adapter que controla los fragments y los tabs
        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(TabFragment.newInstance(1), "Entradas");
        adapter.addFragment(TabFragment.newInstance(2), "Mis entradas");
        adapter.addFragment(TabFragment.newInstance(3), "Información");
        viewPager.setAdapter(adapter);
    }

    public void OnCalendar(){
        // Dialog que permite al usuario filtrar por fecha
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

}
