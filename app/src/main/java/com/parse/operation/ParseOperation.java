package com.parse.operation;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.achartengine.GraphicalView;

import android.R.bool;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.account.AccountHandler;
import com.parse.activities.DashboardActivity;
import com.parse.activities.SearchActivity;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.AppUser;
import com.parse.objects.DemographicValues;
import com.parse.objects.Question;

public class ParseOperation {

	public static boolean UPDATING_VOTE_COUNT = false;

	public static boolean UpdateDemographics(HashMap<String, Integer> demoData,
											 Calendar dateOfBirth, Context con) {
		// To track if at least one of the items were changed.
		boolean itemChanged = false;

		// Create the chart names list to show in the dialogue box.
		// here we see if there were any changed made and a request must be
		// issued or not.

		ParseObject logEntry = new ParseObject(SharedData.VOTER_DEMO_CHANED_LOG);
		ParseACL postACL = getFullAccessACL();
		logEntry.setACL(postACL);
		logEntry.put("user_id", SharedData.current_user);

		for (Entry<String, Integer> entry : demoData.entrySet()) {
			if (entry.getValue() != -1 && entry.getValue() != 0) {
				String demogrpahic = (String) entry.getKey();
				int newValue = entry.getValue();
				Log.e("Demo , newvalue", demogrpahic + ", " + newValue);
				SharedData.current_user.put(demogrpahic, newValue);

				// set the flag that a request must be issued.
				itemChanged = true;
			}
			logEntry.put((String) entry.getKey(), (Integer) entry.getValue());
		}

		// If changed, Send a request to Parse to update the user specific stuff
		// And create a log entry that will change the previously
		// voted data for this user.
		if (itemChanged) {
			try {
				if (dateOfBirth != null) {
					SharedData.current_user.put(DemographicValues.DOB_TITLE,
							dateOfBirth.getTime());
				}
				SharedData.current_user.save();

				logEntry.save();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return itemChanged;
	}

	public static List<ParseObject> GetPersonalQuestions(final Context con,
														 ParseObject relativeObject, Compare lessOrGreaterThan,
														 int spinnderIndex, List<ParseObject> alreadyVisibleData) {

		List<ParseObject> objects = new ArrayList<ParseObject>();

		switch (spinnderIndex) {
			// My Pinned
			case 0 : {
				objects = GetAllQuestionsFollowedByUser(
						SharedData.current_user, con, lessOrGreaterThan,
						relativeObject);
				break;
			}
			// My Voted
			case 1 : {
				objects = GetAllQuestionsVotedByUser(SharedData.current_user,
						con, lessOrGreaterThan, relativeObject);
				break;
			}

			// My Posted
			case 2 : {
				Date date = null;
				if (relativeObject != null)
					date = relativeObject.getCreatedAt();
				objects = GetAllQuestionsPostedByUser(SharedData.current_user,
						con, date, lessOrGreaterThan);
				break;
			}

		}

		return objects;
	}

	public static void GetTotalVotesForQuestionsAndUpdateHashmap(
			ArrayList<ParseObject> newlyRetreivedData) {

		if (UPDATING_VOTE_COUNT)
			return;

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(SharedData.ALL_QUESTION_VOTE_TABLE);

		if (newlyRetreivedData != null) {
			query.whereContainedIn("question_id", newlyRetreivedData);
		}

		// Here the sorting happens based on the the time the vote was cast.
		// A person can cast a vote on an older question after voting on a newer
		// question and that will not show up if you sort by question created
		// date. Hence we sort by vote created date.
		// query.orderByDescending("createdAt");

		UPDATING_VOTE_COUNT = true;
		// Log.i("Fetching total votes", "START");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> objects, ParseException e) {
				// TODO Auto-generated method stub
				if (e == null) {
					UpdateTotalVoteHashMap(objects);

					// Change the data in all the tab questions
					// DashboardActivity.updateVoteCountInAllVisibleTabs();

					// Make the UI update it in the current
					DashboardActivity.displayQuestionsList_.listQAdapter
							.notifyDataSetChanged();
				} else {
					Log.e("Error retreiving vote count",
							"GetTotalVotesForQuestionsAndUpdateHashmap");
				}
				UPDATING_VOTE_COUNT = false;
				// Log.i("Fetching total votes", "END");
			}
		});

	}

	public static void UpdateTotalVoteHashMap(List<ParseObject> data) {
		for (int i = 0; i < data.size(); i++) {
			SharedData.quetionVotes.put(
					data.get(i).getParseObject("question_id").getObjectId(),
					data.get(i));
		}
	}

	public static void ResetPassword(String email, String username, Context con) {
		try {

			if (email.equals("") || username.equals("")
					|| username.equals("Username")
					|| email.equals("Email Address")) {
				Helper.ShowDialogue("Missing",
						"No data for Username and/or Email", con);
				return;
			}
			// Check to see if username and email pair exists.
			ParseQuery<ParseUser> query = ParseUser.getQuery();
			query.whereEqualTo("username", username);
			query.whereEqualTo("email", email);

			List<ParseUser> objects = query.find();

			if ((objects.size() == 0) || objects == null) {
				Helper.ShowDialogue("Incorrect", "Username/Email do not match",
						con);
				return;
			}

			ParseUser.requestPasswordReset(email);
			Helper.ShowDialogue("Success", "Reset email sent", con);
		} catch (ParseException e) {
			Helper.ShowDialogue("Error", e.getMessage(), con);
		}

	}
	// Register a user.
	public static boolean RegisterUser(final AppUser reg_user, final Context con) {
		ParseUser user = new ParseUser();
		user.setUsername(reg_user.username);
		user.setPassword(reg_user.password);
		user.setEmail(reg_user.email);

		// ----------------Set all the categories for subscription.
		for (int i = 0; i < DemographicValues.categories.length; i++) {
			reg_user.user_subscribed_categories.put(i);
		}
		AppUser.setUserSubscribedCategories(user,
				reg_user.user_subscribed_categories);

		// Add the user demographic values.
		user.put(DemographicValues.GENDER_TITLE, reg_user.gender);
		user.put(DemographicValues.AGE_TITLE, reg_user.age);
		user.put(DemographicValues.EDUCATION_TITLE, reg_user.education);
		user.put(DemographicValues.CONTINENT_TITLE, reg_user.continent);
		user.put(DemographicValues.RACE_TITLE, reg_user.race);
		user.put(DemographicValues.RELIGION_TITLE, reg_user.religion);
		user.put(DemographicValues.DOB_TITLE, reg_user.dob);

		try {
			user.signUp();
			// Hooray! Let them use the app now.
			Helper.ShowDialogue("Success", "Voter registered ^_^", con);

			// Update the app statistics.
			ParseAppStatisticsHelper.recordVoter();
			return true;

		} catch (ParseException e1) {
			Helper.ShowDialogue("Error", e1.getMessage(), con);

			// Sign up didn't succeed. Look at the ParseException
			// to figure out what went wrong
			return false;
		}

	}

	// Login a user after he enters a login and a password.
	public static boolean LoginUser(final AppUser reg_user, final Context con) {
		ParseUser user;
		try {
			user = ParseUser.logIn(reg_user.username, reg_user.password);
			if (user != null) {
				// Update new user in the shared objects.
				SharedData.current_user = user;

				// Add to shared preferences
				AccountHandler.setUserNameAndPassword(con, reg_user.username,
						reg_user.password);

				return true;
				// Hooray! The user is logged in.

			} else {
				return false;
				// Helper.ShowDialogue("Login", "Could not login", con);
			}

		} catch (ParseException e1) {
			Helper.ShowDialogue("Password", "incorrect", con);
			e1.printStackTrace();
			return false;
		}
	}

	public static void LogOutCurrentUser(Context con) {
		ParseUser.logOut();
		SharedData.current_user = null;
		AccountHandler.clear(con);
		DashboardActivity.tabFragmentData = new ArrayList[4];
		for (int i = 0; i < DashboardActivity.tabFragmentData.length; i++)
			DashboardActivity.tabFragmentData[i] = null;
	}

	// This helper function adds the sorting and pagination management.
	// Sorting based on a particular field is done in that function.
	public static ParseQuery<ParseObject> GetQuestionQueryStub(Date date,
															   Compare lessOrGreaterThan) {
		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(Question.CLASS_TITLE);
		query.setLimit((SharedData.QueryLimit));
		query.orderByDescending("createdAt");
		query.whereLessThan("total_flags", SharedData.FLAG_FILTER_LIMIT);

		if (date != null) {
			if (lessOrGreaterThan == Compare.GREATER_THAN) {
				query.whereGreaterThan("createdAt", date);

			} else if (lessOrGreaterThan == Compare.LESSER_THAN)
				query.whereLessThan("createdAt", date);

		}

		return query;
	}

	public static enum Compare {
		GREATER_THAN, LESSER_THAN, WASTE
	};

	public static List<ParseObject> GetPromotedQuestions(Context con,
														 List<ParseObject> alreadyVisibleData) {
		List<ParseObject> objects = new ArrayList<ParseObject>();
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Promotion");
		query.include("question_id");
		if (alreadyVisibleData != null) {
			query.whereNotContainedIn("question_id", alreadyVisibleData);
		}
		try {
			objects = query.find();
			objects = extractParseObjects(objects, "question_id");
		} catch (com.parse.ParseException e1) {
			Helper.ShowDialogue("Error loading all questions", e1.getMessage(),
					con);
		}
		return objects;
	}

	// Get all the questions
	public static List<ParseObject> GetAllQuestions(final Context con,
													ParseObject relativeObject, Compare lessOrGreaterThan,
													int spinnderIndex, List<ParseObject> alreadyVisibleData) {
		List<ParseObject> objects = new ArrayList<ParseObject>();

		switch (spinnderIndex) {
			// Most Recent
			case 0 : {
				Date date = null;
				if (relativeObject != null) {
					date = relativeObject.getCreatedAt();
				}
				ParseQuery<ParseObject> query = GetQuestionQueryStub(date,
						lessOrGreaterThan);
				try {
					// Get the current subscriptions and get all questions
					ArrayList<Integer> list = (ArrayList<Integer>) AppUser
							.getCurrentUserSubscribedCategories();
					query.whereContainedIn("category", list);

					// Filter out the flagged questions.
					List<String> flagged = AppUser.getUserFlaggedQuestions();
					query.whereNotContainedIn("objectId", flagged);

					objects = query.find();
				} catch (com.parse.ParseException e1) {
					Helper.ShowDialogue("Error loading all questions",
							e1.getMessage(), con);
				}
				break;
			}
			// Most Voted
			case 1 : {
				ParseQuery<ParseObject> query = ParseQuery
						.getQuery(SharedData.ALL_QUESTION_VOTE_TABLE);
				query.setLimit((SharedData.QueryLimit));

				if (relativeObject == null) {

				} else {

					if (lessOrGreaterThan == Compare.LESSER_THAN) {
						// Find total_votes for that question.
						query.whereLessThanOrEqualTo("total_votes",
								relativeObject.getInt("total_votes"));
					} else if (lessOrGreaterThan == Compare.GREATER_THAN) {
						query.whereGreaterThanOrEqualTo("total_votes",
								relativeObject.getInt("total_votes"));
					}

					// Filter based on user flagged questions.
					query.whereNotContainedIn("question_id", alreadyVisibleData);
				}
				query.orderByDescending("total_votes");
				try {

					// --- Categories constraint
					ParseQuery<ParseObject> innerQuery = ParseQuery
							.getQuery(Question.CLASS_TITLE);
					ArrayList<Integer> list = (ArrayList<Integer>) AppUser
							.getCurrentUserSubscribedCategories();
					innerQuery.whereContainedIn("category", list);

					// Filter out the flagged questions.
					List<String> flagged = AppUser.getUserFlaggedQuestions();
					innerQuery.whereNotContainedIn("objectId", flagged);
					innerQuery.whereLessThan("total_flags",
							SharedData.FLAG_FILTER_LIMIT);
					// ---------------------------

					query.include("question_id");
					query.whereMatchesQuery("question_id", innerQuery);

					List<ParseObject> Intermediateobjects = query.find();
					if (Intermediateobjects != null) {
						objects = extractParseObjects(Intermediateobjects,
								"question_id");
					}

				} catch (com.parse.ParseException e1) {
					Helper.ShowDialogue("Error loading follow questions",
							e1.getMessage(), con);
				}
			}
			break;
		}

		return objects;
	}
	// Get all searched questions based on the search term also accommodating
	// for the categories selected.
	public static List<ParseObject> GetAllSearchedQuestions(final Context con,
															List<Integer> wantedCategories, String searchTerm, Date date,
															Compare lessOrGreaterThan) {
		List<ParseObject> objects = null;

		ParseQuery<ParseObject> queryWithOriginalText = GetQuestionQueryStub(
				date, lessOrGreaterThan);

		queryWithOriginalText.orderByDescending("createdAt");
		queryWithOriginalText.whereContains("statement", searchTerm);
		queryWithOriginalText.whereContainedIn("category", wantedCategories);

		try {
			objects = queryWithOriginalText.find();
		} catch (com.parse.ParseException e1) {
			Helper.ShowDialogue("Error loading search questions",
					e1.getMessage(), con);
		}
		return objects;
	}

	// Get all the questions posted by user.
	public static List<ParseObject> GetAllQuestionsPostedByUser(
			ParseObject user, final Context con, Date date,
			Compare lessOrGreaterThan) {
		List<ParseObject> objects = null;
		ParseQuery<ParseObject> query = GetQuestionQueryStub(date,
				lessOrGreaterThan);
		query.whereEqualTo("posted_by", user);
		query.orderByDescending("createdAt");

		try {
			objects = query.find();
		} catch (com.parse.ParseException e1) {
			Helper.ShowDialogue("Error loading all questions", e1.getMessage(),
					con);
		}
		return objects;
	}

	// Change the password for the user.
	public static ParseUser ChangePassword(ParseUser user, String new_pass,
										   final Context con) {
		user.setPassword(new_pass);
		try {
			user.save();
			// Update the new password once the data is saved.
			AccountHandler.setUserNameAndPassword(con, user.getUsername(),
					new_pass);
			Helper.ShowDialogue("Password changed", "Success", con);
		} catch (com.parse.ParseException e) {
			Helper.ShowDialogue("Error", "Could not change password", con);
		}
		return user;
	}

	// Change the password for the user.
	public static ParseObject AddFollow(final Context con,
										ParseObject questionBookmarkData) {
		if (questionBookmarkData == null) {
			questionBookmarkData = createEmptyQuestionBookmarkData();
		}
		questionBookmarkData.increment("is_bookmarked");
		try {
			questionBookmarkData.save();
			Helper.ShowDialogue("Message", "Question bookmarked", con,
					Toast.LENGTH_SHORT);
		} catch (Exception er) {
			ReturnIfElseException(er, "Question being followed", con);
		}
		return questionBookmarkData;
	}

	// Change the password for the user. The way to delete items is to find them
	// and remove them object by object.
	public static void RemoveFollow(final Context con,
									ParseObject voteBookmarkData) {
		try {
			voteBookmarkData.delete();
			Helper.ShowDialogue("Message", "Question bookmark removed", con,
					Toast.LENGTH_SHORT);
		} catch (Exception er) {
			ReturnIfElseException(er, "Question not being followed", con);
		}

	}

	// Get all the questions posted by user.
	public static List<ParseObject> GetAllQuestionsFollowedByUser(
			ParseObject user, final Context con, Compare lessOrGreaterThan,
			ParseObject relateToThisQuestion) {

		ParseObject followDateObject = null;
		if (relateToThisQuestion != null) {
			ParseQuery<ParseObject> innerQuery = ParseQuery
					.getQuery(SharedData.QUESTION_BOOKMARK_TABLE);
			innerQuery.whereEqualTo("question_id", relateToThisQuestion);
			innerQuery.whereEqualTo("user_id", user.getObjectId());
			try {
				followDateObject = innerQuery.getFirst();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<ParseObject> objects = null;
		List<ParseObject> returnResults = null;

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(SharedData.QUESTION_BOOKMARK_TABLE);
		query.whereEqualTo("user_id", user.getObjectId());
		query.whereGreaterThanOrEqualTo("is_bookmarked", 1);
		query.setLimit((SharedData.QueryLimit));

		if (followDateObject != null) {
			Date date = followDateObject.getCreatedAt();
			if (date != null) {
				if (lessOrGreaterThan == Compare.GREATER_THAN) {
					query.whereGreaterThan("createdAt", date);

				} else if (lessOrGreaterThan == Compare.LESSER_THAN)
					query.whereLessThan("createdAt", date);
			}
		}

		// if (currentlyBeingViewed != null) {
		// innerQuery.whereNotContainedIn("question_id", currentlyBeingViewed);
		// }

		// Here the sorting happens based on the the time the vote was cast.
		// A person can cast a vote on an older question after voting on a newer
		// question and that will not show up if you sort by question created
		// date. Hence we sort by vote created date.
		query.orderByDescending("createdAt");
		try {
			query.include("question_id");
			// innerQuery.addDescendingOrder("createdAt");
			objects = query.find();
			returnResults = extractParseObjects(objects, "question_id");

		} catch (com.parse.ParseException e1) {
			Helper.ShowDialogue("Error loading follow questions",
					e1.getMessage(), con);
		}
		return returnResults;
	}

	public static List<ParseObject> extractParseObjects(
			List<ParseObject> objects, String fieldName) {
		List<ParseObject> returnResults = new ArrayList<ParseObject>();

		for (ParseObject object : objects) {
			// This does not require a network access.
			ParseObject question = object.getParseObject("question_id");
			returnResults.add(question);
		}
		return returnResults;
	}
	// Get all the questions posted by user.
	public static List<ParseObject> GetAllQuestionsVotedByUser(
			ParseObject user, final Context con, Compare lessOrGreaterThan,
			ParseObject relateToThisQuestion) {
		ParseObject followDateObject = null;
		if (relateToThisQuestion != null) {
			ParseQuery<ParseObject> innerQuery = ParseQuery
					.getQuery(SharedData.VOTE_TABLE);
			innerQuery.whereEqualTo("question_id", relateToThisQuestion);
			innerQuery.whereEqualTo("user_id", user);

			try {
				followDateObject = innerQuery.getFirst();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		List<ParseObject> objects = null;
		List<ParseObject> returnResults = null;

		ParseQuery<ParseObject> query = ParseQuery
				.getQuery(SharedData.VOTE_TABLE);
		query.whereEqualTo("user_id", user);
		query.whereNotEqualTo("option_picked", -1);
		query.setLimit((SharedData.QueryLimit));

		if (followDateObject != null) {
			Date date = followDateObject.getCreatedAt();
			if (date != null) {
				if (lessOrGreaterThan == Compare.GREATER_THAN) {
					query.whereGreaterThan("createdAt", date);

				} else if (lessOrGreaterThan == Compare.LESSER_THAN)
					query.whereLessThan("createdAt", date);
			}
		}

		// Here the sorting happens based on the the time the vote was cast.
		// A person can cast a vote on an older question after voting on a newer
		// question and that will not show up if you sort by question created
		// date. Hence we sort by vote created date.
		query.orderByDescending("createdAt");
		try {
			query.include("question_id");
			// innerQuery.addDescendingOrder("createdAt");
			objects = query.find();
			returnResults = extractParseObjects(objects, "question_id");

		} catch (com.parse.ParseException e1) {
			Helper.ShowDialogue("Error loading follow questions",
					e1.getMessage(), con);
		}
		return returnResults;

	}

	// Simple check function called upon completion of a callback. If the
	// exception is null, show the success message. Otherwise, the exception
	// message is displayed.
	public static void ReturnIfElseException(Exception e, String success_msg,
											 Context con) {
		if (e == null) {
			Helper.ShowDialogue("Success", success_msg, con);
		} else {
			Helper.ShowDialogue("Error", e.getMessage(), con);
		}
	}

	public static List<ParseObject> FetchUserVoteData(String questionId) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("QuestionVoteData");
		query.orderByDescending("option_picked");
		query.whereEqualTo("question_id",
				SharedData.focus_question.getObjectId());

		// force the results to be 0th option first , then 1st option etc.
		// etc.
		query.orderByAscending("option_index");
		List<ParseObject> returnedData = null;
		try {
			returnedData = query.find();
		} catch (Exception er) {
		}
		return returnedData;
	}

	public static void incrementUserSpecificField(int option_picked,
												  List<ParseObject> questionVoteData) {
		ParseObject particularObject = null;
		int index = 0;

		for (int i = 0; i < questionVoteData.size(); i++) {

			// Find the right option_index record and increment based on user
			// specifics demographic.
			if (questionVoteData.get(i).getInt("option_index") == option_picked) {
				particularObject = questionVoteData.get(i);
				String updateRace = "race" + "_"
						+ SharedData.current_user.getInt("race");
				String updateReligion = "religion" + "_"
						+ SharedData.current_user.getInt("religion");
				String updateAge = "age" + "_"
						+ SharedData.current_user.getInt("age");
				String updateContinent = "continent" + "_"
						+ SharedData.current_user.getInt("continent");
				String updateEducation = "education" + "_"
						+ SharedData.current_user.getInt("education");
				String updateGender = "gender" + "_"
						+ SharedData.current_user.getInt("gender");
				particularObject.increment(updateRace);
				particularObject.increment(updateReligion);
				particularObject.increment(updateAge);
				particularObject.increment(updateContinent);
				particularObject.increment(updateEducation);
				particularObject.increment(updateGender);
				index = i;
			}
		}

		try {
			if (particularObject != null) {
				particularObject.save();
				questionVoteData.set(index, particularObject);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ParseObject createEmptyVoteData() {
		ParseObject voteBookmarkData = new ParseObject(SharedData.VOTE_TABLE);
		// The user and question id comes from the context. Not needed as
		// input arguments to the function.
		voteBookmarkData.put("user_id", SharedData.current_user);
		voteBookmarkData.put("question_id", SharedData.focus_question);
		voteBookmarkData.put("option_picked", -1);
		ParseACL postACL = getFullAccessACL();
		voteBookmarkData.setACL(postACL);
		return voteBookmarkData;
	}

	public static ParseObject createEmptyQuestionBookmarkData() {
		ParseObject voteBookmarkData = new ParseObject(
				SharedData.QUESTION_BOOKMARK_TABLE);
		// The user and question id comes from the context. Not needed as
		// input arguments to the function.
		voteBookmarkData.put("user_id", SharedData.current_user.getObjectId());
		voteBookmarkData.put("question_id", SharedData.focus_question);
		voteBookmarkData.put("is_bookmarked", 0);
		return voteBookmarkData;
	}

	// Add a vote for a particular option to a specific question by the current
	// user.
	public static List<ParseObject> AddVote(int option_picked,
											final Context con, ParseObject voteData) {

		List<ParseObject> questionVoteData = FetchUserVoteData(SharedData.focus_question
				.getObjectId());

		incrementUserSpecificField(option_picked, questionVoteData);

		// Update the total_votes field in the question.
		// SharedData.focus_question.increment("total_votes");
		// SharedData.focus_question.saveInBackground();

		if (voteData == null) {
			voteData = createEmptyVoteData();
			// The user and question id comes from the context. Not needed as
			// input arguments to the function.

		}
		voteData.put("option_picked", option_picked);
		// Casting a vote is a blocking call.
		try {
			voteData.save();

			// Add the vote to the total_vote of the question.
			ParseObject po = SharedData.quetionVotes
					.get(SharedData.focus_question.getObjectId());
			po.increment("total_votes");
			po.save();

			// List<ParseObject> data = new ArrayList<ParseObject>();
			// data.add(po);
			// UpdateTotalVoteHashMap(data);
			// DashboardActivity.updateVoteCountInAllVisibleTabs();

			// Add the vote to the app statistics
			ParseAppStatisticsHelper.recordVoteCast();
		} catch (Exception er) {
			ReturnIfElseException(er, "Voted", con);

		}
		return questionVoteData;
	}
	public static void notifyCorruptQuestion(String questionId, String reason) {
		// Update the Demographic specific counter
		ParseObject recordQuery = new ParseObject("CorruptQuestion");
		recordQuery.put("question_id", questionId);
		recordQuery.put("reason", reason);
		recordQuery.saveInBackground();
	}

	public static ParseObject GetUserVotedOnQuestion(ParseObject questionId) {
		ParseQuery<ParseObject> userVoteQuery = ParseQuery
				.getQuery(SharedData.VOTE_TABLE);
		userVoteQuery.whereEqualTo("question_id", questionId);
		userVoteQuery.whereEqualTo("user_id", SharedData.current_user);
		ParseObject voteObject = null;
		try {
			voteObject = userVoteQuery.getFirst();
		} catch (com.parse.ParseException e1) {
		}
		return voteObject;
	}

	public static ParseObject GetUserBookmarkOnQuestion(ParseObject questionId) {
		ParseQuery<ParseObject> userVoteQuery = ParseQuery
				.getQuery(SharedData.QUESTION_BOOKMARK_TABLE);
		userVoteQuery.whereEqualTo("question_id", questionId);
		userVoteQuery.whereEqualTo("user_id",
				SharedData.current_user.getObjectId());
		ParseObject voteObject = null;
		try {
			voteObject = userVoteQuery.getFirst();
		} catch (com.parse.ParseException e1) {
		}
		return voteObject;
	}

	public static ParseACL getFullAccessACL() {
		ParseACL postACL = new ParseACL(ParseUser.getCurrentUser());
		postACL.setPublicReadAccess(true);
		postACL.setPublicWriteAccess(true);
		return postACL;
	}
	// Adds an empty record for a particular question to store the counts for
	// all the voter data.
	public static void InsertEmptyVoteTableRecord(String questioId,
												  int optionIndex, Context con) {

		// - ACL for everyone to be able to increment their vote in the
		// counters.
		ParseACL postACL = getFullAccessACL();

		ParseObject object = new ParseObject("QuestionVoteData");
		object.put("question_id", questioId);
		object.put("option_index", optionIndex);

		addAdditionalDemographicFields(object, DemographicValues.genders,
				"gender");
		addAdditionalDemographicFields(object, DemographicValues.ages, "age");
		addAdditionalDemographicFields(object, DemographicValues.continents,
				"continent");
		addAdditionalDemographicFields(object, DemographicValues.educations,
				"education");
		addAdditionalDemographicFields(object, DemographicValues.races, "race");
		addAdditionalDemographicFields(object, DemographicValues.religion,
				"religion");
		object.setACL(postACL);
		try {
			object.save();
		} catch (Exception er) {
			Helper.ShowDialogue("Create question",
					"Cannot create user vote record.", con);
		}
	}

	public static void InsertEmptyALLQuestionVoteTableRecord(
			ParseObject questioId, Context con) {

		// - ACL for everyone to be able to increment their vote in the
		// counters.
		ParseACL postACL = getFullAccessACL();

		ParseObject object = new ParseObject(SharedData.ALL_QUESTION_VOTE_TABLE);
		object.put("question_id", questioId);
		object.put("total_votes", 0);
		object.setACL(postACL);
		try {
			object.save();
		} catch (Exception er) {
			Helper.ShowDialogue("Create question",
					"Cannot create all question vote record.", con);
		}
	}

	static void addAdditionalDemographicFields(ParseObject obj,
											   String[] demograph, String preText) {

		for (int i = 0; i < demograph.length; i++) {
			obj.put(preText + "_" + i, 0);
		}
	}
}
