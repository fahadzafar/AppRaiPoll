package com.parse.helper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.activities.ViewQuestionActivity;
import com.parse.bitmap.BitmapTextStitcher;
import com.parse.objects.DemographicValues;
import com.parse.operation.ParseOperation;

public class Helper {
	public static String ApplicationId_ = "fY92KsxpFTO4lNK15RXlgSMxDf74VFXcQ6RKfP0w";
	public static String ClientId_ = "ZSFkjCrHYj6TuKAtXQ5M6nDoa4bBcDTALycjiEzp";

	public static String setFirstCharacterToCaps(String arg) {
		StringBuilder tokenFixer = null;
		if (!arg.equals("")) {
			char upperCaseCharacter = Character.toUpperCase(arg.charAt(0));
			tokenFixer = new StringBuilder(arg);
			tokenFixer.setCharAt(0, upperCaseCharacter);
		}
		return tokenFixer.toString();
	}

	public static TextView getSimpleTextview(Context con){
		TextView valueTV = new TextView(con);
		valueTV.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		return valueTV;
	}
	
	public static String setFirstCharacterToNoCaps(String arg) {
		if (!arg.equals("")) {
			char lowerCaseCharacter = Character.toLowerCase(arg.charAt(0));
			StringBuilder tokenFixer = new StringBuilder(arg);
			tokenFixer.setCharAt(0, lowerCaseCharacter);
			return tokenFixer.toString();
		} else
			return "";

	}

