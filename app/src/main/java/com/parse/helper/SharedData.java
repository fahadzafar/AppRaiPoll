package com.parse.helper;

import java.util.Date;
import java.util.HashMap;

import android.graphics.Bitmap;

import com.parse.ParseObject;
import com.parse.ParseUser;
public class SharedData {
	public static ParseUser current_user = ParseUser.getCurrentUser();
	public static ParseObject focus_question = null;

	public static final int FLAG_FILTER_LIMIT = 25;

	public static final String VOTE_TABLE = "Vote";
	public static final String ALL_QUESTION_VOTE_TABLE = "AllQuestionVote";
	public static final String VOTER_DEMO_CHANED_LOG = "VoterDemoChangedLog";

	public static final String QUESTION_BOOKMARK_TABLE = "QuestionBookmarkTable";
	
	public static Date defaultDateOfBirth;

	// Maximum dimension of the uploading image.
	public static boolean[] refreshThisTab = {true, true, true, true};

	// Maximum dimension of the uploading image.
	public static final int IMAGE_UPLOAD_MAX_DIMENSION = 1024;

	public static final String APP_STATISTICS_TABLE = "AppStatistics";
	public static final String APP_ID_FOR_VOTE_TABLES = "7KrqvKyrrb";

	// Holds the downloaded images for questions.
	public static HashMap<String, Bitmap> dowloadedImageMap = new HashMap<String, Bitmap>();

	// Holds the most recent votes for questions.
	public static HashMap<String, ParseObject> quetionVotes = new HashMap<String, ParseObject>();

	// Skip this many number of records when getting data.
	public static final int QUERY_LIMIT_PER_PAGE = 20;

	// Limit this many number of records after the skipped ones.
	public static int QueryLimit = QUERY_LIMIT_PER_PAGE;

	// Skip this many number of records when getting data.
	public static int QuerySkip = 0;

}
