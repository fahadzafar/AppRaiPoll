package com.parse.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;

public class SuggestionActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.suggestion);

		Button bn = (Button) findViewById(R.id.about_btn_back);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SubmitUserSuggestion();
			}
		});
	}
	// Submit the user suggestion/comments/feedback.
	public void SubmitUserSuggestion() {

		EditText ev = (EditText) findViewById(R.id.suggestion_userdata);
		String data = ev.getText().toString();
		if (!data.equals("")) {
			ParseObject suggestion = new ParseObject("Suggestion");
			suggestion.put("user_id", SharedData.current_user);
			suggestion.put("suggestion_text", data);

			suggestion.saveInBackground(new SaveCallback() {
				public void done(com.parse.ParseException e) {
					Helper.ShowDialogue("Feedback submitted", "success",
							getApplicationContext());
				}
			});
		}
		finish();
	}
}