	// Returns all the question options including the forced option.
	public static String[] extractFocusQuestionOptionsAsStringArray() {
		JSONArray parent_array = (JSONArray) SharedData.focus_question
				.getJSONArray("options_list");
		String[] returnOptions = new String[]{""};
		try {
			// Total options for this question.
			int total_options = parent_array.getJSONArray(0).length();
			returnOptions = new String[total_options + 1];
			int i = 0;
			for (; i < total_options; i++) {
				returnOptions[i] = parent_array.getJSONArray(0).getString(i);
			}
		} catch (Exception er) {

		}

		// Special case for the last forced option
		int last_option_index = SharedData.focus_question.getInt("option_last");
		returnOptions[returnOptions.length - 1] = DemographicValues.last_options[last_option_index];
		return returnOptions;
	}
	public static void ShowDialogue(String title, String data, Context cont) {
		try {
			Toast.makeText(cont, title + ": " + data, Toast.LENGTH_LONG).show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public static void ShowDialogue(String title, String data, Context cont, int duration) {
		try {
			Toast.makeText(cont, title + ": " + data, duration).show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static boolean isEmpty(String value) {
		if (value.equals("")) {
			return true;
		}
		return false;
	}

	public static boolean passwordCheck(String password, final Context con) {

		String pass = password;
		if (pass.length() < 7) {
			Helper.ShowDialogue("Error", "Password must be at least 7 characters long",
					con);
			return false;
		} else {
			boolean lower = false;
			boolean number = false;
			for (char c : pass.toCharArray()) {
				if (Character.isLowerCase(c)) {
					lower = true;
				} else if (Character.isDigit(c)) {
					number = true;
				}
			}
			if (!lower) {
				Helper.ShowDialogue("Error",
						"Password must contain at least one lowercase character", con);
				return false;
			} else if (!number) {
				Helper.ShowDialogue("Error",
						"Password must contain at least one number", con);
				return false;
			} else {
				return true;
			}
		}
	}

	public static Bitmap resizePostQuestionImage(Bitmap input, Context con) {
		File save_dir = Environment.getExternalStorageDirectory();
		FileOutputStream out;
		try {
			// Set the filename to teh current time.
			String filename = save_dir + "/Vote_Master_" + ".jpg";
			out = new FileOutputStream(filename);
			input.compress(Bitmap.CompressFormat.JPEG, 90, out);

			Uri imageUri = Uri.parse("file:///" + filename);
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(
					con.getContentResolver(), imageUri);
			return bitmap;
		} catch (Exception e) {
			return input;
		}

	}
	// This function executes when ever a question in the view is clicked
	// Deprecated.
	public static void onQuestionClicked(final ListView listview, int position,
			Context con) {
		SharedData.focus_question = (ParseObject) listview
				.getItemAtPosition(position);

		Intent intent = new Intent();
		intent.setClass(con, ViewQuestionActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		SharedData.focus_question = (ParseObject) listview
				.getItemAtPosition(position);

		con.startActivity(intent);
	}
	public static int max(double... arguments) {
		double max = 0;
		for (int i = 0; i < arguments.length; i++) {
			max = Math.max(arguments[i], max);
		}
		return (int) max;
	}

	public static boolean isSizeEqual(String[] arr, int[] arrSecond) {
		return (arr.length == arrSecond.length);
	}

	public static List<double[]> convertChunkToList(List<ParseObject> data,
			String columnName, int numDemographicValues) {
		if (data.size() == 0) {
			ParseOperation.notifyCorruptQuestion(
					SharedData.focus_question.getObjectId(),
					"cannot convert to list, in function convertToList, columnname = "
							+ columnName + ", numDemographicValues = "
							+ numDemographicValues + ", data.size()="
							+ data.size());
		}
		List<double[]> displayVoteData = new ArrayList<double[]>();

		for (int i = 0; i < data.size(); i++) {
			double[] rowHolder = new double[numDemographicValues];

			// Extract each demographic value.
			for (int j = 0; j < numDemographicValues; j++) {
				rowHolder[j] = data.get(i).getInt(columnName + "_" + (j));
			}
			displayVoteData.add(i, rowHolder);
		}

		return displayVoteData;
	}
	// Converts a parse-object list to a usable double value list.
	// The conversion mirrors the array so we mirror again at the end before
	// returning the answer.
	public static List<double[]> convertToList(List<ParseObject> data,
			String columnName, int numDemographicValues) {
		if (data.size() == 0) {
			ParseOperation.notifyCorruptQuestion(
					SharedData.focus_question.getObjectId(),
					"cannot convert to list, in function convertToList, columnname = "
							+ columnName + ", numDemographicValues = "
							+ numDemographicValues + ", data.size()="
							+ data.size());
		}

		List<double[]> displayVoteData = new ArrayList<double[]>();
		// For each record in the list (which = number of options of the
		// question)
		for (int i = 0; i < data.size(); i++) {
			double[] rowHolder = new double[numDemographicValues];

			// Extract each demographic value.
			for (int j = 0; j < numDemographicValues; j++) {
				rowHolder[j] = data.get(i).getInt(columnName + "_" + (j + 1));
			}
			displayVoteData.add(rowHolder);
		}

		List<double[]> yAxisMirrored = new ArrayList<double[]>();
		if (displayVoteData != null) {
			// Reverse the order since the array is mirrored.
			for (int i = 0; i < displayVoteData.size(); i++) {
				yAxisMirrored.add(displayVoteData.get(displayVoteData.size()
						- i - 1));
			}
		}
		return yAxisMirrored;
	}
	public static void LaunchActivity(Context con, Class<?> obj) {
		Intent intent = new Intent();
		intent.setClass(con, obj);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		con.startActivity(intent);
	}

	public static File getTempFile(Context cont) {
		// it will return /sdcard/image.tmp
		final File path = new File(Environment.getExternalStorageDirectory(),
				cont.getPackageName());
		if (!path.exists()) {
			path.mkdir();
		}
		return new File(path, "image.tmp");
	}

	public static Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 512;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}
	public static void PopulateSpinner(Spinner id,final Context con, String[] arr) {
		ArrayAdapter<String> adp = new ArrayAdapter<String>(con,
				android.R.layout.simple_list_item_1, arr);
		adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		id.setAdapter(adp);
	}
	public static byte[] ConverBitmapToByteArray(Bitmap bmp) {
		bmp = BitmapTextStitcher.getPerfectScaledImage(bmp,
				SharedData.IMAGE_UPLOAD_MAX_DIMENSION);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public static Boolean AmIOnline(Context con) {
		final ConnectivityManager conMgr = (ConnectivityManager) con
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			// notify user you are online
			return true;
		} else {
			// notify user you are not online
			return false;
		}
	}
	public static Bitmap ConvertByteArrayToBitmap(byte[] data) {
		Bitmap bmp;
		bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
		return bmp;
	}

	public static void setUpActionBar(FragmentActivity frag) {
		ActionBar viewQuestionActionBar = frag.getActionBar();
		viewQuestionActionBar.setDisplayShowTitleEnabled(false);
		viewQuestionActionBar.setDisplayHomeAsUpEnabled(true);
	}
}
