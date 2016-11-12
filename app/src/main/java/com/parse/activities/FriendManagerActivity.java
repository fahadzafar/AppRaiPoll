package com.parse.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FriendManagerActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_manager);

		Button bn = (Button) findViewById(R.id.friends_manager_btn_add_fb);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});
		
		bn = (Button) findViewById(R.id.friends_manager_btn_add_tw);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});
		
		bn = (Button) findViewById(R.id.friends_manager_btn_add_myapp);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			}
		});

	}
}
