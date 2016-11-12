package com.parse.helper;

import java.util.HashMap;
import java.util.Map.Entry;

import org.achartengine.GraphicalView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

public class DialogueGenerator {

	public static void showCategorySubscriptionDialogue(final View v) {

	}

	public static void showChartShareSelectDialogue(final View v,
			final Bitmap downloadedQuestionImage,
			final HashMap<String, GraphicalView> charts, final Context con) {

		if (charts.size() == 0) {
			Helper.ShowDialogue("Results",
					"Vote first to view and share results.", con);
			return;
		}

		AlertDialog.Builder alerBuilder = new AlertDialog.Builder(
				v.getContext());
		final String[] deviceNameArr = new String[charts.size()];
		int indexer = 0;

		// Create the chart names list to show in the dialogue box.
		for (Entry<String, GraphicalView> entry : charts.entrySet()) {
			deviceNameArr[indexer] = (String) entry.getKey();
			indexer = indexer + 1;
		}

		final boolean[] selectedItems = new boolean[deviceNameArr.length];
		for (int i = 0; i < deviceNameArr.length; i++) {
			deviceNameArr[i] = deviceNameArr[i];
			selectedItems[i] = true;
		}
		alerBuilder
				.setMultiChoiceItems(deviceNameArr, selectedItems,
						new DialogInterface.OnMultiChoiceClickListener() {
							@Override
							public void onClick(
									DialogInterface dialogInterface, int i,
									boolean b) {
								Log.e("CheckStatus", String.valueOf(b));
							}
						})
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int ii) {

						int countOfSelected = 0;
						for (int i = 0; i < selectedItems.length; i++) {
							Log.e("Output", String.valueOf(selectedItems[i]));
							if (selectedItems[i]) {
								countOfSelected++;
							}
						}
						if (countOfSelected == 0) {
							Helper.ShowDialogue("Message", "no chart selected",
									con);
							return;
						}

						// Send these to the sticthing utility.
						Bitmap[] send = new Bitmap[countOfSelected + 1];
						send[0] = downloadedQuestionImage;
						for (int i = 0, j = 1; i < selectedItems.length; i++) {

							// If a particular chart is selected, get the
							// specific chart based on its name from the
							// hash map.
							if (selectedItems[i]) {
								send[j] = charts.get(deviceNameArr[i])
										.toBitmap();
								j++;
							}
						}
						ShareHelper.stitchAndShare(v.getContext(), send);
					}
				})
				.setCancelable(true)
				.setTitle("Share only selected charts")
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
		alerBuilder.create().show();
	}
}
