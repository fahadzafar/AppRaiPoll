package com.parse.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AboutUsActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aboutus);
		
		TextView mLearnMoreView = (TextView) findViewById(R.id.about_us_learn_more);
		mLearnMoreView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(),
						LearnMoreActivity.class);
				startActivity(i);
			}
		});
	}
}
