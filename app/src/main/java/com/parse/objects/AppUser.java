package com.parse.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;

// This class already is present in Parse as a Class, so we dont need to conver
// this object to a parse objects. We pass these values directly to ParseUser
// and upload to the cloud.
public class AppUser {
	public String username;
	public String email;
	public String password;
	public String name;
	public Date dob;
	public int age;
	public int gender;
	public int continent;
	public int race;
	public int education;
	public int religion;
	public JSONArray user_flagged_questions = new JSONArray();
	public JSONArray user_subscribed_categories = new JSONArray();

	public static List<Integer> getCurrentUserSubscribedCategories() {
		JSONArray subs = SharedData.current_user
				.getJSONArray("user_subscribed_categories");

		ArrayList<Integer> list = new ArrayList<Integer>();
		if (subs != null) {
			int len;
			try {
				len = subs.length();
				for (int i = 0; i < len; i++) {
					list.add(subs.getInt(i));
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return list;
	}

	public static int getCurrentUserNumberOfSubscribedCategories() {
		JSONArray subs = SharedData.current_user
				.getJSONArray("user_subscribed_categories");

		if (subs != null) {
			return subs.length();
		} else {
			return 0;
		}
	}
	// Sets the current categories into the user. call saveinbackground
	// if you choose to update the user subscriptions.
	public static void setUserSubscribedCategories(ParseUser user,
			JSONArray data) {
		user.put("user_subscribed_categories", data);
	}

	// ----------------------- Additional Functions for flagged questions.
	public static void setUserFlaggedQuestions(String flaggedQuestion, Context con) {
		List<String> data = getUserFlaggedQuestions();
		data.add(flaggedQuestion);
		SharedData.current_user.addAllUnique("user_flagged_questions", data);
		try {
			SharedData.current_user.save();
			Helper.ShowDialogue("Question", "Flagged", con);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static void removeUserFlaggedQuestions(String flaggedQuestion, Context con) {
		List<String> data = getUserFlaggedQuestions();
		data.remove(flaggedQuestion);
		SharedData.current_user.put("user_flagged_questions", data);
		try {
			SharedData.current_user.save();
			Helper.ShowDialogue("Question", "Un-Flagged", con);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	
	public static List<String> getUserFlaggedQuestions() {
		JSONArray subs = SharedData.current_user
				.getJSONArray("user_flagged_questions");

		ArrayList<String> list = new ArrayList<String>();
		if (subs != null) {
			int len;
			try {
				len = subs.length();
				for (int i = 0; i < len; i++) {
					list.add(subs.getString(i));
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return list;
	}

}
