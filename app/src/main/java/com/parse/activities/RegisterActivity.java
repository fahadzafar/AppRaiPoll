package com.parse.activities;

import java.util.Calendar;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.activities.R.id;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.AppUser;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseOperation;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.DatePicker;

public class RegisterActivity extends Activity {

	public static Button dobPicker;
	public static Calendar loginDOB = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Set View to register.xml
		setContentView(R.layout.register);

		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		/*
		 * TextView loginScreen = (TextView) findViewById(R.id.link_to_login);
		 * 
		 * // Listening to Login Screen link loginScreen.setOnClickListener(new
		 * View.OnClickListener() { public void onClick(View arg0) { // Closing
		 * registration screen // Switching to Login Screen/closing register
		 * screen finish(); } });
		 */

		// The registration submit button to pass on the user information to
		// the server.
		Button registerButon = (Button) findViewById(R.id.btnRegister);
		registerButon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				if (SanityChecks()) {
					AppUser reg_user = GetRegistrationFormData();

					// After you extract the form data, you try to register the
					// user. If it works, close this activity and login.
					if (ParseOperation.RegisterUser(reg_user,
							getApplicationContext())) {
						ParseOperation.LoginUser(reg_user,
								getApplicationContext());
						finish();
					}
				}
			}
		});

		// Inflate the spinners for age, gender, continent, education and race
		// Genders
		Helper.PopulateSpinner((Spinner) findViewById(id.spinnerGender),
				getBaseContext(), DemographicValues.genders);

		// Ages
		// Helper.PopulateSpinner((Spinner) findViewById(id.spinnerAge),
		// getBaseContext(), DemographicValues.ages);

		// Race
		Helper.PopulateSpinner((Spinner) findViewById(id.spinnerRace),
				getBaseContext(), DemographicValues.races);

		// Education
		Helper.PopulateSpinner((Spinner) findViewById(id.spinnerEducation),
				getBaseContext(), DemographicValues.educations);

		// Continents
		Helper.PopulateSpinner((Spinner) findViewById(id.spinnerContinent),
				getBaseContext(), DemographicValues.continents);

		// Religion
		Helper.PopulateSpinner((Spinner) findViewById(id.spinnerReligion),
				getBaseContext(), DemographicValues.religion);

		dobPicker = (Button) findViewById(id.registerDOB);
		loginDOB = null;
	}

	public AppUser GetRegistrationFormData() {
		AppUser registered_user = new AppUser();

		// Extract all the values from the view input fields.
		EditText tv = (EditText) findViewById(R.id.reg_email);
		registered_user.email = tv.getText().toString();
		tv = (EditText) findViewById(R.id.reg_password);
		registered_user.password = tv.getText().toString();
		tv = (EditText) findViewById(R.id.reg_username);
		registered_user.username = tv.getText().toString();

		Spinner sp;

		// If no dates are set, then its unspecified
		if (loginDOB == null) {
			registered_user.age = 0;
			Calendar dob = Calendar.getInstance();
			dob.set(0, 0, 0);
			registered_user.dob = SharedData.defaultDateOfBirth;
		} else {
			registered_user.dob = loginDOB.getTime();

			// Calculate age group
			Calendar today = Calendar.getInstance();
			int age = today.get(Calendar.YEAR) - loginDOB.get(Calendar.YEAR);
			if (today.get(Calendar.MONTH) < loginDOB.get(Calendar.MONTH)) {
				age--;
			} else if (today.get(Calendar.MONTH) == loginDOB.get(Calendar.MONTH)
					&& today.get(Calendar.DAY_OF_MONTH) < loginDOB
							.get(Calendar.DAY_OF_MONTH)) {
				age--;
			}

			if (age < 18) {
				registered_user.age = 1;
			} else if (age >= 18 && age <= 30) {
				registered_user.age = 2;
			} else if (age >= 31 && age <= 40) {
				registered_user.age = 3;
			} else if (age >= 41 && age <= 50) {
				registered_user.age = 4;
			} else if (age >= 51 && age <= 70) {
				registered_user.age = 5;
			} else {
				registered_user.age = 6;
			}

		}
		// sp = (Spinner) findViewById(R.id.spinnerAge);
		// registered_user.age = sp.getSelectedItemPosition();

		// Place the spinner values in the object for registration.
		sp = (Spinner) findViewById(R.id.spinnerGender);
		registered_user.gender = sp.getSelectedItemPosition();
		sp = (Spinner) findViewById(R.id.spinnerContinent);
		registered_user.continent = sp.getSelectedItemPosition();
		sp = (Spinner) findViewById(R.id.spinnerEducation);
		registered_user.education = sp.getSelectedItemPosition();
		sp = (Spinner) findViewById(R.id.spinnerRace);
		registered_user.race = sp.getSelectedItemPosition();
		sp = (Spinner) findViewById(R.id.spinnerReligion);
		registered_user.religion = sp.getSelectedItemPosition();
		return registered_user;
	}

	// Check to see if the data was entered correctly before submission.
	public boolean SanityChecks() {

		EditText tv = (EditText) findViewById(R.id.reg_username);
		String username = tv.getText().toString();
		if (Helper.isEmpty(username)) {
			Helper.ShowDialogue("Error", "Please enter a username",
					getApplicationContext());
			return false;
		}

		tv = (EditText) findViewById(R.id.reg_email);
		String email = tv.getText().toString();
		if (Helper.isEmpty(email)) {
			Helper.ShowDialogue("Error", "Please enter an email address",
					getApplicationContext());
			return false;
		}

		tv = (EditText) findViewById(R.id.reg_password);
		String password1 = tv.getText().toString();

		tv = (EditText) findViewById(R.id.reg_repeat_password);
		String password2 = tv.getText().toString();

		if (Helper.isEmpty(password1) || Helper.isEmpty(password2)) {
			Helper.ShowDialogue("Error", "Please enter password",
					getApplicationContext());
			return false;
		}

		if (!password1.equals(password2)) {
			Helper.ShowDialogue("Error", "Passwords do not match",
					getApplicationContext());
			return false;
		}

		if (Helper.passwordCheck(password1, getApplicationContext()) == false)
			return false;

		Spinner gen_sp = (Spinner) findViewById(R.id.spinnerGender);
		// Spinner age_sp = (Spinner) findViewById(R.id.spinnerAge);
		Spinner race_sp = (Spinner) findViewById(R.id.spinnerRace);
		Spinner edu_sp = (Spinner) findViewById(R.id.spinnerEducation);
		Spinner con_sp = (Spinner) findViewById(R.id.spinnerContinent);
		Spinner rel_sp = (Spinner) findViewById(R.id.spinnerReligion);

		if (gen_sp.getSelectedItemPosition() == 0
				&& (loginDOB == null)
				// && age_sp.getSelectedItemPosition() == 0
				&& race_sp.getSelectedItemPosition() == 0
				&& edu_sp.getSelectedItemPosition() == 0
				&& con_sp.getSelectedItemPosition() == 0
				&& rel_sp.getSelectedItemPosition() == 0) {
			Helper.ShowDialogue("Error:",
					"Please enter at least 1 Demographic.",
					getApplicationContext());
			return false;
		}

		return true;
	}

	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	public static class DatePickerFragment extends DialogFragment
			implements
				OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int year, month, day;
			if (loginDOB == null) {
				// Use the current date as the default date in the picker
				final Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
				month = c.get(Calendar.MONTH);
				day = c.get(Calendar.DAY_OF_MONTH);
			} else {
				
				year = loginDOB.get(Calendar.YEAR);
				month = loginDOB.get(Calendar.MONTH);
				day = loginDOB.get(Calendar.DAY_OF_MONTH);
			}
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {			
			loginDOB = Calendar.getInstance();
			loginDOB.set(arg1, arg2, arg3);
			dobPicker.setText(DateFormat.format("MMM dd, yyyy", loginDOB.getTime())
					.toString());
		}
	}
}