package com.parse.helper;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements OnDateSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
		// Use the current date as the default date in the picker
		final Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);

		// Create a new instance of DatePickerDialog and return it
		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) 
	{
		Log.e("", arg1+" "+arg2+" "+arg3);
	}
}