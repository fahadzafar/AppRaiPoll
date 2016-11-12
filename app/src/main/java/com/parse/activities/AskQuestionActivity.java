package com.parse.activities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
//import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.activities.R.id;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.AnswerTemplates;
import com.parse.objects.DemographicValues;
import com.parse.objects.Question;
import com.parse.operation.ParseAppStatisticsHelper;

public class AskQuestionActivity extends Activity {
	public static final int MAX_OPTION_CHAR_LIMIT = 40;
	public static final int MAX_QUESTION_CHAR_LIMIT = 200;
	public static final int MAX_OPTION_NO_LIMIT = 6;
	public static String WARNING_PRE_PRE_MESSAGE = "Option ";
	public static String WARNING_PRE_MESSAGE = ":                                                   (";

	private static final int RESULT_TAKEPHOTO = 1;
	private static final int RESULT_GALLERYPHOTO = 2;

	Button post_button_;
	Button add_option_;
	Button remove_option;
	Spinner answerTemplates, categorySpinner;

	ProgressDialog progress_dialogue_;

	ImageView question_image_;
	Bitmap image_bitmap_;

	public static ArrayList<EditText> arrQuestionOptions;
	public static ArrayList<TextView> arrWarningMessage;

	EditText txtQuestion, txtOption1, txtOption2;
	TextView tvQuestion, tvOption1, tvOption2;

	EditText editPollText;

	LinearLayout linear_layout;
	LayoutParams layout_params;

