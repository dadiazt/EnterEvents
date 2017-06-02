package com.example.entereventsproject.Utilities;

/**
 * Created by Jordi Campoy, Kilian Henares y Dante Diaz 09/05/17.
 */
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.example.entereventsproject.R;

import java.util.Calendar;

import static com.example.entereventsproject.Activities.ListaEntradas.selected_day;
import static com.example.entereventsproject.Utilities.TabFragment.tad;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Ponemos la fecha actual por defecto en el calendario
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), R.style.AppTheme, this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Cambiamos el dia seleccionado por el elegido por el usuario
        selected_day=view.getDayOfMonth()+"-"+(view.getMonth()+1)+"-"+view.getYear();
        tad.notifyDataSetChanged();
    }
}

