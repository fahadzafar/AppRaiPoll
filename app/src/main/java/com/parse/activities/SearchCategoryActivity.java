package com.parse.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.parse.ParseObject;
import com.parse.activities.R.id;
import com.parse.helper.Helper;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseOperation;

public class SearchCategoryActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_category);

		Button bn = (Button) findViewById(R.id.search_category_btn_search);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				postSearch();
			}
		});

		// Populate the categories spinner.
		Helper.PopulateSpinner(
				(Spinner) findViewById(id.search_category_spnr_categories),
				getBaseContext(), DemographicValues.categories);
	}

	void postSearch() {
		// Get the right category index from the spinner.
		Spinner sp = (Spinner) findViewById(R.id.search_category_spnr_categories);
		int categoryIndex = sp.getSelectedItemPosition();
		List<Integer> requiredCat = new ArrayList<Integer>();
		requiredCat.add(categoryIndex);

		String lookForMe = "";

		// Get the questions found based on your search.
		/*List<ParseObject> question_objects = ParseOperation
				.GetAllSearchedQuestions(getApplicationContext(), requiredCat,
						lookForMe);

		int returnedObjectCount = 0;
		if (question_objects != null)
			returnedObjectCount = question_objects.size();
		Helper.ShowDialogue("Questions Received", "Total-"
				+ returnedObjectCount, getApplicationContext());

		// Get the list view in the search activity.
		final ListView listview = (ListView) findViewById(R.id.search_category_listview);

		// Use the ListQuestionAdapter
		ListQuestionAdapter la = new ListQuestionAdapter(this, question_objects);
		listview.setAdapter(la);
		// Attach the on touch action
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					final int position, long id) {

				Helper.onQuestionClicked(listview, position,
						getApplicationContext());
			}
		});*/
	}

}
