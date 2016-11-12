package com.parse.helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.parse.bitmap.BitmapTextStitcher;

public class ShareHelper {

	public static void stitchAndShare(Context con, Bitmap... these) {
		Bitmap questionAndChart = BitmapTextStitcher.stitchQuestionOnTop(con,
				these);
		shareOneImage(questionAndChart, con);
	}

	public static String SHARE_EXTRA_TEXT = "Question analytics";
	public static String SHARE_EXTRA_SUBJECT = "RaiPoll generated image (only on Android).";

	public static void shareOneImage(Bitmap shareBitmap, Context con) {
		File save_dir = Environment.getExternalStorageDirectory();
		FileOutputStream out;
		try {
			// Set the filename to the current time.
			String filename = save_dir + "/rai_image" + ".jpg";
			out = new FileOutputStream(filename);

			shareBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			Intent share = new Intent(Intent.ACTION_SEND);
			share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			share.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

			share.putExtra(Intent.EXTRA_SUBJECT, SHARE_EXTRA_SUBJECT);
			share.putExtra(Intent.EXTRA_TEXT, SHARE_EXTRA_TEXT);
			share.putExtra(Intent.EXTRA_STREAM,
					Uri.parse("file:///" + filename));
			share.setType("image/png");

			con.startActivity(Intent.createChooser(share, "Share to"));
		} catch (FileNotFoundException e) {
			Helper.ShowDialogue("Sharing Issue",
					"Some error occured:" + e.getMessage(), con);
		}
	}
}
