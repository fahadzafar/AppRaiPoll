package com.parse.activities;

import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.AppUser;
import com.parse.objects.DemographicValues;

public class SubscriptionActivity extends Activity {
	private ListView lView;

	boolean allChecked = false;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.subscription);
		lView = (ListView) findViewById(R.id.subscription_listview);
		lView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		String[] tempCategories = new String[DemographicValues.categories.length + 1];
		tempCategories[0] = "All";
		for (int i = 0; i < DemographicValues.categories.length; i++) {
			tempCategories[i + 1] = DemographicValues.categories[i];
		}

		// Set option as Multiple Choice. So that user can able to select more
		// the one option from list
		lView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice,
				tempCategories));
		lView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					final int position, long id) {
				if (position == 0) {
					if (allChecked == false)
						allChecked = true;
					else
						allChecked = false;

					// Check all items
					for (int i = 1; i < DemographicValues.categories.length + 1; i++) {
						lView.setItemChecked(i, allChecked);
					}

				}
				if (areAllValuesChecked()) {
					allChecked = true;
					lView.setItemChecked(0, allChecked);
				} else {
					allChecked = false;
					lView.setItemChecked(0, allChecked);
				}
			}
		});

		UpdateUserInterface();

		Button bn = (Button) findViewById(R.id.subscription_update);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onUpdateSubscription();
			}
		});
	}

	boolean areAllValuesChecked() {
		SparseBooleanArray checked = lView.getCheckedItemPositions();
		if ( checked.size() < DemographicValues.categories.length) {
			return false;
		}
		for (int i = 1; i < checked.size(); i++) {
			if (checked.get(i, false) == false) {
				return false;
			}
		}
		return true;
	}
	// This function displays the currently selected subscription categories.
	void UpdateUserInterface() {
		List<Integer> listIndices = AppUser
				.getCurrentUserSubscribedCategories();

		// The categories are checked based on data read from the parse db,
		// but off by one since the 0th index contains "All".
		for (int i = 0; i < listIndices.size(); i++)
			lView.setItemChecked(listIndices.get(i) + 1, true);

		if (areAllValuesChecked()) {
			allChecked = true;
			lView.setItemChecked(0, allChecked);
		} else {
			lView.setItemChecked(0, allChecked);
		}

	}

	void onUpdateSubscription() {

		SparseBooleanArray checked = lView.getCheckedItemPositions();
		JSONArray subscribed_categories = new JSONArray();

		boolean noCatSelected = true;
		for (int i = 0; i < checked.size(); i++) {
			if (checked.get(checked.keyAt(i), false)) {
				// Add this data to the parseobject later to be saved but
				// offset by -1 since top most is all, which is not present
				// in the database. for all, we checkk everything.
				if (checked.keyAt(i) != 0) {
					subscribed_categories.put(checked.keyAt(i) - 1);
					noCatSelected = false;
				}
			}
		}

		if (noCatSelected) {
			Helper.ShowDialogue("No category selected", "Select at least 1",
					getApplicationContext());
			return;
		}

		AppUser.setUserSubscribedCategories(SharedData.current_user,
				subscribed_categories);
		try {
			SharedData.current_user.save();
			Helper.ShowDialogue("Subscriptions", "Updated",
					getApplicationContext());
			SharedData.refreshThisTab[DashboardActivity.HOME_QUESTIONS] = true;
			
			// Update the subscription drawe textbox.
			
			//finish();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
