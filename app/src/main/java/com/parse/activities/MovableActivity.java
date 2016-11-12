package com.parse.activities;

import com.parse.helper.Helper;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MovableActivity extends Activity {

	ScrollView top;
	ScrollView bottom;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.movable);

		top = (ScrollView) findViewById(R.id.layout_top);
		bottom = (ScrollView) findViewById(R.id.layout_bottom);

		View v = (View) findViewById(R.id.layout_draggable);
		v.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent me) {
				if (me.getAction() == MotionEvent.ACTION_DOWN) {
					float oldXvalue = me.getX();
					float oldYvalue = me.getY();
					Log.i("myTag", "Action Down " + oldXvalue + "," + oldYvalue);
					Helper.ShowDialogue("down", "", getApplicationContext());
				} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
					int newYvalue = (int) me.getRawY();
					int oldYvalue = (int) (me.getY());

					// Log.i("myTag", "Action Down: old= " + oldYvalue +
					// ", new=" + newYvalue);
					// Log.i("v data", "height= " + top.getHeight());

					top.setLayoutParams(new LinearLayout.LayoutParams(v
							.getWidth(), Math.max(100, top.getHeight()
							+ oldYvalue)));

					Helper.ShowDialogue("moved", "", getApplicationContext());
				}
				return true;
			}
			// Implementation;
		});
	}
}