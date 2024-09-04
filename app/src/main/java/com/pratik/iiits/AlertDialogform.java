package com.pratik.iiits;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.cardview.widget.CardView;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class AlertDialogform extends AppCompatDialogFragment {
    private EditText editTextDate,editTextTime, editTextLabel;
    private CardView addeventbutton,closedailog;
    private EventFormDailogListner listner;
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_event_form,null);

        builder.setView(view);

        editTextDate = view.findViewById(R.id.datebox);
        editTextTime = view.findViewById(R.id.timebox);
        editTextLabel = view.findViewById(R.id.labelofeventbox);
        addeventbutton = view.findViewById(R.id.addeventbutton);
        closedailog= view.findViewById(R.id.closedailog);
        editTextDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent arg1) {
                switch(arg1.getAction())
                {
                    case MotionEvent.ACTION_DOWN :
                        showDateDialog(editTextDate);
                        break;
                    case MotionEvent.ACTION_UP  :
                        break;
                }

                return true;

            }
        });
        editTextTime.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent arg1) {
                switch(arg1.getAction())
                {
                    case MotionEvent.ACTION_DOWN :
                        showTimeDialog(editTextTime);
                        break;
                    case MotionEvent.ACTION_UP  :
                        break;

                }

                return true;
            }
        });
        addeventbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editTextDate.getText().toString().isEmpty() || editTextTime.getText().toString().isEmpty() || editTextLabel.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(),"Please Fill the form completely",Toast.LENGTH_SHORT).show();
                }
                else {
                    String label = editTextLabel.getText().toString();
                    String date = editTextDate.getText().toString();
                    String time = editTextTime.getText().toString();
                    listner.appplyTexts(label,date,time);
                }
            }
        });
        closedailog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listner.appplyTexts(null,null,null);
            }
        });
        return builder.create();
    }
    private void showTimeDialog(final EditText time_in) {
        final Calendar calendar=Calendar.getInstance();

        TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                calendar.set(Calendar.MINUTE,minute);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm:ss.SSS zzz");
                time_in.setText(simpleDateFormat.format(calendar.getTime()));
            }
        };

        new TimePickerDialog(getActivity(),R.style.Pickertheme,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false).show();
    }
    private void showDateDialog(final EditText date_in) {
        final Calendar calendar=Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MMM dd yyyy");
                date_in.setText(simpleDateFormat.format(calendar.getTime()));

            }
        };

        new DatePickerDialog(getActivity(),R.style.Pickertheme,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listner = (EventFormDailogListner) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+"Must implement EventformDailoglistner");
        }
    }

    public interface EventFormDailogListner {
       void appplyTexts(String label, String Date, String Time);
    }
}
