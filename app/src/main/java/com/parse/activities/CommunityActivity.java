package com.parse.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.parse.chart.ChartMaker;
import com.parse.helper.Helper;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseAppStatisticsHelper;

public class CommunityActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.community);

		// Setup all the data, input to charts and display.
		showAllCharts();
	}
	public void showAllCharts() {
		// Show the first chart that displays total users, total votes, total
		// questions
		int[] data = ParseAppStatisticsHelper.getAppStatistics();
		List<double[]> inputFormat = new ArrayList<double[]>();
		inputFormat.add(new double[]{data[0]});
		inputFormat.add(new double[]{data[1]});
		inputFormat.add(new double[]{data[2]});

		String[] dataLabels = new String[]{"Total Users",
				"Total Questions Posted", "Total Votes Cast"};

		// Send this to set the limits of the y-axis
		double yMax = Helper.max(data[0], data[1], data[2]);
		ChartMaker.showAppStatisticsChart(getApplicationContext(),
				(LinearLayout) findViewById(R.id.community_voter_number_chart),
				inputFormat, dataLabels, "App Statistics", yMax);

		// Show the Gender Chart
		inputFormat.clear();
		inputFormat.add(new double[]{data[39]});
		inputFormat.add(new double[]{data[3]});
		inputFormat.add(new double[]{data[4]});
		ChartMaker.showGenderChart(this,
				(LinearLayout) findViewById(R.id.community_gender_chart),
				inputFormat, 0, false, "Gender Based Distribution", 1,
				Helper.max(data[3], data[4], data[39]),
				ChartMaker.COMMUNITY_BAR_WIDTH, "Voters", "Count");

		// Show the Age Chart
		inputFormat.clear();
		inputFormat.add(new double[]{data[40]});
		inputFormat.add(new double[]{data[5]});
		inputFormat.add(new double[]{data[6]});
		inputFormat.add(new double[]{data[7]});
		inputFormat.add(new double[]{data[8]});
		inputFormat.add(new double[]{data[9]});
		inputFormat.add(new double[]{data[10]});
		ChartMaker.showAgeChart(this,
				(LinearLayout) findViewById(R.id.community_age_chart),
				inputFormat, 0, false, "Age Based Distribution", 1, Helper.max(
						data[5], data[6], data[7], data[8], data[9], data[10],
						data[40]), ChartMaker.COMMUNITY_BAR_WIDTH, "Voters",
				"Count");

		// Show the continent Chart
		inputFormat.clear();
		inputFormat.add(new double[]{data[41]});
		inputFormat.add(new double[]{data[11]});
		inputFormat.add(new double[]{data[12]});
		inputFormat.add(new double[]{data[13]});
		inputFormat.add(new double[]{data[14]});
		inputFormat.add(new double[]{data[15]});
		inputFormat.add(new double[]{data[16]});
		inputFormat.add(new double[]{data[17]});
		ChartMaker.showContinentChart(this,
				(LinearLayout) findViewById(R.id.community_continent_chart),
				inputFormat, 0, false, "Continent Based Distribution", 1,
				Helper.max(data[11], data[12], data[13], data[14], data[15],
						data[16], data[17], data[41]),
				ChartMaker.COMMUNITY_BAR_WIDTH, "Voters", "Count");

		// Show the education Chart
		inputFormat.clear();
		inputFormat.add(new double[]{data[42]});
		inputFormat.add(new double[]{data[18]});
		inputFormat.add(new double[]{data[19]});
		inputFormat.add(new double[]{data[20]});
		inputFormat.add(new double[]{data[21]});
		inputFormat.add(new double[]{data[22]});
		inputFormat.add(new double[]{data[23]});
		inputFormat.add(new double[]{data[24]});
		ChartMaker.showEducationChart(this,
				(LinearLayout) findViewById(R.id.community_education_chart),
				inputFormat, DemographicValues.educations.length - 1, false,
				"Education Based Distribution", 1, Helper.max(data[18],
						data[19], data[20], data[21], data[22], data[23],
						data[24], data[42]), ChartMaker.COMMUNITY_BAR_WIDTH,
				"Voters", "Count");

		// Show the race Chart
		inputFormat.clear();
		inputFormat.add(new double[]{data[43]});
		inputFormat.add(new double[]{data[25]});
		inputFormat.add(new double[]{data[26]});
		inputFormat.add(new double[]{data[27]});
		inputFormat.add(new double[]{data[28]});
		inputFormat.add(new double[]{data[29]});
		inputFormat.add(new double[]{data[30]});
		inputFormat.add(new double[]{data[49]});
		inputFormat.add(new double[]{data[50]});
		ChartMaker.showRaceChart(this,
				(LinearLayout) findViewById(R.id.community_race_chart),
				inputFormat, 0, false, "Race Based Distribution", 1, Helper
						.max(data[25], data[26], data[27], data[28], data[29],
								data[30], data[43], data[49],data[50]),
				ChartMaker.COMMUNITY_BAR_WIDTH, "Voters", "Count");

		// Show the religion Chart
		inputFormat.clear();
		inputFormat.add(new double[]{data[44]});
		inputFormat.add(new double[]{data[31]});
		inputFormat.add(new double[]{data[32]});
		inputFormat.add(new double[]{data[33]});
		inputFormat.add(new double[]{data[34]});
		inputFormat.add(new double[]{data[35]});
		inputFormat.add(new double[]{data[36]});
		inputFormat.add(new double[]{data[37]});
		inputFormat.add(new double[]{data[38]});
		inputFormat.add(new double[]{data[45]});
		inputFormat.add(new double[]{data[46]});
		inputFormat.add(new double[]{data[47]});
		inputFormat.add(new double[]{data[48]});
		ChartMaker.showReligionChart(this,
				(LinearLayout) findViewById(R.id.community_religion_chart),
				inputFormat, 0, false, "Religion Based Distribution", 1, Helper
						.max(data[25], data[26], data[27], data[28], data[29],
								data[30], data[44], data[45], data[46],
								data[47], data[48]),
				50, "Voters", "Count");

	}
}
