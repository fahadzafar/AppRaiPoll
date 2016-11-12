package com.parse.activities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.chart.ChartMaker;
import com.parse.helper.ColorPallets;
import com.parse.helper.DialogueGenerator;
import com.parse.helper.Helper;
import com.parse.helper.ShareHelper;
import com.parse.helper.SharedData;
import com.parse.helper.VoteMathHelper;
import com.parse.objects.AnswerTemplates;
import com.parse.objects.AppUser;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseOperation;

public class ViewQuestionActivity extends FragmentActivity {

	Bitmap downloadedQuestionImage = null;
	ScrollView top;
	ScrollView bottom;
	Button Buttonbookmark, Buttonflag;
	// Stores all the charts viewed on this screen.
	HashMap<String, GraphicalView> chartsForShare;
	ParseObject VoteData;
	ParseObject BookmarkData;

	// String[] deviceNameArr = new String[]{"Voter Distibution",
	// "Gender Based", "Age Based", "Continent Based",
	// "Education Based", "Race Based", "Religion Based"};
	public static final String KEY_VOTER_CHART = "Voter Distibution";
	public static final String KEY_GENDER_CHART = "Gender Based";
	public static final String KEY_AGE_CHART = "Age Based";
	public static final String KEY_CONTINENT_CHART = "Continent Based";
	public static final String KEY_EDUCATION_CHART = "Education Based";
	public static final String KEY_RACE_CHART = "Race Based";
	public static final String KEY_RELIGION_CHART = "Religion Based";

	// The user selected option for the question.
	int optionSelected = -1;

	// Layout parameters for the dynamically generated radio buttons.
	LayoutParams layout_params = new LayoutParams(LayoutParams.MATCH_PARENT,
			LayoutParams.WRAP_CONTENT);

