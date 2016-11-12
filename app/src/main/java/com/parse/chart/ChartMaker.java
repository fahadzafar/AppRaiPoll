package com.parse.chart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.helper.ColorPallets;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseOperation;

public class ChartMaker {
	public static int PERCENTAGE_LABEL_TEXT_SIZE = 35;
	public static int LABEL_TEXT_SIZE = 15;
	public static int LEGEND_TEXT_SIZE = 10;
	public static int AXIS_TITLE_TEXT_SIZE = 15;
	public static int TITLE_TEXT_SIZE = 15;
	public static int COMMUNITY_BAR_WIDTH = 70;
	public static int[] MARGINS = {1, 1, 1, 1};// Margin order: top, left,
												// bottom, right

	public static GraphicalView showPercentagePieChart(Context con,
			RelativeLayout layout, int[] iValues, String[] iOptions,
			String iTitle) {

		int[] COLORS = new int[]{ColorPallets.simple[0], ColorPallets.light[1],
				ColorPallets.light[2], ColorPallets.light[0],
				ColorPallets.light[4], ColorPallets.light[5],
				ColorPallets.light[6]};

		if (Helper.isSizeEqual(iOptions, iValues) == false) {
			ParseOperation.notifyCorruptQuestion(
					SharedData.focus_question.getObjectId(),
					"Cannot show percentagePieChart,iValues.length ="
							+ iValues.length + ",  iValues[0] = " + iValues[0]
							+ ", iOptions.length =" + iOptions.length
							+ ", iTitle =" + iTitle);
		}

		// AChart API objects used to render the chart correctly.
		CategorySeries mSeries = new CategorySeries("");
		DefaultRenderer mRenderer = new DefaultRenderer();

		// Set up the chart parameters
		mRenderer.setApplyBackgroundColor(true);
		mRenderer.setBackgroundColor(Color.WHITE);

		mRenderer.setLabelsTextSize(LABEL_TEXT_SIZE);
		mRenderer.setChartTitleTextSize(TITLE_TEXT_SIZE);
		mRenderer.setLabelsColor(Color.BLACK);

		mRenderer.setDisplayValues(true);

		mRenderer.setShowLegend(false);
		mRenderer.setFitLegend(false);
		mRenderer.setLegendTextSize(LEGEND_TEXT_SIZE);

		mRenderer.setZoomEnabled(false);
		mRenderer.setZoomButtonsVisible(false);
		mRenderer.setStartAngle(0);
		mRenderer.setChartTitle(iTitle);
		mRenderer.setPanEnabled(false);

		float total = 0;
		for (int i = 0; i < iValues.length; i++) {
			total = total + iValues[i];
		}

		GraphicalView mChartView;
		// mRenderer.setClickEnabled(true);
		mRenderer.setSelectableBuffer(100);
		mChartView = ChartFactory.getPieChartView(con, mSeries, mRenderer);
		layout.addView(mChartView);
		DecimalFormat df = new DecimalFormat("0.0");

		// The new values are added to the chart engine.
		for (int i = 0; i < iValues.length; i++) {
			String percent = "0.0";
			if (total != 0) {
				percent = df.format(iValues[i] / total * 100);
			}

			// Inline if else to show shorter legend if the toatl == 0.
			// mSeries.add(percent + "%" + "---" + (char) (65 + i) + ((total ==
			// 0) ? "" : ": " + iOptions[i]), iValues[i]);
			// mSeries.add();
			if ((iValues[i] / total * 100) > 0) {
				mSeries.add((char) (65 + i) + ": " + percent + "%", iValues[i]);
			}
			SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
			renderer.setColor(COLORS[i]);
			renderer.setDisplayChartValues(true);
			mRenderer.addSeriesRenderer(renderer);
		}
		return mChartView;
	}
	public static GraphicalView showRaceChart(Context con, LinearLayout layout,
			List<double[]> values, int iOptionsLength, boolean displayXLabels,
			String iTitle, int xMax, int yMax, int barSize, String xAxisTitle,
			String yAxisTitle) {
		// Setup legend title and the color pellet.
		String[] titles = DemographicValues.races;
		int[] colors = new int[]{ColorPallets.unspecified,
				ColorPallets.newLarge[0], ColorPallets.newLarge[1],
				ColorPallets.newLarge[2], ColorPallets.newLarge[3],
				ColorPallets.newLarge[4], ColorPallets.newLarge[5],
				ColorPallets.newLarge[6]};

		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0,
				xMax, 0, yMax, Color.BLACK, Color.BLACK, iOptionsLength,
				displayXLabels);