	public static final int OPTION_COLOR = Color.rgb(180, 180, 180);

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ask_question);

		// Sets up actionbar
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		linear_layout = (LinearLayout) findViewById(R.id.ask_question_linearlayout);
		layout_params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		arrQuestionOptions = new ArrayList<EditText>();
		arrWarningMessage = new ArrayList<TextView>();

		// Creates edit text for the poll question
		editPollText = (EditText) findViewById(R.id.ask_question_editpoll);
		editPollText.setBackgroundColor(Color.rgb(200, 200, 200));
		editPollText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(
				MAX_QUESTION_CHAR_LIMIT)});

		// prevents assholes from spamming enters
		editPollText.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {
				for (int i = s.length(); i > 0; i--) {

					if (s.subSequence(i - 1, i).toString().equals("\n"))
						s.replace(i - 1, i, "");

				}

			}
		});

		// Adds 6 options
		for (int i = 0; i < 2; i++) {
			this.addNewOption("", i);
		}

		// Setup the image for the question.
		image_bitmap_ = null;
		question_image_ = (ImageView) findViewById(R.id.ask_question_image);
		question_image_.setOnClickListener(imageHandleListener);
		question_image_.setAdjustViewBounds(true);

		// Adds template spinner
		addTemplates();
		// Adds category spinner
		addCategorySpinner();

		add_option_ = (Button) findViewById(R.id.ask_question_btn_AddOption);
		add_option_.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!OptionCheck()) {
					return;
				} else {
					addNewOption("", arrQuestionOptions.size());
				}
			}
		});

		remove_option = (Button) findViewById(R.id.ask_question_btn_removeOption);
		remove_option.setVisibility(View.INVISIBLE);
		remove_option.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				removeLastOption();
			}
		});
	}

	// This function adds the option templates
	private void addTemplates() {
		answerTemplates = (Spinner) findViewById(R.id.ask_question_spnr_ans_templates);

		List<String> list = new ArrayList<String>();
		list.add("Option Templates"); // Initial dummy entry

		for (String str : AnswerTemplates.answerTemplates) {
			list.add(str);
		}

		// Populate the spinner using a customized ArrayAdapter that hides the
		// first (dummy) entry
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list) {
			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				View v = null;

				// If this is the initial dummy entry, make it hidden
				if (position == 0) {
					TextView tv = new TextView(getContext());
					tv.setHeight(0);
					tv.setVisibility(View.GONE);
					v = tv;
				} else {
					// Pass convertView as null to prevent reuse of special case
					// views
					v = super.getDropDownView(position, null, parent);
				}

				// Hide scroll bar because it appears sometimes unnecessarily,
				// this does not prevent scrolling
				parent.setVerticalScrollBarEnabled(false);
				return v;
			}
		};

		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		answerTemplates.setAdapter(dataAdapter);

		// Function to handle filling option boxes with spinner values
		answerTemplates
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						// Clear the old options, separate the tokens and add
						// them to a list. Set the list to the new options one
						// by one.
						resetOptions();

						// This is needed because the first item in the spinner
						// is a dummy variable
						// Therefore we only add if the position is > 0
						if (answerTemplates.getSelectedItemPosition() > 1) {
							String data = answerTemplates.getSelectedItem()
									.toString();

							StringTokenizer st = new StringTokenizer(data, "/");
							List<String> options = new ArrayList<String>();

							while (st.hasMoreElements()) {
								options.add(st.nextElement().toString());
							}

							int i = 0;
							if (arrQuestionOptions.size() < options.size()) {
								for (i = arrQuestionOptions.size(); i < options
										.size(); i++) {
									addNewOption("", i);
								}
							} else if (arrQuestionOptions.size() > options
									.size()) {
								for (i = arrQuestionOptions.size(); i > options
										.size(); i--) {
									removeLastOption();
								}
							}

							for (i = 0; i < options.size(); i++) {
								arrQuestionOptions.get(i).setText(
										options.get(i));
							}

						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
	}

	// This function adds the option templates
	private void addCategorySpinner() {
		categorySpinner = (Spinner) findViewById(R.id.ask_question_spnr_categories);

		List<String> list = new ArrayList<String>();
		list.add("Category"); // Initial dummy entry

		for (String str : DemographicValues.categories) {
			list.add(str);
		}

		// Populate the spinner using a customized ArrayAdapter that hides the
		// first (dummy) entry
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list) {
			@Override
			public View getDropDownView(int position, View convertView,
					ViewGroup parent) {
				View v = null;

				// If this is the initial dummy entry, make it hidden
				if (position == 0) {
					TextView tv = new TextView(getContext());
					tv.setHeight(0);
					tv.setVisibility(View.GONE);
					v = tv;
				} else {
					// Pass convertView as null to prevent reuse of special case
					// views
					v = super.getDropDownView(position, null, parent);
				}

				// Hide scroll bar because it appears sometimes unnecessarily,
				// this does not prevent scrolling
				parent.setVerticalScrollBarEnabled(false);
				return v;
			}
		};

		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(dataAdapter);
	}

	// This is a simple code that adds new options
	public void addNewOption(String initialText, int counter) {
		// create character counting warning text view.
		TextView warningMessage = new TextView(getApplicationContext());
		warningMessage.setTextColor(OPTION_COLOR);
		warningMessage.setBackgroundColor(Color.rgb(255, 255, 255));
		warningMessage.setText(AskQuestionActivity.WARNING_PRE_PRE_MESSAGE
				+ (arrWarningMessage.size() + 1) + WARNING_PRE_MESSAGE
				+ AskQuestionActivity.MAX_OPTION_CHAR_LIMIT + ")");
		arrWarningMessage.add(warningMessage);

		// Create a temporary edit text
		EditText newEmptyOption = new EditText(getApplicationContext());
		newEmptyOption.setBackgroundColor(Color.rgb(200, 200, 200));
		newEmptyOption.setTextColor(Color.rgb(0, 0, 0));
		newEmptyOption
				.setFilters(new InputFilter[]{new InputFilter.LengthFilter(
						MAX_OPTION_CHAR_LIMIT)});

		newEmptyOption.setInputType(android.text.InputType.TYPE_CLASS_TEXT
				| android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
				| android.text.InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
		newEmptyOption.setText(initialText);

		// Attach a callback to the option.
		newEmptyOption.addTextChangedListener(new MyTextWatcher(
				arrWarningMessage.size() - 1));

		// This focus change listener gets used when a user types and moves
		// on to another option box, this gets called to check if the current
		// edit box is empty. If it is, it reset the color of the text view
		// to grey, otherwise the text view is black
		/*
		 * newEmptyOption.setOnFocusChangeListener(new OnFocusChangeListener() {
		 * public void onFocusChange(View v, boolean hasFocus) { if (!hasFocus)
		 * { resetEmptyOptions(); } } });
		 */
		resetEmptyOptions();

		newEmptyOption.requestFocus();
		arrQuestionOptions.add(newEmptyOption);

		// added to linear layout
		linear_layout.addView(warningMessage, layout_params);
		linear_layout.addView(newEmptyOption, layout_params);

		if (arrQuestionOptions.size() >= 3)
			remove_option.setVisibility(View.VISIBLE);

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		} else if (item.getItemId() == R.id.action_addoption) {
			// When user clicks on + button
			makeNewPost();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_activity_ask_questions, menu);

		// creates the spinner for categories in upper right
		// Spinner categorySpinner = (Spinner) menu
		// .findItem(R.id.category_spinner).getActionView();
		// addCategories(categorySpinner);

		return super.onCreateOptionsMenu(menu);
	}

	public void makeNewPost() {
		// Perform Sanity checks to see if the correct data has been
		// entered.
		if (!SanityChecks()) {
			return;
		}
		// post_button_.setEnabled(false);
		progress_dialogue_ = ProgressDialog.show(AskQuestionActivity.this, "",
				"Loading...", true, false);

		// Now setup and run the actual task behind the loading
		// dialogue box.
		new Thread(new Runnable() {
			@Override
			public void run() {
				Question question_data = GetRegistrationFormData();
				question_data.UploadAsParseObject(getApplicationContext());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						SharedData.refreshThisTab[DashboardActivity.PERSONAL_QUESTIONS] = true;
						SharedData.refreshThisTab[DashboardActivity.HOME_QUESTIONS] = true;
						ParseAppStatisticsHelper.recordQuestionCreated();
						progress_dialogue_.dismiss();
						Helper.ShowDialogue("Success",
								"Your question has been posted.",
								getApplicationContext());
						finish();
					}
				});
			}
		}).start();
	}

	// This gets called when a edit text loses focus and resets the back
	// ground of the text view to grey to show it hasn't be written
	void resetEmptyOptions() {
		for (int i = 0; i < arrQuestionOptions.size(); i++) {
			if (arrQuestionOptions.get(i).getText().length() == 0) {
				arrWarningMessage.get(i).setTextColor(OPTION_COLOR);
			}
		}
	}

	// Resets all text to empty strings and puts the text view color back into
	// grey
	void resetOptions() {
		for (EditText et : arrQuestionOptions) {
			et.setText("");
		}

		for (TextView tv : arrWarningMessage) {
			tv.setTextColor(OPTION_COLOR);
		}
	}

	// On the button click in the activity, it removes the last option.
	void removeLastOption() {
		if (arrQuestionOptions.size() == 0)
			return;

		if (arrQuestionOptions.size() == 2)
			remove_option.setVisibility(View.INVISIBLE);

		int removeIndex = arrQuestionOptions.size() - 1;
		linear_layout.removeView((View) arrQuestionOptions.get(removeIndex));
		linear_layout.removeView((View) arrWarningMessage.get(removeIndex));

		arrQuestionOptions.remove(removeIndex);
		arrWarningMessage.remove(removeIndex);

		if (arrQuestionOptions.size() <= 2)
			remove_option.setVisibility(View.INVISIBLE);
	}

	public boolean OptionCheck() {
		// 7 is the limit in number of max options allowed
		if (arrQuestionOptions.size() == MAX_OPTION_NO_LIMIT) {
			Helper.ShowDialogue("Notice", " Only " + MAX_OPTION_NO_LIMIT
					+ " maximum options allowed.", getApplicationContext());
			return false;
		}

		return true;
	}

	public boolean SanityChecks() {
		/*
		 * if (TextFieldIsEmpty(txtOption1)) { Helper.ShowDialogue("Error",
		 * "Please enter the first option.", getApplicationContext()); return
		 * false; }
		 */
		if (checkNumberOfOptionsMoreThanTwo() == false) {
			// Helper.ShowDialogue("Error!",
			// "Please enter at least two option.",
			// getApplicationContext());
			Toast errMessages = Toast.makeText(getApplicationContext(),
					"Please enter at least two options.", Toast.LENGTH_LONG);
			// errMessages.setGravity(Gravity.TOP, 0, 0);
			errMessages.show();
			return false;
		}

		if (TextFieldIsEmpty(editPollText)) {
			// Helper.ShowDialogue("Error!",
			// " Please enter the the poll statement.",
			// getApplicationContext());
			Toast errMessages = Toast.makeText(getApplicationContext(),
					"Please enter the the poll statement.", Toast.LENGTH_LONG);
			// errMessages.setGravity(Gravity.TOP, 0, 0);
			errMessages.show();
			return false;
		}

		// The current spinner hides index 0, as it is not a category, therefore
		// need a check here
		Spinner sp = (Spinner) findViewById(R.id.ask_question_spnr_categories);
		if (sp.getSelectedItemPosition() == 0) {
			// Helper.ShowDialogue("Error!",
			// " Please choose a category for poll",
			// getApplicationContext());
			Toast errMessages = Toast.makeText(getApplicationContext(),
					"Please choose a category for poll.", Toast.LENGTH_LONG);
			// errMessages.setGravity(Gravity.TOP, 0, 0);
			errMessages.show();
			return false;
		}
		return true;
	}

	boolean TextFieldIsEmpty(EditText te) {
		if (te.getText().toString().equals(""))
			return true;
		return false;
	}

	public Question GetRegistrationFormData() {
		Question new_question = new Question();

		String noCapsString = editPollText.getText().toString();
		new_question.statement = Helper.setFirstCharacterToNoCaps(noCapsString);
		new_question.posted_by = SharedData.current_user;

		// Check sanity of options and add to the array.
		new_question.user_entered_options = new JSONArray();

		// This dynamically adds all options, even if a user only writes option
		// 1 and 6,
		// it will still work by adding both as options
		for (EditText et : arrQuestionOptions) {
			if (et.getText().length() > 0) {
				new_question.user_entered_options.put(et.getText().toString());
			}
		}

		// Place the spinner values in the object for registration.
		// Last option and category indices.
		Spinner sp = (Spinner) findViewById(R.id.ask_question_spnr_categories);
		new_question.category = sp.getSelectedItemPosition() - 1;

		// Log.i("Han", sp.getSelectedItem().toString());

		if (image_bitmap_ != null) {
			new_question.has_image = true;
			new_question.image = image_bitmap_;
		}

		return new_question;
	}

	private boolean checkNumberOfOptionsMoreThanTwo() {
		int counter = 0;
		for (EditText et : arrQuestionOptions) {
			if (et.getText().length() > 0) {
				counter++;
			}
		}

		if (counter >= 2)
			return true;
		else
			return false;
	}
	private OnClickListener imageHandleListener = new OnClickListener() {
		public void onClick(View v) {
			CharSequence[] items;
			if (image_bitmap_ == null) {
				CharSequence[] twoOptions = {"Take a photo with camera",
						"Pick from gallery"};
				items = twoOptions;
			} else {
				CharSequence[] threeOptions = {"Take a photo with camera",
						"Pick from gallery", "Remove Picture"};
				items = threeOptions;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(
					AskQuestionActivity.this);
			builder.setTitle("Select photo source");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
						case 0 :
							// define the file-name to save photo taken by
							// Camera activity
							String fileName = "new-photo-name.jpg";
							// create parameters for Intent with filename
							ContentValues values = new ContentValues();
							values.put(MediaStore.Images.Media.TITLE, fileName);
							values.put(MediaStore.Images.Media.DESCRIPTION,
									"Image capture by camera");
							// imageUri is the current activity attribute,
							// define and save it for later usage (also in
							// onSaveInstanceState)
							imageUri = getContentResolver()
									.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
											values);
							// create new Intent
							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
							intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
							startActivityForResult(intent, RESULT_TAKEPHOTO);
							break;
						case 1 :
							// GET IMAGE FROM THE GALLERY
							Intent intent2 = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent2.setType("image/*");

							Intent chooser = Intent.createChooser(intent2,
									"Choose a Picture");
							startActivityForResult(chooser, RESULT_GALLERYPHOTO);
							break;
						case 2 :
							question_image_ = (ImageView) findViewById(R.id.ask_question_image);
							question_image_
									.setImageResource(R.drawable.select_image);
							image_bitmap_ = null;
							break;
					}
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	};

	Uri imageUri;
	public String getPath(Uri uri) {
		String res = null;
		String[] proj = {MediaStore.Images.Media.DATA};
		Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
		if (cursor.moveToFirst()) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			res = cursor.getString(column_index);
		}
		cursor.close();
		return res;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri selectedImageUri = null;
		String filePath = null;
		switch (requestCode) {
			case RESULT_TAKEPHOTO :
				if (resultCode == RESULT_OK) {
					// use imageUri here to access the image
					selectedImageUri = imageUri;
				} else if (resultCode == RESULT_CANCELED) {
					Toast.makeText(this, "Picture was not taken",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "Picture was not taken",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case RESULT_GALLERYPHOTO :

				if (data != null && data.getData() != null) {
					if (resultCode == Activity.RESULT_OK) {
						Uri mImageCaptureUri = data.getData();
						try {
							Bitmap bitmap = MediaStore.Images.Media
									.getBitmap(this.getContentResolver(),
											mImageCaptureUri);
							question_image_.setImageBitmap(bitmap);
							image_bitmap_ = bitmap;
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					break;
				}
				break;
		}

		if (selectedImageUri != null) {
			try {
				// OI FILE Manager
				String filemanagerstring = selectedImageUri.getPath();

				// MEDIA GALLERY
				String selectedImagePath = getPath(selectedImageUri);

				if (selectedImagePath != null) {
					filePath = selectedImagePath;
				} else if (filemanagerstring != null) {
					filePath = filemanagerstring;
				} else {
					Toast.makeText(getApplicationContext(), "Unknown path",
							Toast.LENGTH_LONG).show();
					Log.e("Bitmap", "Unknown path");
				}

				if (filePath != null) {
					// Toast.makeText(getApplicationContext(), " path" +
					// filePath,
					// Toast.LENGTH_LONG).show();
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
					Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
					question_image_.setImageBitmap(bitmap);
					image_bitmap_ = bitmap;

				}
			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "Internal error",
						Toast.LENGTH_LONG).show();
				Log.e(e.getClass().getName(), e.getMessage(), e);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	class MyTextWatcher implements TextWatcher {
		public int myWarningArrayMessageId;
		public MyTextWatcher(int i) {
			myWarningArrayMessageId = i;
		}

		@Override
		public void afterTextChanged(Editable s) {
			TextView tv = AskQuestionActivity.arrWarningMessage
					.get(myWarningArrayMessageId);

			EditText et = AskQuestionActivity.arrQuestionOptions
					.get(myWarningArrayMessageId);

			tv.setText(AskQuestionActivity.WARNING_PRE_PRE_MESSAGE
					+ (myWarningArrayMessageId + 1)
					+ AskQuestionActivity.WARNING_PRE_MESSAGE
					+ (AskQuestionActivity.MAX_OPTION_CHAR_LIMIT - et.getText()
							.toString().length()) + ")");

			// This sets the text view text color to black to show its being
			// used by user
			// Graphical touch
			tv.setTextColor(Color.BLACK);
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// TODO Auto-generated method stub

		}
	}
}
/*
 * 
 * // This adds the different categories listed in DemographicValues public void
 * addCategories(Spinner categorySpinner) { List<String> list = new
 * ArrayList<String>(); list.add("Categories"); // Initial dummy entry
 * 
 * for (String str : DemographicValues.categories) { list.add(str); }
 * 
 * // Populate the spinner using a customized ArrayAdapter that hides the //
 * first (dummy) entry ArrayAdapter<String> dataAdapter = new
 * ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list) {
 * 
 * @Override public View getDropDownView(int position, View convertView,
 * ViewGroup parent) { View v = null;
 * 
 * // If this is the initial dummy entry, make it hidden if (position == 0) {
 * TextView tv = new TextView(getContext()); tv.setHeight(0);
 * tv.setVisibility(View.GONE); v = tv; } else { // Pass convertView as null to
 * prevent reuse of special case // views v = super.getDropDownView(position,
 * null, parent); }
 * 
 * // Hide scroll bar because it appears sometimes unnecessarily, // this does
 * not prevent scrolling parent.setVerticalScrollBarEnabled(false); return v; }
 * };
 * 
 * dataAdapter
 * .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 * categorySpinner.setAdapter(dataAdapter); } public class AskQuestionActivity
 * extends FragmentActivity {
 * 
 * public static final int MAX_OPTION_CHAR_LIMIT = 40; public static final int
 * MAX_QUESTION_CHAR_LIMIT = 140; public static final int MAX_OPTION_NO_LIMIT =
 * 6; public static String WARNING_PRE_PRE_MESSAGE = "Option "; public static
 * String WARNING_PRE_MESSAGE =
 * ":                                                   (";
 * 
 * private static final int RESULT_TAKEPHOTO = 1; private static final int
 * RESULT_GALLERYPHOTO = 2;
 * 
 * Button post_button_; Button add_option_; Button remove_option; Spinner
 * answerTemplates;
 * 
 * ProgressDialog progress_dialogue_;
 * 
 * ImageView question_image_; Bitmap image_bitmap_;
 * 
 * public static ArrayList<EditText> arrQuestionOptions; public static
 * ArrayList<TextView> arrWarningMessage;
 * 
 * EditText txtQuestion, txtOption1, txtOption2; TextView tvQuestion, tvOption1,
 * tvOption2;
 * 
 * LinearLayout linear_layout; LayoutParams layout_params;
 * 
 * public static final int OPTION_COLOR = Color.rgb(180, 180, 180);
 * 
 * public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState); setContentView(R.layout.ask_question);
 * 
 * // Initialize array lists arrQuestionOptions = new ArrayList<EditText>();
 * arrWarningMessage = new ArrayList<TextView>();
 * 
 * // Getting the layout for dynamically adding options linear_layout =
 * (LinearLayout) findViewById(R.id.ask_question_linearlayout); layout_params =
 * new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 * 
 * // Setup the image for the question. image_bitmap_ = null; question_image_ =
 * (ImageView) findViewById(R.id.ask_question_image);
 * question_image_.setOnClickListener(imageHandleListener);
 * 
 * // Populate the categories spinner. Helper.PopulateSpinner( (Spinner)
 * findViewById(id.ask_question_spnr_categories), DemographicValues.categories);
 * 
 * // Populate the privacy type. Helper.PopulateSpinner( (Spinner)
 * findViewById(id.ask_question_spnr_privacy), DemographicValues.type);
 * 
 * // Populate the last option spinner. Helper.PopulateSpinner( (Spinner)
 * findViewById(id.ask_question_spnr_optionLast),
 * DemographicValues.last_options);
 * 
 * // Populate the option template spinner. answerTemplates = (Spinner)
 * findViewById(id.ask_question_spnr_ans_templates);
 * Helper.PopulateSpinner(answerTemplates, AnswerTemplates.answerTemplates);
 * answerTemplates .setOnItemSelectedListener(new
 * AdapterView.OnItemSelectedListener() {
 * 
 * @Override public void onItemSelected(AdapterView<?> arg0, View arg1, int
 * arg2, long arg3) { // Clear the old options, separate the tokens and add //
 * them to a list. Set the list to the new options one // by one.
 * resetOptions(); remove_option.setVisibility(View.INVISIBLE);
 * 
 * String data = answerTemplates.getSelectedItem() .toString(); if
 * (data.contains("autofill")) { add_option_.performClick();
 * add_option_.performClick(); return; } StringTokenizer st = new
 * StringTokenizer(data, "/"); List<String> options = new ArrayList<String>();
 * 
 * while (st.hasMoreElements()) { options.add(st.nextElement().toString()); }
 * for (int i = 0; i < options.size(); i++) { add_option_.performClick();
 * EditText et = arrQuestionOptions .get(arrQuestionOptions.size() - 1);
 * et.setText(options.get(i)); } }
 * 
 * @Override public void onNothingSelected(AdapterView<?> arg0) { } });
 * 
 * // Find and store the reference to the edit boxes and the labels. txtQuestion
 * = (EditText) findViewById(R.id.ask_question_); tvQuestion = (TextView)
 * findViewById(R.id.tvQuestion);
 * 
 * // attachTextChangeListners(); setupAddNewOptionBox("");
 * 
 * // The post question button callback. post_button_ = (Button)
 * findViewById(R.id.ask_question_btn_Post); post_button_.setOnClickListener(new
 * View.OnClickListener() { public void onClick(View v) { // Perform Sanity
 * checks to see if the correct data has been // entered. if (!SanityChecks()) {
 * return; } post_button_.setEnabled(false); progress_dialogue_ = ProgressDialog
 * .show(AskQuestionActivity.this, "", "Loading...", true, false);
 * 
 * // Now setup and run the actual task behind the loading // dialogue box. new
 * Thread(new Runnable() {
 * 
 * @Override public void run() { Question question_data =
 * GetRegistrationFormData(); question_data
 * .UploadAsParseObject(getApplicationContext()); runOnUiThread(new Runnable() {
 * 
 * @Override public void run() { progress_dialogue_.dismiss();
 * post_button_.setEnabled(true); Helper.ShowDialogue("Success",
 * "Your question has been posted.", getApplicationContext());
 * ParseAppStatisticsHelper .recordQuestionCreated(); finish(); } }); }
 * }).start(); // -------------- } });
 * 
 * // Setup the remove options button. remove_option = (Button)
 * findViewById(R.id.ask_question_btn_removeOption);
 * remove_option.setOnClickListener(new View.OnClickListener() { public void
 * onClick(View v) { removeLastOption(); } });
 * 
 * // Add two options be default. add_option_.performClick();
 * add_option_.performClick(); }
 * 
 * private void setupAddNewOptionBox(final String initText) { // The add option
 * button callback. add_option_ = (Button)
 * findViewById(R.id.ask_question_btn_AddOption);
 * add_option_.setOnClickListener(new View.OnClickListener() { public void
 * onClick(View v) { // Perform option limit check ( MAX_OPTION_NO_LIMIT) if
 * (!OptionCheck()) { return; }
 * 
 * // create character counting warning text view. TextView warningMessage = new
 * TextView(v.getContext()); warningMessage.setTextColor(OPTION_COLOR);
 * warningMessage .setText(AskQuestionActivity.WARNING_PRE_PRE_MESSAGE +
 * (arrWarningMessage.size() + 1) + WARNING_PRE_MESSAGE +
 * AskQuestionActivity.MAX_OPTION_CHAR_LIMIT + ")");
 * arrWarningMessage.add(warningMessage);
 * 
 * // Create a temporary edit text EditText newEmptyOption = new
 * EditText(txtQuestion.getContext()); newEmptyOption .setFilters(new
 * InputFilter[]{new InputFilter.LengthFilter( MAX_OPTION_CHAR_LIMIT)});
 * 
 * newEmptyOption
 * .setInputType(android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
 * newEmptyOption.setText(initText);
 * 
 * // Attach a callback to the option. newEmptyOption.addTextChangedListener(new
 * MyTextWatcher( arrWarningMessage.size() - 1));
 * 
 * arrQuestionOptions.add(newEmptyOption);
 * 
 * // added to linear layout linear_layout.addView(warningMessage,
 * layout_params); linear_layout.addView(newEmptyOption, layout_params);
 * 
 * if (arrQuestionOptions.size() > 2) {
 * remove_option.setVisibility(View.VISIBLE); } } }); }
 * 
 * void resetOptions() { linear_layout.removeAllViews();
 * arrQuestionOptions.clear(); arrWarningMessage.clear(); }
 * 
 * // On the button click in the activity, it removes the last option. void
 * removeLastOption() { if (arrQuestionOptions.size() == 0) return;
 * 
 * int removeIndex = arrQuestionOptions.size() - 1;
 * linear_layout.removeView((View) arrQuestionOptions.get(removeIndex));
 * linear_layout.removeView((View) arrWarningMessage.get(removeIndex));
 * 
 * arrQuestionOptions.remove(removeIndex);
 * arrWarningMessage.remove(removeIndex);
 * 
 * if (arrQuestionOptions.size() <= 2)
 * remove_option.setVisibility(View.INVISIBLE); }
 * 
 * private OnClickListener imageHandleListener = new OnClickListener() { public
 * void onClick(View v) { CharSequence[] items; if (image_bitmap_ == null) {
 * CharSequence[] twoOptions = {"Take a photo with camera",
 * "Pick from gallery"}; items = twoOptions; } else { CharSequence[]
 * threeOptions = {"Take a photo with camera", "Pick from gallery",
 * "Remove Picture"}; items = threeOptions; }
 * 
 * AlertDialog.Builder builder = new AlertDialog.Builder(
 * AskQuestionActivity.this); builder.setTitle("Select photo source");
 * builder.setItems(items, new DialogInterface.OnClickListener() { public void
 * onClick(DialogInterface dialog, int item) { switch (item) { case 0 : //
 * define the file-name to save photo taken by // Camera activity String
 * fileName = "newphotoname.png"; // create parameters for Intent with filename
 * ContentValues values = new ContentValues();
 * values.put(MediaStore.Images.Media.TITLE, fileName);
 * values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
 * // imageUri is the current activity attribute, // define and save it for
 * later usage (also in // onSaveInstanceState) imageUri = getContentResolver()
 * .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); // create new
 * Intent Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE);
 * intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
 * intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
 * startActivityForResult(intent, RESULT_TAKEPHOTO); break; case 1 : // GET
 * IMAGE FROM THE GALLERY Intent intent2 = new Intent(
 * Intent.ACTION_GET_CONTENT); intent2.setType("image/*");
 * 
 * Intent chooser = Intent.createChooser(intent2, "Choose a Picture");
 * startActivityForResult(chooser, RESULT_GALLERYPHOTO); break; case 2 :
 * question_image_ = (ImageView) findViewById(R.id.ask_question_image);
 * question_image_ .setImageResource(R.drawable.select_image); image_bitmap_ =
 * null; break; } } }); AlertDialog alert = builder.create(); alert.show(); } };
 * 
 * Uri imageUri; public String getPath(Uri uri) { String res = null; String[]
 * proj = {MediaStore.Images.Media.DATA}; Cursor cursor =
 * getContentResolver().query(uri, proj, null, null, null); if
 * (cursor.moveToFirst()) { int column_index = cursor
 * .getColumnIndexOrThrow(MediaStore.Images.Media.DATA); res =
 * cursor.getString(column_index); } cursor.close(); return res; }
 * 
 * @Override protected void onActivityResult(int requestCode, int resultCode,
 * Intent data) { Uri selectedImageUri = null; String filePath = null; switch
 * (requestCode) { case RESULT_TAKEPHOTO : if (resultCode == RESULT_OK) { // use
 * imageUri here to access the image selectedImageUri = imageUri; } else if
 * (resultCode == RESULT_CANCELED) { Toast.makeText(this,
 * "Picture was not taken", Toast.LENGTH_SHORT).show(); } else {
 * Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT).show(); }
 * break; case RESULT_GALLERYPHOTO :
 * 
 * if (data != null && data.getData() != null) { if (resultCode ==
 * Activity.RESULT_OK) { Uri mImageCaptureUri = data.getData(); try { Bitmap
 * bitmap = MediaStore.Images.Media .getBitmap(this.getContentResolver(),
 * mImageCaptureUri); question_image_.setImageBitmap(bitmap); image_bitmap_ =
 * bitmap; } catch (FileNotFoundException e) { // TODO Auto-generated catch
 * block e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
 * catch block e.printStackTrace(); }
 * 
 * } break; } break; }
 * 
 * if (selectedImageUri != null) { try { // OI FILE Manager String
 * filemanagerstring = selectedImageUri.getPath();
 * 
 * // MEDIA GALLERY String selectedImagePath = getPath(selectedImageUri);
 * 
 * if (selectedImagePath != null) { filePath = selectedImagePath; } else if
 * (filemanagerstring != null) { filePath = filemanagerstring; } else {
 * Toast.makeText(getApplicationContext(), "Unknown path",
 * Toast.LENGTH_LONG).show(); Log.e("Bitmap", "Unknown path"); }
 * 
 * if (filePath != null) { // Toast.makeText(getApplicationContext(), " path" +
 * // filePath, // Toast.LENGTH_LONG).show(); BitmapFactory.Options options =
 * new BitmapFactory.Options(); options.inPreferredConfig =
 * Bitmap.Config.ARGB_8888; Bitmap bitmap = BitmapFactory.decodeFile(filePath,
 * options); question_image_.setImageBitmap(bitmap); image_bitmap_ = bitmap;
 * 
 * } } catch (Exception e) { Toast.makeText(getApplicationContext(),
 * "Internal error", Toast.LENGTH_LONG).show(); Log.e(e.getClass().getName(),
 * e.getMessage(), e); } }
 * 
 * super.onActivityResult(requestCode, resultCode, data); }
 * 
 * public boolean OptionCheck() { // 7 is the limit in number of max options
 * allowed if (arrQuestionOptions.size() == MAX_OPTION_NO_LIMIT) {
 * Helper.ShowDialogue("Notice", " Only " + MAX_OPTION_NO_LIMIT +
 * " maximum options allowed.", getApplicationContext()); return false; }
 * 
 * return true; }
 * 
 * public boolean SanityChecks() { /* if (TextFieldIsEmpty(txtOption1)) {
 * Helper.ShowDialogue("Error", "Please enter the first option.",
 * getApplicationContext()); return false; }
 * 
 * if (TextFieldIsEmpty(arrQuestionOptions.get(0)) ||
 * TextFieldIsEmpty(arrQuestionOptions.get(1))) { Helper.ShowDialogue("Error!",
 * "Please enter at least two option.", getApplicationContext()); return false;
 * }
 * 
 * if (TextFieldIsEmpty(txtQuestion)) { Helper.ShowDialogue("Error!",
 * " Please enter the the question statement.", getApplicationContext()); return
 * false; } return true; }
 * 
 * boolean TextFieldIsEmpty(EditText te) { if
 * (te.getText().toString().equals("")) return true; return false; }
 * 
 * public Question GetRegistrationFormData() { Question new_question = new
 * Question();
 * 
 * String noCapsString = txtQuestion.getText().toString();
 * new_question.statement = Helper.setFirstCharacterToNoCaps(noCapsString);
 * new_question.posted_by = SharedData.current_user.getObjectId();
 * 
 * // Check sanity of options and add to the array. // TODO(HAN): Get dynamic
 * number of options and set their values here, // everything else should work
 * fine from here on. int hard_coded_options_length = arrQuestionOptions.size();
 * new_question.user_entered_options = new JSONArray();
 * 
 * for (int i = 0; i < hard_coded_options_length; i++) {
 * new_question.user_entered_options.put(arrQuestionOptions.get(i)
 * .getText().toString()); }
 * 
 * // Place the spinner values in the object for registration. // Last option
 * and category indices. Spinner sp = (Spinner)
 * findViewById(R.id.ask_question_spnr_categories); new_question.category =
 * sp.getSelectedItemPosition();
 * 
 * sp = (Spinner) findViewById(R.id.ask_question_spnr_optionLast);
 * new_question.option_last = sp.getSelectedItemPosition();
 * 
 * if (image_bitmap_ != null) { new_question.has_image = true;
 * new_question.image = image_bitmap_; }
 * 
 * return new_question; }
 * 
 * } class MyTextWatcher implements TextWatcher { public int
 * myWarningArrayMessageId; public MyTextWatcher(int i) {
 * myWarningArrayMessageId = i; }
 * 
 * @Override public void afterTextChanged(Editable s) { TextView tv =
 * AskQuestionActivity.arrWarningMessage .get(myWarningArrayMessageId);
 * 
 * EditText et = AskQuestionActivity.arrQuestionOptions
 * .get(myWarningArrayMessageId);
 * 
 * tv.setTextColor(AskQuestionActivity.OPTION_COLOR);
 * tv.setText(AskQuestionActivity.WARNING_PRE_PRE_MESSAGE +
 * (myWarningArrayMessageId + 1) + AskQuestionActivity.WARNING_PRE_MESSAGE +
 * (AskQuestionActivity.MAX_OPTION_CHAR_LIMIT - et.getText()
 * .toString().length()) + ")"); }
 * 
 * @Override public void beforeTextChanged(CharSequence s, int start, int count,
 * int after) { // TODO Auto-generated method stub }
 * 
 * @Override public void onTextChanged(CharSequence s, int start, int before,
 * int count) { // TODO Auto-generated method stub
 * 
 * }
 * 
 * }
 */