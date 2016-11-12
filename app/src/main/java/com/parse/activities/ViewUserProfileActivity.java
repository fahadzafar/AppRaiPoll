package com.parse.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.activities.R.id;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseOperation;

public class ViewUserProfileActivity extends Activity {

	public static Button viewUserDobPicker;
	public static Calendar viewUserDob = null;

	// Tracks which demographics have changed and to what value.
	HashMap<String, Integer> changedDemo = new HashMap<String, Integer>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_user_profile);

		// set the user name based on the current context.
		TextView tv_username = (TextView) findViewById(R.id.view_user_profile_lgUsername);
		tv_username.setText(SharedData.current_user.getUsername());

		SetUserInterface();
		// Set the change password request.
		Button bn = (Button) findViewById(R.id.view_user_profile_btn_lgChangePassword);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Check to see if the new passwords match. If they do not send
				// the blocking request to change password.
				EditText new_pass = (EditText) findViewById(R.id.view_user_profile_lgPasswordnew);
				EditText retyped_new_pass = (EditText) findViewById(R.id.view_user_profile_lgPasswordRetype);
				EditText old_pass = (EditText) findViewById(R.id.view_user_profile_lgPasswordold);

				String new_string = new_pass.getText().toString();
				String retyped_new_string = retyped_new_pass.getText()
						.toString();
				String oldPassword = old_pass.getText().toString();

				if (new_string.length() == 0
						|| retyped_new_string.length() == 0) {
					Helper.ShowDialogue("Error", "Password cannot be empty",
							getApplicationContext());
					return;
				}

				if (new_string.compareTo(oldPassword) == 0) {
					Helper.ShowDialogue("Error",
							"Old and new passwords are the same",
							getApplicationContext());
					return;
				}

				if (Helper.passwordCheck(new_string, getApplicationContext()) == false) {
					return;
				}
				if (new_string.equals(retyped_new_string)) {
					try {
						ParseUser oldPasswordTest = ParseUser.logIn(
								SharedData.current_user.getUsername(),
								oldPassword);

						if (oldPasswordTest != null) {
							SharedData.current_user = ParseOperation
									.ChangePassword(SharedData.current_user,
											new_string, getApplicationContext());
						}
					} catch (ParseException e1) {
						Helper.ShowDialogue("Old Password", "Incorrecct",
								getApplicationContext());
					}

				} else {
					Helper.ShowDialogue("Password mismatch",
							"New passwords do not match",
							getApplicationContext());
				}
			}
		});

		// On submitting demographic changes.
		bn = (Button) findViewById(R.id.view_user_profile_btn_submitDemographicChanges);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// Populate the hashmap
				setSpinnerValueInMap("gender",
						id.view_user_profile_spinnerGender);
				setSpinnerValueInMap("race", id.view_user_profile_spinnerRace);
				setSpinnerValueInMap("age", id.view_user_profile_spinnerAge);
				setSpinnerValueInMap("education",
						id.view_user_profile_spinnerEducation);
				setSpinnerValueInMap("continent",
						id.view_user_profile_spinnerContinent);
				setSpinnerValueInMap("religion",
						id.view_user_profile_spinnerReligion);

				if (ParseOperation.UpdateDemographics(changedDemo,viewUserDob,
						getApplicationContext())) {

					// Reset the user interface again after the changes have
					// been saved.
					SetUserInterface();
					Helper.ShowDialogue(
							"Message",
							"Previously voted questions data will change accordingly in a few hours.",
							getApplicationContext());
				} else {
					Helper.ShowDialogue("Message", "Nohing to change",
							getApplicationContext());
				}
			}
		});

		viewUserDobPicker = (Button) findViewById(id.view_user_profile_btnDOB);
		viewUserDob = null;
	}

	void showAgeButtonOrText(String demographic, int textId, int spinnerId, int btnId,
			String[] data)
	{
		int user_index = SharedData.current_user.getInt(demographic);
		TextView valueTV = (TextView) findViewById(textId);
		Button demoSpinner = (Button) findViewById(btnId);		

		if (user_index != 0) {
			Date dofBirth = (Date) SharedData.current_user.get(DemographicValues.DOB_TITLE);
			valueTV.setText(DateFormat.format("MMM dd, yyyy", dofBirth)
					.toString());
			valueTV.setVisibility(View.VISIBLE);
			demoSpinner.setVisibility(View.GONE);
		} else {
			valueTV.setVisibility(View.GONE);
			demoSpinner.setVisibility(View.VISIBLE);
		}

		// If there is already a value present, we will not change it
		// Thus we put a -1 for those that are not changing
		if (user_index != 0) {
			user_index = -1;
		}
		changedDemo.put(demographic, user_index);
	}

	void showSpinnerOrText(String demographic, int textId, int spinnerId,
			String[] data) {
		int user_index = SharedData.current_user.getInt(demographic);
		TextView valueTV = (TextView) findViewById(textId);
		Spinner demoSpinner = (Spinner) findViewById(spinnerId);

		if (user_index != 0) {
			valueTV.setText(data[user_index]);
			valueTV.setVisibility(View.VISIBLE);
			demoSpinner.setVisibility(View.GONE);
		} else {
			valueTV.setVisibility(View.GONE);
			demoSpinner.setVisibility(View.VISIBLE);
			Helper.PopulateSpinner(demoSpinner, getBaseContext(), data);
		}

		// If there is already a value present, we will not change it
		// Thus we put a -1 for those that are not changing
		if (user_index != 0) {
			user_index = -1;
		}
		changedDemo.put(demographic, user_index);
	}

	void setSpinnerValueInMap(String demographic, int spinnerId) {
		//special case for age
		if(demographic.equalsIgnoreCase("age") && viewUserDobPicker.getVisibility() == View.VISIBLE)
		{
			// If no dates are set, then its unspecified
			if(viewUserDob == null)
			{
				changedDemo.put(demographic, 0);
			}
			else
			{
				// Calculate age group
				Calendar today = Calendar.getInstance();  
				int age = today.get(Calendar.YEAR) - viewUserDob.get(Calendar.YEAR);  
				if (today.get(Calendar.MONTH) < viewUserDob.get(Calendar.MONTH)) {
					age--;  
				} else if (today.get(Calendar.MONTH) == viewUserDob.get(Calendar.MONTH)
						&& today.get(Calendar.DAY_OF_MONTH) < viewUserDob.get(Calendar.DAY_OF_MONTH)) {
					age--;  
				}

				if(age < 18)
				{
					changedDemo.put(demographic, 1);
				}
				else if(age >= 18 && age <= 30)
				{
					changedDemo.put(demographic, 2);
				}
				else if(age >= 31 && age <= 40)
				{
					changedDemo.put(demographic, 3);
				}
				else if(age >= 41 && age <= 50)
				{
					changedDemo.put(demographic, 4);
				}
				else if(age >= 51 && age <= 70)
				{
					changedDemo.put(demographic, 5);
				}
				else
				{
					changedDemo.put(demographic, 6);
				}
			}
		}
		else
		{
			Spinner demoSpinner = (Spinner) findViewById(spinnerId);
			if (demoSpinner.getVisibility() == View.VISIBLE) {
				changedDemo.put(demographic, demoSpinner.getSelectedItemPosition());
			}
		}
	}

	void SetUserInterface() {
		showSpinnerOrText("gender", R.id.view_user_profile_valGender,
				id.view_user_profile_spinnerGender, DemographicValues.genders);

		// custom for age
		showAgeButtonOrText("age", R.id.view_user_profile_valAge,
				id.view_user_profile_spinnerAge, id.view_user_profile_btnDOB, DemographicValues.ages);


		showSpinnerOrText("continent", R.id.view_user_profile_valContinent,
				id.view_user_profile_spinnerContinent,
				DemographicValues.continents);

		showSpinnerOrText("education", R.id.view_user_profile_valEducation,
				id.view_user_profile_spinnerEducation,
				DemographicValues.educations);

		showSpinnerOrText("race", R.id.view_user_profile_valRace,
				id.view_user_profile_spinnerRace, DemographicValues.races);

		showSpinnerOrText("religion", R.id.view_user_profile_valReligion,
				id.view_user_profile_spinnerReligion,
				DemographicValues.religion);

	}

	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new ViewUserDatePickerFragment();
		newFragment.show(getFragmentManager(), "datePicker");
	}

	public static class ViewUserDatePickerFragment extends DialogFragment implements OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) 
		{
			int year, month, day;
			if(viewUserDob == null)
			{
				// Use the current date as the default date in the picker
				final Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
				month = c.get(Calendar.MONTH);
				day = c.get(Calendar.DAY_OF_MONTH);
			}
			else
			{
				year = viewUserDob.get(Calendar.YEAR);
				month = viewUserDob.get(Calendar.MONTH);
				day = viewUserDob.get(Calendar.DAY_OF_MONTH);
			}
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) 
		{
			viewUserDob = Calendar.getInstance();
			viewUserDob.set(arg1, arg2, arg3);
			viewUserDobPicker.setText(DateFormat.format("MMM dd, yyyy", viewUserDob.getTime()).toString());
		}
	}
}