		if (barSize > 0) {
			renderer.setBarWidth(barSize);
		}

		GraphicalView mChartView = ChartFactory.getBarChartView(con,
				buildBarDataset(titles, values), renderer, Type.DEFAULT);
		layout.addView(mChartView);
		mChartView.setBackgroundColor(Color.WHITE);
		return mChartView;
	}

	public static GraphicalView showReligionChart(Context con,
			LinearLayout layout, List<double[]> values, int iOptionsLength,
			boolean displayXLabels, String iTitle, int xMax, int yMax,
			int barSize, String xAxisTitle, String yAxisTitle) {
		// Setup legend title and the color pellet.
		String[] titles = DemographicValues.religion;
		int[] colors = new int[]{ColorPallets.unspecified,
				ColorPallets.newLarge[0], ColorPallets.newLarge[1],
				ColorPallets.newLarge[2], ColorPallets.newLarge[3],
				ColorPallets.newLarge[4], ColorPallets.newLarge[5],
				ColorPallets.newLarge[6], ColorPallets.newLarge[7],
				ColorPallets.newLarge[8], ColorPallets.newLarge[9],
				ColorPallets.newLarge[10], ColorPallets.newLarge[11]};

		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0,
				xMax, 0, yMax, Color.BLACK, Color.BLACK, iOptionsLength,
				displayXLabels);

		if (barSize > 0) {
			renderer.setBarWidth(barSize);
		}

		GraphicalView mChartView = ChartFactory.getBarChartView(con,
				buildBarDataset(titles, values), renderer, Type.DEFAULT);
		layout.addView(mChartView);
		mChartView.setBackgroundColor(Color.WHITE);
		return mChartView;
	}

	public static void showAppStatisticsChart(Context con, LinearLayout layout,
			List<double[]> values, String[] iOptions, String iTitle, double yMax) {
		// Setup legend title and the color pellet.
		String[] titles = iOptions;
		int[] colors = new int[]{ColorPallets.another[0],
				ColorPallets.another[3], ColorPallets.another[1]};

		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		setCommonChartSettings(renderer, iTitle, "", "Count", 0, 1, 0, yMax,
				Color.BLACK, Color.BLACK, 0, false);
		renderer.setBarWidth(COMMUNITY_BAR_WIDTH);

		GraphicalView mChartView = ChartFactory.getBarChartView(con,
				buildBarDataset(titles, values), renderer, Type.DEFAULT);
		layout.addView(mChartView);
		mChartView.setBackgroundColor(Color.WHITE);
	}

	public static GraphicalView showEducationChart(Context con,
			LinearLayout layout, List<double[]> values, int iOptionsLength,
			boolean displayXLabels, String iTitle, int xMax, int yMax,
			int barSize, String xAxisTitle, String yAxisTitle) {
		// Setup legend title and the color pellet.
		String[] titles = DemographicValues.educations;
		int[] colors = new int[]{ColorPallets.unspecified,
				ColorPallets.newLarge[0], ColorPallets.newLarge[1],
				ColorPallets.newLarge[2], ColorPallets.newLarge[3],
				ColorPallets.newLarge[4], ColorPallets.newLarge[5],
				ColorPallets.newLarge[6]};

		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0,
				xMax, 0, yMax, Color.BLACK, Color.BLACK, iOptionsLength,
				displayXLabels);

		if (barSize > 0) {
			renderer.setBarWidth(barSize);
		}

		GraphicalView mChartView = ChartFactory.getBarChartView(con,
				buildBarDataset(titles, values), renderer, Type.DEFAULT);
		layout.addView(mChartView);
		mChartView.setBackgroundColor(Color.WHITE);
		return mChartView;
	}

	public static GraphicalView showContinentChart(Context con,
			LinearLayout layout, List<double[]> values, int iOptionsLength,
			boolean displayXLabels, String iTitle, int xMax, int yMax,
			int barSize, String xAxisTitle, String yAxisTitle) {
		// Setup legend title and the color pellet.
		String[] titles = DemographicValues.continents;
		int[] colors = new int[]{ColorPallets.unspecified,
				ColorPallets.newLarge[0], ColorPallets.newLarge[1],
				ColorPallets.newLarge[2], ColorPallets.newLarge[3],
				ColorPallets.newLarge[4], ColorPallets.newLarge[5],
				ColorPallets.newLarge[6]};

		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		setCommonChartSettings(renderer, "Continent Distribution", xAxisTitle,
				yAxisTitle, 0, xMax, 0, yMax, Color.BLACK, Color.BLACK,
				iOptionsLength, displayXLabels);

		if (barSize > 0) {
			renderer.setBarWidth(barSize);
		}

		GraphicalView mChartView = ChartFactory.getBarChartView(con,
				buildBarDataset(titles, values), renderer, Type.DEFAULT);
		layout.addView(mChartView);
		mChartView.setBackgroundColor(Color.WHITE);
		return mChartView;
	}

	public static GraphicalView showAgeChart(Context con, LinearLayout layout,
			List<double[]> values, int iOptionsLength, boolean displayXLabels,
			String iTitle, int xMax, int yMax, int barSize, String xAxisTitle,
			String yAxisTitle) {
		// Setup legend title and the color pellet.
		String[] titles = DemographicValues.ages;
		int[] colors = new int[]{ColorPallets.unspecified,
				ColorPallets.newLarge[0], ColorPallets.newLarge[1],
				ColorPallets.newLarge[2], ColorPallets.newLarge[3],
				ColorPallets.newLarge[4], ColorPallets.newLarge[5]};

		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0,
				xMax, 0, yMax, Color.BLACK, Color.BLACK, iOptionsLength,
				displayXLabels);

		if (barSize > 0) {
			renderer.setBarWidth(barSize);
		}

		GraphicalView mChartView = ChartFactory.getBarChartView(con,
				buildBarDataset(titles, values), renderer, Type.DEFAULT);
		layout.addView(mChartView);
		mChartView.setBackgroundColor(Color.WHITE);
		return mChartView;
	}

	public static GraphicalView showGenderChart(Context con,
			LinearLayout layout, List<double[]> values, int iOptionsLength,
			boolean displayXLabels, String iTitle, int xMax, int yMax,
			int barSize, String xAxisTitle, String yAxisTitle) {
		// Set up the legend and color pellet.
		String[] titles = DemographicValues.genders;// new
													// String[]{DemographicValues.genders[1],
		// DemographicValues.genders[2]};
		int[] colors = new int[]{ColorPallets.unspecified,
				ColorPallets.light[1], ColorPallets.light[0]};

		// Set up renderer parameters.
		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		renderer.setOrientation(Orientation.HORIZONTAL);
		setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0,
				xMax, 0, yMax, Color.BLACK, Color.BLACK, iOptionsLength,
				displayXLabels);

		if (barSize > 0) {
			renderer.setBarWidth(barSize);
		}

		GraphicalView mChartView = ChartFactory.getBarChartView(con,
				buildBarDataset(titles, values), renderer, Type.DEFAULT);
		layout.addView(mChartView);
		mChartView.setBackgroundColor(Color.WHITE);
		return mChartView;
	}

	// Bar chart helper functions --------------------------------------
	protected static XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(50);
		renderer.setChartTitleTextSize(50);
		renderer.setLabelsTextSize(35);
		renderer.setLegendTextSize(35);
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			renderer.addSeriesRenderer(r);

		}
		return renderer;
	}
	protected static XYMultipleSeriesDataset buildBarDataset(String[] titles,
			List<double[]> values) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			CategorySeries series = new CategorySeries(titles[i]);
			double[] v = values.get(i);
			int seriesLength = v.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(v[k]);
			}
			dataset.addSeries(series.toXYSeries());
		}
		return dataset;
	}
	protected static void setCommonChartSettings(
			XYMultipleSeriesRenderer renderer, String title, String xTitle,
			String yTitle, double xMin, double xMax, double yMin, double yMax,
			int axesColor, int labelsColor, int noOfQuestionOptions,
			boolean displayXLablels) {
		// Show the X-axis labels, based on the question data.
		renderer.setShowCustomTextGrid(true);

		// For all the options that exist add the A,B,C... etc at the x-axis
		if (displayXLablels == false) {

		} else {
			int i = 0;
			for (; (i < noOfQuestionOptions); i++) {
				renderer.addXTextLabel(i + 1, ((char) (65 + i)) + "");
			}
			// Add one extra with no x-title
			renderer.addXTextLabel(i + 1, "");
		}

		renderer.setMarginsColor(Color.WHITE);
		renderer.setPanEnabled(false, false);
		renderer.setDisplayValues(false);
		renderer.setZoomEnabled(false, false);

		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax + 1);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
		renderer.setXLabels(0);
		renderer.setYLabels(10);
		renderer.setBarSpacing(1);
		renderer.setYLabelsAlign(Align.RIGHT);

		renderer.setXLabelsColor(Color.BLACK);
		renderer.setYLabelsColor(0, Color.BLACK);
		renderer.setShowGridX(true);
		renderer.setShowGridY(true);
		renderer.setGridColor(Color.LTGRAY);

		renderer.setLabelsTextSize(LABEL_TEXT_SIZE);
		renderer.setLegendTextSize(LEGEND_TEXT_SIZE);
		renderer.setChartTitleTextSize(TITLE_TEXT_SIZE);
		renderer.setAxisTitleTextSize(AXIS_TITLE_TEXT_SIZE);
		// Legend was being cropped or not shown at all until I did this
		renderer.setFitLegend(true);
		renderer.setMargins(MARGINS);

		// Do not show the chart values on the bars.
		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer seriesRenderer = renderer
					.getSeriesRendererAt(i);
			seriesRenderer.setDisplayChartValues(false);
		}

	}
}

