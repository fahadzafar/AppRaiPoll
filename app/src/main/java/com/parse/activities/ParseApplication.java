package com.parse.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.parse.Parse;
import com.parse.ParseACL;

import com.parse.ParseUser;
import com.parse.chart.ChartMaker;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;

import android.app.Application;

public class ParseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Add your initialization code here
		Parse.initialize(this, Helper.ApplicationId_, Helper.ClientId_);
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();

		// If you would like all objects to be private by default, remove this
		// line.
		defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);

		// Resizing of the chart properties based on screen resolution.
		int val = (int) getResources().getDisplayMetrics().density;
		if (val < 3) {
			ChartMaker.LABEL_TEXT_SIZE = 15;
			ChartMaker.LEGEND_TEXT_SIZE = 20;
			ChartMaker.AXIS_TITLE_TEXT_SIZE = 25;
			ChartMaker.TITLE_TEXT_SIZE = 25;
			ChartMaker.MARGINS = new int[]{75, 50, 50, 50}; // Margin order: top,
															// left, bottom,
															// right
			ChartMaker.COMMUNITY_BAR_WIDTH = 50;
			ChartMaker.PERCENTAGE_LABEL_TEXT_SIZE = 25;
		} else if (val >= 3) {
			ChartMaker.LABEL_TEXT_SIZE = 30;
			ChartMaker.LEGEND_TEXT_SIZE = 30;
			ChartMaker.AXIS_TITLE_TEXT_SIZE = 30;
			ChartMaker.TITLE_TEXT_SIZE = 50;
			ChartMaker.MARGINS = new int[]{100, 100, 100, 100}; // Margin order:
																// top, left,
																// bottom, right
			ChartMaker.COMMUNITY_BAR_WIDTH = 100;
			ChartMaker.PERCENTAGE_LABEL_TEXT_SIZE = 30;
		}
		try {
			SharedData.defaultDateOfBirth =  new SimpleDateFormat("yyyy/MM/dd").parse("3000/01/01");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
