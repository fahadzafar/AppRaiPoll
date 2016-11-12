package com.parse.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.parse.ParseObject;
import com.parse.activities.R.id;
import com.parse.customui.MultiSpinner;
import com.parse.customui.MultiSpinner.MultiSpinnerListener;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseOperation;
import com.parse.operation.ParseOperation.Compare;

public class SearchActivity extends Activity
		implements
			MultiSpinnerListener,
			OnScrollListener,
			OnTouchListener {

	boolean[] selectedCategories;
	ListView listview;
	ListQuestionAdapter la;
	public static List<ParseObject> question_objects;
	
	// for scroll down
	private float mInitialY;
	private boolean scrollDown = false;
	private boolean forcePaginationWait = false;	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.search);

		Button bn = (Button) findViewById(R.id.search_btn_search);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				postSearch();
			}
		});
		// Populate the categories spinner.
		ArrayList<String> tempCategories = new ArrayList<String>();
		tempCategories.add("All");
		for(int i = 0; i < DemographicValues.categories.length; i ++)
		{
			tempCategories.add(DemographicValues.categories[i]);
		}
		MultiSpinner ms = (MultiSpinner) findViewById(id.search_spnr_categories2);
		ms.setItems(tempCategories, "All", this);

		selectedCategories = new boolean[DemographicValues.categories.length+1];
		for (int i = 0; i < selectedCategories.length; i++)
			selectedCategories[i] = true;
	}
	
	void postSearch() {

		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 0);
		// Create the required categories array.
		List<Integer> requiredCat = new ArrayList<Integer>();
		boolean allCategoriesFalse = true;
		for (int i = 1; i < selectedCategories.length; i++) {
			if (selectedCategories[i]) {
				//Log.e("selected", DemographicValues.categories[i-1]);
				requiredCat.add(i-1);
				allCategoriesFalse = false;
			}
		}

		// If no category is selected simply return and print the no category
		// selected message.
		if (allCategoriesFalse) {
			Helper.ShowDialogue("Select at least one category",
					"No category selected", getApplicationContext());
			return;
		}

		// Get the original search text.
		EditText searchField = (EditText) findViewById(R.id.search_query_text);
		String searchText = searchField.getText().toString();

		if (searchText == null)
			searchText = "";

		// Get the questions found based on your search.
		question_objects = ParseOperation
				.GetAllSearchedQuestions(getApplicationContext(), requiredCat,
						Helper.setFirstCharacterToNoCaps(searchText), null, Compare.WASTE);

		// Print number of returned questions.
		int returnedObjectCount = 0;
		if (question_objects != null)
			returnedObjectCount = question_objects.size();
		//Helper.ShowDialogue("Questions Received", "Total-"
			//	+ returnedObjectCount, getApplicationContext());

		// Get the list view in the search activity.
		listview = (ListView) findViewById(R.id.search_listview);

		listview.setOnScrollListener(this);
		listview.setOnTouchListener(this);
		
		// Use the ListQuestionAdapter
		la = new ListQuestionAdapter(this, question_objects);
		listview.setAdapter(la);

		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, 900);
		// Attach the on touch action
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					final int position, long id) {
				Helper.onQuestionClicked(
						(ListView) findViewById(R.id.search_listview),
						position, getApplicationContext());
			}
		});
	}

	@Override
	public void onItemsSelected(boolean[] selected) {
		// TODO Auto-generated method stub
		selectedCategories = selected;
	}

	public void onScroll(AbsListView lw, int firstVisibleItem,
			final int visibleItemCount, final int totalItemCount) {
		switch (lw.getId()) {
			case R.id.search_listview :
				final int lastItem = firstVisibleItem + visibleItemCount;
				if (scrollDown && lastItem == totalItemCount) {
					scrollDown = false;
					forcePaginationWait = true;
					new GetPaginatedDataTask().execute();
					//lastItemMsg.show();
					//Log.e("", "search activity load more data ...");
				}
		}
	}

	public boolean onTouch(View arg0, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				mInitialY = event.getY();
			case MotionEvent.ACTION_MOVE :
				if (event.getY() - mInitialY > 0.0) {
					scrollDown = false;
				} else if (event.getY() - mInitialY < 0.0) {
					if (scrollDown == false && forcePaginationWait == false) {
						scrollDown = true;
					}
				}
		}
		return false;
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	private class GetPaginatedDataTask extends AsyncTask<Void, Void, Void> 
	{
		protected Void doInBackground(Void... arg0) 
		{
			if(question_objects == null)
				return null;
			
			// Don't add if number of polls shown is less than the default limit
			if (question_objects.size() < SharedData.QueryLimit)
				return null;
			
			int currentBottom = 0;
			ParseObject lastViewableObject = null;
			Date usingThisDate = null;
			
			currentBottom = question_objects.size() - 1;
			lastViewableObject = (ParseObject) question_objects.get(currentBottom);
			
			if (lastViewableObject != null) 
			{
				usingThisDate = lastViewableObject.getCreatedAt();
			}
			
			// Create the required categories array.
			List<Integer> requiredCat = new ArrayList<Integer>();
			boolean allCategoriesFalse = true;
			for (int i = 1; i < selectedCategories.length; i++) {
				if (selectedCategories[i]) {
					requiredCat.add(i-1);
					allCategoriesFalse = false;
				}
			}

			// Get the original search text.
			EditText searchField = (EditText) findViewById(R.id.search_query_text);
			String searchText = searchField.getText().toString();

			if (searchText == null)
				searchText = "";
			
			List<ParseObject> newlyRetreivedData = ParseOperation
					.GetAllSearchedQuestions(getApplicationContext(), requiredCat,
							Helper.setFirstCharacterToNoCaps(searchText), usingThisDate, Compare.LESSER_THAN);
			
			Log.e("", "search activity load more data ... "+newlyRetreivedData.size());
			for (ParseObject newQuestion : newlyRetreivedData) {
				// The questions with date greater than the top question
				// date have been retreived. Just add them to the top
				// and keep incrementing top to update the insert
				// location.
				question_objects.add(newQuestion);
			}
			
			return null;
		}
		
		protected void onPostExecute(Void result) {
			forcePaginationWait = false;
			// Need to avoid error when we change the listview and
			// don't notify the UI thread that it's changed
			la.notifyDataSetChanged();
			super.onPostExecute(result);
		}
	
	}
}
