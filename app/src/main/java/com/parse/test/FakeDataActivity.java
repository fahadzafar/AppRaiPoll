package com.parse.test;

import java.util.Random;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.activities.R;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.DemographicValues;
import com.parse.objects.Question;
import com.parse.operation.ParseOperation;

public class FakeDataActivity extends Activity {

	static String question_statements[];
	static String question_options[];
	static {
		question_statements = new String[20];
		question_statements[0] = "Who has been the most important person in your life? Can you tell me about him or her?";
		question_statements[1] = "What was the happiest moment of your life? The saddest?";
		question_statements[2] = "Who has been the biggest influence on your life? What lessons did that person teach you?";
		question_statements[3] = "Who has been the kindest to you in your life?";
		question_statements[4] = "What are the most important lessons you've learned in life?";
		question_statements[5] = "What is your earliest memory?";
		question_statements[6] = "What is your favorite memory of me?";
		question_statements[7] = "What are the funniest or most embarrassing stories your family tells about you?";
		question_statements[8] = "If you could hold on to just one memory from your life forever, what would that be?";
		question_statements[9] = "If this was to be our very last conversation, what words of wisdom would you want to pass on to me?";
		question_statements[10] = " What are you proudest of in your life?";
		question_statements[11] = "When in life have you felt most alone?";
		question_statements[12] = "What are your hopes and dreams for what the future holds for me? For my children?";
		question_statements[13] = "How has your life been different than what you'd imagined?";
		question_statements[14] = "How would you like to be remembered?";
		question_statements[15] = "Do you have any regrets?";
		question_statements[16] = "What does your future hold?";
		question_statements[17] = "Is there anything that you've never told me but want to tell me now?";
		question_statements[18] = "Is there something about me that you've always wanted to know but have never asked?";
		question_statements[19] = "Is there any message you want to give to or anything you want to say to your great-great-great grandchildren when they listen to this?";
		question_options = new String[10];
		question_options[0] = "True";
		question_options[1] = "False";
		question_options[2] = "Yes";
		question_options[3] = "What the hell were you thinking";
		question_options[4] = "Not really";
		question_options[5] = "WTF !!!!";
		question_options[6] = "Maybe, but im not sure";
		question_options[7] = "Where else would you find one";
		question_options[8] = "Almost, but not quite";
		question_options[9] = "Mad goats are coming";

	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fake_data);

		Button bn = (Button) findViewById(R.id.fake_data_btn_add_20_questions);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				InsertFakeQuestions(20);
				finish();
			}
		});

		bn = (Button) findViewById(R.id.fake_data_btn_reset_app_statistics);
		bn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				resetAppStatistics();
			}
		});

	}

	public void resetAppStatistics() {
		ParseObject appData = new ParseObject(SharedData.APP_STATISTICS_TABLE);
		// The user and question id comes from the context. Not needed as
		// input arguments to the function.
		appData.put("total_votes", 0);
		appData.put("total_users", 0);
		appData.put("total_questions", 0);

		addAllArrayValues("age", DemographicValues.ages, appData);
		addAllArrayValues("gender", DemographicValues.genders, appData);
		addAllArrayValues("continent", DemographicValues.continents, appData);
		addAllArrayValues("race", DemographicValues.races, appData);
		addAllArrayValues("education", DemographicValues.educations, appData);
		addAllArrayValues("religion", DemographicValues.religion, appData);

		appData.setACL(ParseOperation.getFullAccessACL());
		try {
			appData.save();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void addAllArrayValues(String marker, String[] list, ParseObject singleRow) {
		for (int i = 0; i < list.length; i++) {
			singleRow.put(marker + "_" + i, 0);
		}
	}

	public Question MakeQuestion(Random gen, int index) {
		Question new_q = new Question();
		new_q.category = gen.nextInt(DemographicValues.categories.length);
		new_q.posted_by = SharedData.current_user;
		new_q.statement = index + ":"
				+ question_statements[gen.nextInt(question_statements.length)];
		new_q.has_image = false;
		new_q.user_entered_options = new JSONArray();
		int total_options = 2 + gen.nextInt(4);
		for (int i = 0; i < total_options; i++) {
			new_q.user_entered_options.put(question_options[gen
					.nextInt(question_options.length)]);
		}
		return new_q;
	}
	public void InsertFakeQuestions(int size) {
		Random gen = new Random();
		for (int i = 0; i < size; i++) {
			Question a_question = MakeQuestion(gen, i);
			a_question.UploadAsParseObject(getApplicationContext());
		}
	}
}
