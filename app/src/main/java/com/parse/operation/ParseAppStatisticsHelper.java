package com.parse.operation;

import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;

public class ParseAppStatisticsHelper {
	// Add 1 to the App statistics maintained for all users, questions and
	// votes.
	public static void recordVoter() {

		// Update the Demographic specific counter
		ParseQuery<ParseObject> recordQuery = ParseQuery
				.getQuery(SharedData.APP_STATISTICS_TABLE);
		recordQuery.whereEqualTo("objectId", SharedData.APP_ID_FOR_VOTE_TABLES);

		recordQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject object, ParseException e) {
				// Add 1 to race
				object.increment("race_"
						+ SharedData.current_user.getInt("race"));
				// Add 1 to gender
				object.increment("gender_"
						+ SharedData.current_user.getInt("gender"));
				// Add 1 to age
				object.increment("age_" + SharedData.current_user.getInt("age"));
				// Add 1 to education
				object.increment("education_"
						+ SharedData.current_user.getInt("education"));
				// Add 1 to continent
				object.increment("continent_"
						+ SharedData.current_user.getInt("continent"));
				// Add 1 to continent
				object.increment("religion_"
						+ SharedData.current_user.getInt("religion"));

				// Add 1 to total_users
				object.increment("total_users");

				object.saveInBackground();
			}
		});
	}

	public static void recordVoteCast() {
		String tableName = "AppStatistics";
		// Update the Demographic specific counter
		ParseQuery<ParseObject> recordQuery = ParseQuery.getQuery(tableName);
		recordQuery.whereEqualTo("objectId", SharedData.APP_ID_FOR_VOTE_TABLES);

		recordQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject object, ParseException e) {
				// Add 1 to total votes
				object.increment("total_votes");
				object.saveInBackground();
			}
		});
	}

	public static void recordQuestionCreated() {
		String tableName = "AppStatistics";
		// Update the Demographic specific counter
		ParseQuery<ParseObject> recordQuery = ParseQuery.getQuery(tableName);
		recordQuery.whereEqualTo("objectId", SharedData.APP_ID_FOR_VOTE_TABLES);

		recordQuery.getFirstInBackground(new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject object, ParseException e) {
				// Add 1 to total votes
				object.increment("total_questions");
				object.saveInBackground();
			}
		});
	}

	public static int[] getAppStatistics() {
		String tableName = "AppStatistics";
		// Update the Demographic specific counter
		ParseQuery<ParseObject> recordQuery = ParseQuery.getQuery(tableName);
		recordQuery.whereEqualTo("objectId", SharedData.APP_ID_FOR_VOTE_TABLES);

		// Total users, total questions, total votes
		int[] returnData = new int[3 + 2 + 6 + 7 + 7 + 6 + 8 + 6 + 4 + 2];
		try {
			ParseObject appData = recordQuery.getFirst();
			returnData[0] = appData.getInt("total_users");
			returnData[1] = appData.getInt("total_questions");
			returnData[2] = appData.getInt("total_votes");

			returnData[3] = appData.getInt("gender_1");
			returnData[4] = appData.getInt("gender_2");

			returnData[5] = appData.getInt("age_1");
			returnData[6] = appData.getInt("age_2");
			returnData[7] = appData.getInt("age_3");
			returnData[8] = appData.getInt("age_4");
			returnData[9] = appData.getInt("age_5");
			returnData[10] = appData.getInt("age_6");

			returnData[11] = appData.getInt("continent_1");
			returnData[12] = appData.getInt("continent_2");
			returnData[13] = appData.getInt("continent_3");
			returnData[14] = appData.getInt("continent_4");
			returnData[15] = appData.getInt("continent_5");
			returnData[16] = appData.getInt("continent_6");
			returnData[17] = appData.getInt("continent_7");

			returnData[18] = appData.getInt("education_1");
			returnData[19] = appData.getInt("education_2");
			returnData[20] = appData.getInt("education_3");
			returnData[21] = appData.getInt("education_4");
			returnData[22] = appData.getInt("education_5");
			returnData[23] = appData.getInt("education_6");
			returnData[24] = appData.getInt("education_7");

			returnData[25] = appData.getInt("race_1");
			returnData[26] = appData.getInt("race_2");
			returnData[27] = appData.getInt("race_3");
			returnData[28] = appData.getInt("race_4");
			returnData[29] = appData.getInt("race_5");
			returnData[30] = appData.getInt("race_6");
			returnData[49] = appData.getInt("race_7");
			returnData[50] = appData.getInt("race_8");

			returnData[31] = appData.getInt("religion_1");
			returnData[32] = appData.getInt("religion_2");
			returnData[33] = appData.getInt("religion_3");
			returnData[34] = appData.getInt("religion_4");
			returnData[35] = appData.getInt("religion_5");
			returnData[36] = appData.getInt("religion_6");
			returnData[37] = appData.getInt("religion_7");
			returnData[38] = appData.getInt("religion_8");

			returnData[39] = appData.getInt("gender_0");
			returnData[40] = appData.getInt("age_0");
			returnData[41] = appData.getInt("continent_0");
			returnData[42] = appData.getInt("education_0");
			returnData[43] = appData.getInt("race_0");
			returnData[44] = appData.getInt("religion_0");

			returnData[45] = appData.getInt("religion_9");
			returnData[46] = appData.getInt("religion_10");
			returnData[47] = appData.getInt("religion_11");
			returnData[48] = appData.getInt("religion_12");

			
		} catch (ParseException e) {
			e.printStackTrace();
			return returnData;
		}
		return returnData;
	}
}