	public static ArrayList<RadioButton> arrQuestionOptions;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_question);

		// Dismisses loading screen from clicking on item in list
		if (ListFragmentQuestions.itemClickProgress != null)
			if (ListFragmentQuestions.itemClickProgress.isShowing())
				ListFragmentQuestions.itemClickProgress.dismiss();

		chartsForShare = new HashMap<String, GraphicalView>();
		Helper.setUpActionBar(this);

		// Holder for all the dynamically generated radio buttons.
		arrQuestionOptions = new ArrayList<RadioButton>();

		// Set the values in the activity controls to the incoming question.
		UpdateUserInterface();

		// -------------- drag bar
		top = (ScrollView) findViewById(R.id.view_question_layout_top);
		bottom = (ScrollView) findViewById(R.id.view_question_layout_bottom);

		View v = (View) findViewById(R.id.view_question_layout_draggable);
		v.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent me) {
				if (me.getAction() == MotionEvent.ACTION_DOWN) {
					// float oldXvalue = me.getX();
					// float oldYvalue = me.getY();
					// Log.i("myTag", "Action Down " + oldXvalue + "," +
					// oldYvalue);
					// Helper.ShowDialogue("down", "", getApplicationContext());
				} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
					// int newYvalue = (int) me.getRawY();
					int oldYvalue = (int) (me.getY());

					Display display = getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);

					top.setLayoutParams(new LinearLayout.LayoutParams(v
							.getWidth(),
							Math.max(100, Math.min(top.getHeight() + oldYvalue,
									size.y - 500))));

				}
				return true;
			}
			// Implementation;
		});
		// ----------------
		// Add share button code.
		// Share buttons are enabled based on retrieving the particular
		// demographi
		// data and rendering its chart. when all charts are rendered the share
		// multi-select button is enabled.

		ImageView iv = (ImageView) findViewById(R.id.view_question_question_image);
		iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FullImageActivity.questionImage = downloadedQuestionImage;
				Intent i = new Intent();
				i.setClass(getApplicationContext(), FullImageActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		});

		attachPinFlagButtons();

		Button bn = (Button) findViewById(R.id.view_question_btnVote);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				v.setEnabled(false);
				RadioGroup rg = (RadioGroup) findViewById(R.id.view_question_radiogrp_answers);
				optionSelected = rg.getCheckedRadioButtonId();

				if (optionSelected != -1) {
					List<ParseObject> plotData = ParseOperation.AddVote(
							optionSelected, getApplicationContext(), VoteData);

					SharedData.refreshThisTab[DashboardActivity.PERSONAL_QUESTIONS] = true;

					// update the visible tab total_votes value
					v.setEnabled(false);
					setAllRadioGroup(false);

					if (plotData.size() != 0) {
						showAllCharts(plotData);
					}
				}
			}
		});
		// --- Setup the Flag Button

		Buttonflag = (Button) findViewById(R.id.view_question_btnFlag);
		Buttonflag.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (isFlagged == false) {
					Buttonflag.setEnabled(false);
					AppUser.setUserFlaggedQuestions(
							SharedData.focus_question.getObjectId(),
							getApplicationContext());
					Buttonflag.setText(unFlagText);
					SharedData.focus_question.increment("total_flags");
					SharedData.focus_question.saveInBackground();
					isFlagged = true;
					Buttonflag.setEnabled(true);
				} else {
					Buttonflag.setEnabled(false);
					Buttonflag.setText(flagText);
					isFlagged = false;
					AppUser.removeUserFlaggedQuestions(
							SharedData.focus_question.getObjectId(),
							getApplicationContext());
					SharedData.focus_question.increment("total_flags", -1);
					SharedData.focus_question.saveInBackground();
					Buttonflag.setEnabled(true);
				}
				SharedData.refreshThisTab[DashboardActivity.HOME_QUESTIONS] = true;
			}
		});

		if (AppUser.getUserFlaggedQuestions().contains(
				SharedData.focus_question.getObjectId())) {
			isFlagged = true;
			Buttonflag.setText(unFlagText);
		}
	}

	public static boolean isFlagged = false;
	String flagText = "Flag";
	String unFlagText = "Un-Flag";

	public static boolean isBookmarked = false;
	String bookmarkText = "Bookmark";
	String unBookmarkText = "Remove Boorkmark";
	void setMakeBookemarkPinButton(boolean pinFlag) {
		Button bn = (Button) findViewById(R.id.view_question_btnPin);
		if (pinFlag) {
			bn.setText(bookmarkText);
		} else {
			bn.setText(unBookmarkText);
		}
		isBookmarked = pinFlag;
	}

	void attachPinFlagButtons() {
		// Following a question callback.
		Buttonbookmark = (Button) findViewById(R.id.view_question_btnPin);
		Buttonbookmark.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (isBookmarked) {
					Buttonbookmark.setEnabled(false);
					BookmarkData = ParseOperation.AddFollow(
							getApplicationContext(), BookmarkData);
					setMakeBookemarkPinButton(false);
					SharedData.refreshThisTab[DashboardActivity.PERSONAL_QUESTIONS] = true;
					Buttonbookmark.setEnabled(true);

				} else {
					Buttonbookmark.setEnabled(false);
					ParseOperation.RemoveFollow(getApplicationContext(),
							BookmarkData);
					setMakeBookemarkPinButton(true);
					BookmarkData = null;
					SharedData.refreshThisTab[DashboardActivity.PERSONAL_QUESTIONS] = true;
					Buttonbookmark.setEnabled(true);
				}
			}
		});
	}

	void setTotalAndPlottable(final List<ParseObject> returnedData,
			String demog, String[] demoData) {
		List<double[]> voterData = Helper.convertChunkToList(returnedData,
				demog, demoData.length);
		bufferVoteRealValues = voterData;
		// Add them up
		total_votes = VoteMathHelper.sumAll2DElements(bufferVoteRealValues);

		// Create a percentage based array for the votes to
		// plot.
		bufferPlotValues = VoteMathHelper.divideAll2DElementsAndRotate(
				bufferVoteRealValues, total_votes);
	}

	static List<double[]> bufferVoteRealValues = null;
	List<double[]> bufferPlotValues = null;
	static int total_votes = 0;
	public static int maxPercentageElement = 0;
	
	// Displays the result charts.
	void showAllCharts(List<ParseObject> plotData) {

		// Update the total votes data entry in the hashmap by calling
		// the current total votes in an async call.
		ParseObject po = SharedData.focus_question;
		ArrayList<ParseObject> myList = new ArrayList<ParseObject>();
		myList.add(po);

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(SharedData.ALL_QUESTION_VOTE_TABLE);
		query.whereContainedIn("question_id", myList);
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {
					TextView tv = (TextView) findViewById(R.id.view_question_totalVotes);
					tv.setText(tv.getText().toString() + " "
							+ objects.get(0).getInt("total_votes"));
					ParseOperation.UpdateTotalVoteHashMap(objects);
					DashboardActivity.updateVoteCountInAllVisibleTabs();
				}
			}
		});

		// Start plotting the data.
		setTotalAndPlottable(plotData, "gender", DemographicValues.genders);

		// you can use any demographic to calculate this, but im
		// just using gender the result should be the same since
		// male + female vote should be equal to all the
		// continent vote, Africa+Asia+Europe+... votes.
		int[] totalVoterPerOption = VoteMathHelper
				.sumEachOptionElements(bufferVoteRealValues);

		// Gets all the options of the question in a string
		// array.
		String[] extractedQuestionOptions = VoteMathHelper
				.getFocusQuestionOptions(getApplicationContext());
		// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		// CHART: Total vote count.>>>>>>>>>>>>>>>>>>
		GraphicalView pieChart = ChartMaker.showPercentagePieChart(
				getApplicationContext(),
				(RelativeLayout) findViewById(R.id.first_chart),
				totalVoterPerOption, extractedQuestionOptions,
				"Total Vote Distribution");
		chartsForShare.put(KEY_VOTER_CHART, pieChart);
		attachFullScreenListener(pieChart, KEY_VOTER_CHART);
		// attachToShareButton(R.id.view_question_shareFirstChart, 0);

		LinearLayout chart = (LinearLayout) findViewById(R.id.second_chart);

		if (SharedData.current_user.getInt("gender") != 0) {
			// Gender Chart.
			GraphicalView genderChart = ChartMaker.showGenderChart(
					getApplicationContext(),
					(LinearLayout) findViewById(R.id.second_chart),
					bufferPlotValues, extractedQuestionOptions.length, true,
					"Gender Based Distribution",
					extractedQuestionOptions.length, maxPercentageElement, -1, "Options",
					"Percentage");
			chartsForShare.put(KEY_GENDER_CHART, genderChart);
			attachFullScreenListener(genderChart, KEY_GENDER_CHART);
			// attachToShareButton(R.id.view_question_shareGenderChart, 1);
		} else {
			TextView valueTV = Helper
					.getSimpleTextview(getApplicationContext());
			valueTV.setText("Specify gender in profile to view gender based results.");
			chart.addView(valueTV);
		}

		chart = (LinearLayout) findViewById(R.id.age_chart);
		if (SharedData.current_user.getInt("age") != 0) {
			// Age Chart.
			setTotalAndPlottable(plotData, "age", DemographicValues.ages);
			GraphicalView ageChart = ChartMaker.showAgeChart(
					getApplicationContext(),
					(LinearLayout) findViewById(R.id.age_chart),
					bufferPlotValues, extractedQuestionOptions.length, true,
					"Age Based Distribution", extractedQuestionOptions.length,
					maxPercentageElement, -1, "Options", "Percentage");
			chartsForShare.put(KEY_AGE_CHART, ageChart);
			attachFullScreenListener(ageChart, KEY_AGE_CHART);
			// attachToShareButton(R.id.view_question_shareAgeChart, 2);
		} else {
			TextView valueTV = Helper
					.getSimpleTextview(getApplicationContext());
			valueTV.setText("Specify age in profile to view age based results.");
			chart.addView(valueTV);
		}

		chart = (LinearLayout) findViewById(R.id.continent_chart);
		if (SharedData.current_user.getInt("continent") != 0) {
			// Continent Chart.
			setTotalAndPlottable(plotData, "continent",
					DemographicValues.continents);
			GraphicalView continentChart = ChartMaker.showContinentChart(
					getApplicationContext(),
					(LinearLayout) findViewById(R.id.continent_chart),
					bufferPlotValues, extractedQuestionOptions.length, true,
					"Continent Based Distribution",
					extractedQuestionOptions.length, maxPercentageElement, -1, "Options",
					"Percentage");
			chartsForShare.put(KEY_CONTINENT_CHART, continentChart);
			attachFullScreenListener(continentChart, KEY_CONTINENT_CHART);
			// attachToShareButton(R.id.view_question_shareContinentChart, 3);
		} else {
			TextView valueTV = Helper
					.getSimpleTextview(getApplicationContext());
			valueTV.setText("Specify continent in profile to view continent based results.");
			chart.addView(valueTV);
		}

		chart = (LinearLayout) findViewById(R.id.education_chart);
		if (SharedData.current_user.getInt("education") != 0) {

			// Education Chart.
			setTotalAndPlottable(plotData, "education",
					DemographicValues.educations);
			GraphicalView educationChart = ChartMaker.showEducationChart(
					getApplicationContext(),
					(LinearLayout) findViewById(R.id.education_chart),
					bufferPlotValues, extractedQuestionOptions.length, true,
					"Education Based Distribution",
					extractedQuestionOptions.length, maxPercentageElement , -1, "Options",
					"Percentage");
			chartsForShare.put(KEY_EDUCATION_CHART, educationChart);
			attachFullScreenListener(educationChart, KEY_EDUCATION_CHART);
			// attachToShareButton(R.id.view_question_shareEducationChart, 4);
		} else {
			TextView valueTV = Helper
					.getSimpleTextview(getApplicationContext());
			valueTV.setText("Specify education in profile to view education based results.");
			chart.addView(valueTV);
		}

		chart = (LinearLayout) findViewById(R.id.race_chart);
		if (SharedData.current_user.getInt("race") != 0) {

			// Race Chart
			setTotalAndPlottable(plotData, "race", DemographicValues.races);
			GraphicalView raceChart = ChartMaker.showRaceChart(
					getApplicationContext(),
					(LinearLayout) findViewById(R.id.race_chart),
					bufferPlotValues, extractedQuestionOptions.length, true,
					"Race Based Distribution", extractedQuestionOptions.length,
					maxPercentageElement, -1, "Options", "Percentage");
			chartsForShare.put(KEY_RACE_CHART, raceChart);
			attachFullScreenListener(raceChart, KEY_RACE_CHART);
			//
			// Enable the share button.
			// attachToShareButton(R.id.view_question_shareRaceChart, 5);
		} else {
			TextView valueTV = Helper
					.getSimpleTextview(getApplicationContext());
			valueTV.setText("Specify race in profile to view race based results.");
			chart.addView(valueTV);
		}

		chart = (LinearLayout) findViewById(R.id.religion_chart);
		if (SharedData.current_user.getInt("religion") != 0) {
			// Religion Chart
			setTotalAndPlottable(plotData, "religion",
					DemographicValues.religion);
			GraphicalView religionChart = ChartMaker.showReligionChart(
					getApplicationContext(),
					(LinearLayout) findViewById(R.id.religion_chart),
					bufferPlotValues, extractedQuestionOptions.length, true,
					"Belief Based Distribution",
					extractedQuestionOptions.length, maxPercentageElement , -1, "Options",
					"Percentage");
			chartsForShare.put(KEY_RELIGION_CHART, religionChart);
			attachFullScreenListener(religionChart, KEY_RELIGION_CHART);
		} else {
			TextView valueTV = Helper
					.getSimpleTextview(getApplicationContext());
			valueTV.setText("Specify belief in profile to view belief based results.");
			chart.addView(valueTV);
		}

		// Enable the share button.
		// attachToShareButton(R.id.view_question_shareReligionChart, 6);

		// Turn on the multi-share button
		/*
		 * Button bn = (Button)
		 * findViewById(R.id.view_question_shareMultiChoiceChart);
		 * bn.setEnabled(true); bn.setOnClickListener(new View.OnClickListener()
		 * { public void onClick(View v) {
		 * DialogueGenerator.showChartShareSelectDialogue(v,
		 * downloadedQuestionImage, chartsForShare); } });
		 */
	}
	// Shows the chart in full screen.
	void attachFullScreenListener(GraphicalView gv, final String index) {
		gv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// On clicking a chart, just show it in fullscreen activity.
				Bitmap showme = chartsForShare.get(index).toBitmap();
				FullImageActivity.questionImage = showme;
				Intent i = new Intent();
				i.setClass(getApplicationContext(), FullImageActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		});
	}
	void UpdateUserInterface() {
		boolean val = SharedData.focus_question.getBoolean("has_image");
		if (val == true) {
			ImageView iv = (ImageView) findViewById(R.id.view_question_question_image);

			downloadedQuestionImage = SharedData.dowloadedImageMap
					.get(SharedData.focus_question.getObjectId());
			iv.setImageBitmap(downloadedQuestionImage);
		}

		TextView tv = (TextView) findViewById(R.id.view_question_statement);

		String statementWithCaps = Helper
				.setFirstCharacterToCaps(SharedData.focus_question.get(
						"statement").toString());
		tv.setText(statementWithCaps);

		tv = (TextView) findViewById(R.id.view_question_category);
		tv.setText("Category: "
				+ DemographicValues.categories[SharedData.focus_question
						.getInt("category")]);

		tv = (TextView) findViewById(R.id.view_question_postDate);
		Date printDate = SharedData.focus_question.getCreatedAt();
		tv.setText("Posted on: " + DateFormat.format("dd MMM yyyy", printDate));

		// Generate the radio buttons dynamically.
		JSONArray parent_array = (JSONArray) SharedData.focus_question
				.getJSONArray("options_list");
		try {
			// Total options for this question.
			int total_options = parent_array.getJSONArray(0).length();
			int i = 0;
			int counter = 0;
			for (; i < total_options; i++) {
				String data = parent_array.getJSONArray(0).getString(i);
				addRadioButton(counter, data);
				counter++;
			}
			// And one extra for the last option since its not present in the
			// options_list. Its index is separately stored in a variable.
			// int last_option_index = SharedData.focus_question
			// .getInt("option_last");
			// addRadioButton(counter,
			// DemographicValues.last_options[last_option_index]);

			// Get and set the values.
			// rb.setText(parent_array.getJSONArray(0).getString(0));
		} catch (JSONException e) {
			Helper.ShowDialogue("JSON Read Error", "Cannot read options",
					getApplicationContext());
		}

		// Get the information about the vote casted on this question and update
		// the UI accordingly.
		// ParseObject userVote = null;
		VoteData = ParseOperation
				.GetUserVotedOnQuestion(SharedData.focus_question);
		Button bn = (Button) findViewById(R.id.view_question_btnVote);
		RadioGroup rg = (RadioGroup) findViewById(R.id.view_question_radiogrp_answers);

		bn.setEnabled(true);
		setAllRadioGroup(true);

		if (VoteData != null) {
			if (VoteData.getInt("option_picked") != -1) {
				// The user has already voted on the question.
				bn.setEnabled(false);

				// Setup all the data, input to charts and display.
				optionSelected = VoteData.getInt("option_picked");
				rg.check(optionSelected);
				setAllRadioGroup(false);

				List<ParseObject> data = ParseOperation
						.FetchUserVoteData(SharedData.focus_question
								.getObjectId());
				if (data.size() != 0) {
					showAllCharts(data);
				}
			}
		}

		// The bookmark button needs to be fixed accordingly
		updatePinButton();
	}
	void updatePinButton() {
		BookmarkData = ParseOperation
				.GetUserBookmarkOnQuestion(SharedData.focus_question);
		if (BookmarkData == null) {
			setMakeBookemarkPinButton(true);
		} else {
			// Check to see if the value exists for the pin/bookmark
			if (BookmarkData.getInt("is_bookmarked") > 0) {
				setMakeBookemarkPinButton(false);
			} else {
				setMakeBookemarkPinButton(true);
			}
		}
	}

	void setAllRadioGroup(boolean input) {
		RadioGroup rg = (RadioGroup) findViewById(R.id.view_question_radiogrp_answers);
		for (int i = 0; i < rg.getChildCount(); i++) {
			rg.getChildAt(i).setEnabled(input);
		}

		// Also get the results and plot them if the input is being turned off
		// since now the user has voted
		if (input == false) {
			LinearLayout resultsView = (LinearLayout) findViewById(R.id.view_question_results_panel);
			resultsView.setVisibility(View.VISIBLE);
			TextView resultsHint = (TextView) findViewById(R.id.view_question_results_hint);
			resultsHint.setVisibility(View.GONE);

		}
	}
	public void addRadioButton(int index, String value) {
		RadioButton rb = new RadioButton(this);
		rb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		rb.setId(index);
		rb.setEnabled(false);
		RadioGroup rg = (RadioGroup) findViewById(R.id.view_question_radiogrp_answers);
		rb.setText(AnswerTemplates.questionIndex[index] + value);
		arrQuestionOptions.add(rb);
		rg.addView(rb, layout_params);
	}

	void setImageViewVisibility(int i) {
		ImageView iv = (ImageView) findViewById(R.id.view_question_question_image);

		if (i == View.INVISIBLE) {
			// iv.setLayoutParams(new LayoutParams(0, 0));
			iv.setVisibility(View.INVISIBLE);
			iv.setClickable(false);
		} else {
			// iv.setLayoutParams(layout_params);
			iv.setVisibility(View.VISIBLE);
			iv.setClickable(true);
		}
	}

	void attachToShareButton(int buttonId, final int chartIndex) {
		// Voter distribution.
		Button bn = (Button) findViewById(buttonId);
		bn.setEnabled(true);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ShareHelper.stitchAndShare(v.getContext(),
						downloadedQuestionImage, chartsForShare.get(chartIndex)
								.toBitmap());
			}
		});
	}

	// Downloads the image that is associated with the question.
	public void DownloadNonBlockingQuestionImage(ParseObject question,
			Context con) {
		final ParseFile question_image = (ParseFile) question.get("image");
		question_image.getDataInBackground(new GetDataCallback() {
			public void done(byte[] data, com.parse.ParseException e) {
				if (e == null) {
					Bitmap image_bitmap = Helper.ConvertByteArrayToBitmap(data);
					setQuestionImage(image_bitmap);
				} else {
					// something went wrong
				}
			}
		});

	}

	void setQuestionImage(Bitmap qImage) {
		ImageView iv = (ImageView) findViewById(R.id.view_question_question_image);
		iv.setImageBitmap(qImage);
		downloadedQuestionImage = qImage;
		setImageViewVisibility(View.VISIBLE);
	}

	public void showPercentagePieChart(LinearLayout layout, int[] iValues,
			String[] iOptions) {
		// Color set used for the output latency pie chart.
		int[] COLORS = new int[]{ColorPallets.simple[0],
				ColorPallets.simple[1], ColorPallets.simple[2],
				ColorPallets.simple[3], ColorPallets.simple[4],
				ColorPallets.simple[5], ColorPallets.simple[6]};

		// AChart API objects used to render the chart correctly.
		CategorySeries mSeries = new CategorySeries("");
		DefaultRenderer mRenderer = new DefaultRenderer();

		// Set up the chart parameters
		mRenderer.setApplyBackgroundColor(false);
		mRenderer.setChartTitleTextSize(50);
		mRenderer.setLabelsColor(Color.BLACK);
		mRenderer.setLabelsTextSize(30);
		mRenderer.setShowLegend(false);
		// mRenderer.setMargins(new int[]{20, 30, 15, 0});
		mRenderer.setZoomButtonsVisible(true);
		mRenderer.setStartAngle(-90);
		mRenderer.setChartTitle("Title");

		float total = 0;
		for (int i = 0; i < iValues.length; i++) {
			total = total + iValues[i];
		}

		GraphicalView mChartView;
		mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
		mRenderer.setClickEnabled(true);
		mRenderer.setSelectableBuffer(100);
		mChartView = ChartFactory.getPieChartView(this, mSeries, mRenderer);
		layout.addView(mChartView);

		DecimalFormat df = new DecimalFormat("#.0");

		// The new values are added to the chart engine.
		for (int i = 0; i < iValues.length; i++) {
			mSeries.add(df.format(iValues[i] / total * 100) + "%" + "---"
					+ (char) (65 + i) + ": " + iOptions[i], iValues[i]);
			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			renderer.setColor(COLORS[i]);
			mRenderer.addSeriesRenderer(renderer);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_activity_view_questions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.view_questions_share) {
			DialogueGenerator.showChartShareSelectDialogue(
					this.findViewById(R.id.view_question_layout_top),
					downloadedQuestionImage, chartsForShare,
					getApplicationContext());
		}
		return super.onOptionsItemSelected(item);
	}
}
