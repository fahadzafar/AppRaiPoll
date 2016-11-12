package com.parse.helper;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.parse.activities.ViewQuestionActivity;
import com.parse.objects.DemographicValues;
import com.parse.test.TestData;

import android.content.Context;

// This class helps aggregate the multiple vote records coming from the VoteGender,
// VoteAge, VoteContinent, VoteEducation and VoteRace tables.
public class VoteMathHelper {

	// Sum all the elements in the table to find the total number of votes cast
	// for this question.
	public static int sumAll2DElements(List<double[]> data) {

		double total = 0;
		for (int i = 0; i < data.size(); i++) {
			double[] row = data.get(i);
			for (int j = 0; j < data.get(i).length; j++) {
				total = total + row[j];
			}
		}
		return ((int) total);
	}

	// Divide each value of the list with the total so you can get an average
	// percentage list for each option.
	public static List<double[]> divideAll2DElementsAndRotate(
			List<double[]> data, int total) {

		// if total is 0, only rotate.

		// The incoming data is in
		// option-1 : Male Female
		// option-2 : Male Female
		// option-3 : Male Female
		// ..... rotate it to
		// Male: option-1 option-2 option-3
		// Female: option-1 option-2 option-3
		List<double[]> answer = new ArrayList<double[]>();
		ViewQuestionActivity.maxPercentageElement = 0;
		if (data.size() == 0)
			return null;
		for (int i = 0; i < data.get(0).length; i++)
			answer.add(new double[data.size()]);

		for (int i = 0; i < data.size(); i++) {
			for (int j = 0; j < data.get(i).length; j++) {
				double holder = 0;
				if (total != 0) {
					holder = ((data.get(i)[j] / total) * 100.0);
				}

				answer.get(j)[i] = holder;

				if (holder > ViewQuestionActivity.maxPercentageElement) {
					ViewQuestionActivity.maxPercentageElement = (int) Math
							.ceil(holder);
					// Percentage cannot be bigger than 100.
					if (ViewQuestionActivity.maxPercentageElement > 100) {
						ViewQuestionActivity.maxPercentageElement = 100;
					}
				}
			}
		}
		return answer;
	}

	// Exact and return the option text for a question including the forced
	// option
	public static String[] getFocusQuestionOptions(Context con) {
		String[] options = new String[SharedData.focus_question
				.getInt("total_options")];

		JSONArray parent_array = (JSONArray) SharedData.focus_question
				.getJSONArray("options_list");
		try {
			// Total options for this question.
			int total_options = parent_array.getJSONArray(0).length();

			int i = 0;
			for (; i < total_options; i++) {
				options[i] = parent_array.getJSONArray(0).getString(i);
			}

		} catch (JSONException e) {
			Helper.ShowDialogue("JSON Read Error",
					"Cannot read options for the question", con);
		}
		return options;
	}

	// this function returns the total number of votes cast per option of the
	// question by aggregating all the category vote, for instance in case of
	// gender it will aggregate the male and female vote for each option and
	// return the total sum per option in an array.
	public static int[] sumEachOptionElements(List<double[]> data) {
		int[] answer = new int[data.size()];
		for (int i = 0; i < data.size(); i++) {
			answer[i] = 0;
			for (int j = 0; j < data.get(i).length; j++) {
				answer[i] = answer[i] + ((int) data.get(i)[j]);
			}
		}
		return answer;
	}
}