/*
 * public class ChartMaker { public static int PERCENTAGE_LABEL_TEXT_SIZE = 15;
 * public static int LABEL_TEXT_SIZE = 15; public static int LEGEND_TEXT_SIZE =
 * 15; public static int AXIS_TITLE_TEXT_SIZE = 15; public static int
 * TITLE_TEXT_SIZE = 35; public static int COMMUNITY_BAR_WIDTH = 100; public
 * static int[] MARGINS = {75, 75, 75, 75};// Margin order: top, left, //
 * bottom, right
 * 
 * public static GraphicalView showPercentagePieChart(Context con, LinearLayout
 * layout, int[] iValues, String[] iOptions, String iTitle) {
 * 
 * int[] COLORS = new int[]{ColorPallets.simple[0], ColorPallets.simple[1],
 * ColorPallets.simple[2], ColorPallets.simple[3], ColorPallets.simple[4],
 * ColorPallets.simple[5], ColorPallets.simple[6]};
 * 
 * if (Helper.isSizeEqual(iOptions, iValues) == false) {
 * ParseOperation.notifyCorruptQuestion(
 * SharedData.focus_question.getObjectId(),
 * "Cannot show percentagePieChart,iValues.length =" + iValues.length +
 * ",  iValues[0] = " + iValues[0] + ", iOptions.length =" + iOptions.length +
 * ", iTitle =" + iTitle); }
 * 
 * // AChart API objects used to render the chart correctly. CategorySeries
 * mSeries = new CategorySeries(""); DefaultRenderer mRenderer = new
 * DefaultRenderer();
 * 
 * // Set up the chart parameters mRenderer.setApplyBackgroundColor(true);
 * mRenderer.setBackgroundColor(Color.WHITE);
 * 
 * mRenderer.setLabelsTextSize(LABEL_TEXT_SIZE);
 * mRenderer.setLegendTextSize(LEGEND_TEXT_SIZE);
 * mRenderer.setChartTitleTextSize(PERCENTAGE_LABEL_TEXT_SIZE);
 * 
 * mRenderer.setLabelsColor(Color.BLACK); mRenderer.setShowLegend(true);
 * 
 * mRenderer.setZoomButtonsVisible(false); mRenderer.setStartAngle(0);
 * mRenderer.setChartTitle(iTitle); mRenderer.setPanEnabled(false);
 * 
 * float total = 0; for (int i = 0; i < iValues.length; i++) { total = total +
 * iValues[i]; }
 * 
 * GraphicalView mChartView; mRenderer.setClickEnabled(true);
 * mRenderer.setSelectableBuffer(100); mChartView =
 * ChartFactory.getPieChartView(con, mSeries, mRenderer);
 * layout.addView(mChartView); DecimalFormat df = new DecimalFormat("0.0");
 * 
 * // The new values are added to the chart engine. for (int i = 0; i <
 * iValues.length; i++) { String percent = "0.0"; if (total != 0) { percent =
 * df.format(iValues[i] / total * 100); }
 * 
 * // Inline if else to show shorter legend if the toatl == 0.
 * mSeries.add(percent + "%" + "---" + (char) (65 + i) + ((total == 0) ? "" :
 * ": " + iOptions[i]), iValues[i]); SimpleSeriesRenderer renderer = new
 * SimpleSeriesRenderer(); renderer.setColor(COLORS[i]);
 * mRenderer.addSeriesRenderer(renderer); } return mChartView; } public static
 * GraphicalView showRaceChart(Context con, LinearLayout layout, List<double[]>
 * values, int iOptionsLength, boolean displayXLabels, String iTitle, int xMax,
 * int yMax, int barSize, String xAxisTitle, String yAxisTitle) { // Setup
 * legend title and the color pellet. String[] titles = new
 * String[]{DemographicValues.races[1], DemographicValues.races[2],
 * DemographicValues.races[3], DemographicValues.races[4],
 * DemographicValues.races[5], DemographicValues.races[6]}; int[] colors = new
 * int[]{ColorPallets.winter[0], ColorPallets.winter[1], ColorPallets.winter[2],
 * ColorPallets.winter[3], ColorPallets.winter[4], ColorPallets.winter[5]};
 * 
 * XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
 * setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0, xMax, 0,
 * yMax, Color.BLACK, Color.BLACK, iOptionsLength, displayXLabels);
 * 
 * if (barSize > 0) { renderer.setBarWidth(barSize); }
 * 
 * GraphicalView mChartView = ChartFactory.getBarChartView(con,
 * buildBarDataset(titles, values), renderer, Type.DEFAULT);
 * layout.addView(mChartView); mChartView.setBackgroundColor(Color.WHITE);
 * return mChartView; }
 * 
 * public static GraphicalView showReligionChart(Context con, LinearLayout
 * layout, List<double[]> values, int iOptionsLength, boolean displayXLabels,
 * String iTitle, int xMax, int yMax, int barSize, String xAxisTitle, String
 * yAxisTitle) { // Setup legend title and the color pellet. String[] titles =
 * new String[]{DemographicValues.religion[1], DemographicValues.religion[2],
 * DemographicValues.religion[3], DemographicValues.religion[4],
 * DemographicValues.religion[5], DemographicValues.religion[6],
 * DemographicValues.religion[7], DemographicValues.religion[8]}; int[] colors =
 * new int[]{ColorPallets.simple[0], ColorPallets.simple[1],
 * ColorPallets.simple[2], ColorPallets.simple[3], ColorPallets.simple[4],
 * ColorPallets.simple[5], ColorPallets.simple[6], ColorPallets.simple[7]};
 * 
 * XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
 * setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0, xMax, 0,
 * yMax, Color.BLACK, Color.BLACK, iOptionsLength, displayXLabels);
 * 
 * if (barSize > 0) { renderer.setBarWidth(barSize); }
 * 
 * GraphicalView mChartView = ChartFactory.getBarChartView(con,
 * buildBarDataset(titles, values), renderer, Type.DEFAULT);
 * layout.addView(mChartView); mChartView.setBackgroundColor(Color.WHITE);
 * return mChartView; }
 * 
 * public static void showAppStatisticsChart(Context con, LinearLayout layout,
 * List<double[]> values, String[] iOptions, String iTitle, double yMax) { //
 * Setup legend title and the color pellet. String[] titles = iOptions; int[]
 * colors = new int[]{ColorPallets.another[0], ColorPallets.another[3],
 * ColorPallets.another[1]};
 * 
 * XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
 * setCommonChartSettings(renderer, iTitle, "", "Count", 0, 1, 0, yMax,
 * Color.BLACK, Color.BLACK, 0, false);
 * renderer.setBarWidth(COMMUNITY_BAR_WIDTH);
 * 
 * GraphicalView mChartView = ChartFactory.getBarChartView(con,
 * buildBarDataset(titles, values), renderer, Type.DEFAULT);
 * layout.addView(mChartView); mChartView.setBackgroundColor(Color.WHITE); }
 * 
 * public static GraphicalView showEducationChart(Context con, LinearLayout
 * layout, List<double[]> values, int iOptionsLength, boolean displayXLabels,
 * String iTitle, int xMax, int yMax, int barSize, String xAxisTitle, String
 * yAxisTitle) { // Setup legend title and the color pellet. String[] titles =
 * new String[]{DemographicValues.educations[1],
 * DemographicValues.educations[2], DemographicValues.educations[3],
 * DemographicValues.educations[4], DemographicValues.educations[5],
 * DemographicValues.educations[6], DemographicValues.educations[7]}; int[]
 * colors = new int[]{ColorPallets.simple[0], ColorPallets.light[1],
 * ColorPallets.light[2], ColorPallets.light[3], ColorPallets.light[4],
 * ColorPallets.light[5], ColorPallets.light[6]};
 * 
 * XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
 * setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0, xMax, 0,
 * yMax, Color.BLACK, Color.BLACK, iOptionsLength, displayXLabels);
 * 
 * if (barSize > 0) { renderer.setBarWidth(barSize); }
 * 
 * GraphicalView mChartView = ChartFactory.getBarChartView(con,
 * buildBarDataset(titles, values), renderer, Type.DEFAULT);
 * layout.addView(mChartView); mChartView.setBackgroundColor(Color.WHITE);
 * return mChartView; }
 * 
 * public static GraphicalView showContinentChart(Context con, LinearLayout
 * layout, List<double[]> values, int iOptionsLength, boolean displayXLabels,
 * String iTitle, int xMax, int yMax, int barSize, String xAxisTitle, String
 * yAxisTitle) { // Setup legend title and the color pellet. String[] titles =
 * new String[]{DemographicValues.continents[1],
 * DemographicValues.continents[2], DemographicValues.continents[3],
 * DemographicValues.continents[4], DemographicValues.continents[5],
 * DemographicValues.continents[6], DemographicValues.continents[7]}; int[]
 * colors = new int[]{ColorPallets.simple[0], ColorPallets.simple[1],
 * ColorPallets.simple[2], ColorPallets.simple[3], ColorPallets.simple[4],
 * ColorPallets.simple[5], ColorPallets.simple[6]};
 * 
 * XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
 * setCommonChartSettings(renderer, "Continent Distribution", xAxisTitle,
 * yAxisTitle, 0, xMax, 0, yMax, Color.BLACK, Color.BLACK, iOptionsLength,
 * displayXLabels);
 * 
 * if (barSize > 0) { renderer.setBarWidth(barSize); }
 * 
 * GraphicalView mChartView = ChartFactory.getBarChartView(con,
 * buildBarDataset(titles, values), renderer, Type.DEFAULT);
 * layout.addView(mChartView); mChartView.setBackgroundColor(Color.WHITE);
 * return mChartView; }
 * 
 * public static GraphicalView showAgeChart(Context con, LinearLayout layout,
 * List<double[]> values, int iOptionsLength, boolean displayXLabels, String
 * iTitle, int xMax, int yMax, int barSize, String xAxisTitle, String
 * yAxisTitle) { // Setup legend title and the color pellet. String[] titles =
 * new String[]{DemographicValues.ages[1], DemographicValues.ages[2],
 * DemographicValues.ages[3], DemographicValues.ages[4],
 * DemographicValues.ages[5], DemographicValues.ages[6]}; int[] colors = new
 * int[]{ColorPallets.simple[0], ColorPallets.simple[1], ColorPallets.simple[2],
 * ColorPallets.simple[3], ColorPallets.simple[4], ColorPallets.simple[5]};
 * 
 * XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
 * setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0, xMax, 0,
 * yMax, Color.BLACK, Color.BLACK, iOptionsLength, displayXLabels);
 * 
 * if (barSize > 0) { renderer.setBarWidth(barSize); }
 * 
 * GraphicalView mChartView = ChartFactory.getBarChartView(con,
 * buildBarDataset(titles, values), renderer, Type.DEFAULT);
 * layout.addView(mChartView); mChartView.setBackgroundColor(Color.WHITE);
 * return mChartView; }
 * 
 * public static GraphicalView showGenderChart(Context con, LinearLayout layout,
 * List<double[]> values, int iOptionsLength, boolean displayXLabels, String
 * iTitle, int xMax, int yMax, int barSize, String xAxisTitle, String
 * yAxisTitle) { // Set up the legend and color pellet. String[] titles = new
 * String[]{DemographicValues.genders[1], DemographicValues.genders[2]}; int[]
 * colors = new int[]{ColorPallets.light[1], ColorPallets.light[0]};
 * 
 * // Set up renderer parameters. XYMultipleSeriesRenderer renderer =
 * buildBarRenderer(colors); renderer.setOrientation(Orientation.HORIZONTAL);
 * setCommonChartSettings(renderer, iTitle, xAxisTitle, yAxisTitle, 0, xMax, 0,
 * yMax, Color.BLACK, Color.BLACK, iOptionsLength, displayXLabels);
 * 
 * if (barSize > 0) { renderer.setBarWidth(barSize); }
 * 
 * GraphicalView mChartView = ChartFactory.getBarChartView(con,
 * buildBarDataset(titles, values), renderer, Type.DEFAULT);
 * layout.addView(mChartView); mChartView.setBackgroundColor(Color.WHITE);
 * return mChartView; }
 * 
 * // Bar chart helper functions --------------------------------------
 * protected static XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
 * XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
 * renderer.setAxisTitleTextSize(50); renderer.setChartTitleTextSize(50);
 * renderer.setLabelsTextSize(35); renderer.setLegendTextSize(35); int length =
 * colors.length; for (int i = 0; i < length; i++) { SimpleSeriesRenderer r =
 * new SimpleSeriesRenderer(); r.setColor(colors[i]);
 * renderer.addSeriesRenderer(r);
 * 
 * } return renderer; } protected static XYMultipleSeriesDataset
 * buildBarDataset(String[] titles, List<double[]> values) {
 * XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset(); int length =
 * titles.length; for (int i = 0; i < length; i++) { CategorySeries series = new
 * CategorySeries(titles[i]); double[] v = values.get(i); int seriesLength =
 * v.length; for (int k = 0; k < seriesLength; k++) { series.add(v[k]); }
 * dataset.addSeries(series.toXYSeries()); } return dataset; } protected static
 * void setCommonChartSettings( XYMultipleSeriesRenderer renderer, String title,
 * String xTitle, String yTitle, double xMin, double xMax, double yMin, double
 * yMax, int axesColor, int labelsColor, int noOfQuestionOptions, boolean
 * displayXLablels) { // Show the X-axis labels, based on the question data.
 * renderer.setShowCustomTextGrid(true);
 * 
 * // For all the options that exist add the A,B,C... etc at the x-axis if
 * (displayXLablels == false) {
 * 
 * } else { int i = 0; for (; (i < noOfQuestionOptions); i++) {
 * renderer.addXTextLabel(i + 1, ((char) (65 + i)) + ""); } // Add one extra
 * with no x-title renderer.addXTextLabel(i + 1, ""); }
 * 
 * renderer.setMarginsColor(Color.WHITE); renderer.setPanEnabled(false, false);
 * renderer.setDisplayValues(false); renderer.setZoomEnabled(false, false);
 * 
 * renderer.setChartTitle(title); renderer.setXTitle(xTitle);
 * renderer.setYTitle(yTitle); renderer.setXAxisMin(xMin);
 * renderer.setXAxisMax(xMax + 1); renderer.setYAxisMin(yMin);
 * renderer.setYAxisMax(yMax); renderer.setAxesColor(axesColor);
 * renderer.setLabelsColor(labelsColor); renderer.setXLabels(0);
 * renderer.setYLabels(10); renderer.setBarSpacing(1);
 * renderer.setYLabelsAlign(Align.RIGHT);
 * 
 * renderer.setLabelsTextSize(LABEL_TEXT_SIZE);
 * renderer.setLegendTextSize(LEGEND_TEXT_SIZE);
 * renderer.setChartTitleTextSize(TITLE_TEXT_SIZE);
 * renderer.setAxisTitleTextSize(AXIS_TITLE_TEXT_SIZE); // Legend was being
 * cropped or not shown at all until I did this renderer.setFitLegend(true);
 * renderer.setMargins(MARGINS);
 * 
 * // Do not show the chart values on the bars. int length =
 * renderer.getSeriesRendererCount(); for (int i = 0; i < length; i++) {
 * SimpleSeriesRenderer seriesRenderer = renderer .getSeriesRendererAt(i);
 * seriesRenderer.setDisplayChartValues(false); }
 * 
 * } }
 */
