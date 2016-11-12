package com.parse.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.DemographicValues;

public class ListQuestionAdapter extends ArrayAdapter<ParseObject> {
	private final Context context;
	private List<ParseObject> values;

	static class ViewHolder {
		TextView statement;
		TextView postDate;
		TextView category;
		TextView total_votes;
		ImageView image;
		LinearLayout imageFrame;
	}

	public ListQuestionAdapter(Context context, List<ParseObject> values) {
		super(context, R.layout.single_question_list, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// Create the holder
		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.single_question_list,
					parent, false);

			holder = new ViewHolder();

			// Set the question statement after the processing.
			TextView textView = (TextView) convertView
					.findViewById(R.id.single_question_title);
			holder.statement = textView;
			// Set the correct category
			textView = (TextView) convertView
					.findViewById(R.id.single_question_category);
			holder.category = textView;

			// Set the postdate here.
			textView = (TextView) convertView
					.findViewById(R.id.single_question_postdate);
			holder.postDate = textView;

			holder.total_votes = (TextView) convertView
					.findViewById(R.id.single_question_totalvotes);

			holder.image = (ImageView) convertView
					.findViewById(R.id.single_question_icon);
			holder.image.setAdjustViewBounds(true);

			// holder.imageFrame = (LinearLayout) convertView
			// .findViewById(R.id.single_question_image_frame);

			// Save for reuse.
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// Now that the view might have been recycled, set the correct data
		ParseObject currentObject = values.get(position);

		// Process and set the right statement.
		String statementWithCaps = Helper.setFirstCharacterToCaps(currentObject
				.get("statement").toString());
		holder.statement.setText(statementWithCaps);
		holder.category.setText(DemographicValues.categories[currentObject
				.getInt("category")]);

		Date printDate = currentObject.getCreatedAt();
		// EEE MMM dd HH:mm:ss zzz yyyy", which looks something like "Tue Jun 22
		// 13:07:00 PDT 1999".
		// holder.postDate.setText(DateFormat.format("HH:mm MMM dd yyyy",
		// printDate));
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		String postTime = DateFormat.format("MM/dd/yyyy HH:mm:ss", printDate)
				.toString();
		String currentTime = format.format(Calendar.getInstance(
				TimeZone.getTimeZone("PST")).getTime());

		Date d1 = null;
		Date d2 = null;

		try {
			d1 = format.parse(postTime);
			d2 = format.parse(currentTime);

			// in milliseconds
			long diff = d2.getTime() - d1.getTime();

			int diffSeconds = (int) (diff / 1000 % 60);
			int diffMinutes = (int) (diff / (60 * 1000) % 60);
			int diffHours = (int) (diff / (60 * 60 * 1000) % 24);
			int diffDays = (int) (diff / (24 * 60 * 60 * 1000));

			if (diffDays > 0) {
				holder.postDate.setText(diffDays + " days ago");
			} else if (diffHours > 0) {
				holder.postDate.setText(diffHours + " hours ago");
			} else if (diffMinutes > 0) {
				holder.postDate.setText(diffMinutes + " minutes ago");
			} else {
				holder.postDate.setText(diffSeconds + " seconds ago");
			}

		} catch (Exception e) {
			Log.e("error", e.getMessage());
			// e.printStackTrace();
		}
		// holder.imageFrame.setVisibility(View.GONE);

		ParseObject voteData = SharedData.quetionVotes.get(currentObject
				.getObjectId());
		int totalV = 0;
		if (voteData != null) {
			totalV = voteData.getInt("total_votes");
		}
		holder.total_votes.setText(totalV + "");
		// --------------- Image Magic
		// Download the image
		if (currentObject.getBoolean("has_image")) {
			// holder.imageFrame.setVisibility(View.VISIBLE);
			if (SharedData.dowloadedImageMap.containsKey(currentObject
					.getObjectId())) {
				holder.image
						.setImageBitmap((Bitmap) SharedData.dowloadedImageMap
								.get(currentObject.getObjectId()));
			} else {
				holder.image.setImageBitmap(null);
				DownloadNonBlockingQuestionImage(currentObject, context,
						(ImageView) convertView
								.findViewById(R.id.single_question_icon),
						position);

				// try {
				// Blocking image fetch.
				// final ParseFile question_image = (ParseFile) currentObject
				// .get("image");
				// byte[] data = question_image.getData();
				// Bitmap image_bitmap = Helper.ConvertByteArrayToBitmap(data);
				// holder.image.setImageBitmap(image_bitmap);
				// dowloadedImageMap.put(position, image_bitmap);

				// } catch (ParseException e) {
				// e.printStackTrace();
				// }

			}
		} else {
			holder.image.setImageBitmap(null);
		}

		return convertView;
	}

	// Downloads the image that is associated with the question.
	public void DownloadNonBlockingQuestionImage(final ParseObject question,
			Context con, final ImageView rView, final int position) {
		final ParseFile question_image = (ParseFile) question.get("image");
		question_image.getDataInBackground(new GetDataCallback() {
			public void done(byte[] data, com.parse.ParseException e) {
				if (e == null) {
					Bitmap image_bitmap = Helper.ConvertByteArrayToBitmap(data);
					// Check to see if the image was downloaded earlier

					rView.setImageBitmap(image_bitmap);
					SharedData.dowloadedImageMap.put(question.getObjectId(),
							image_bitmap);
					System.out.print("pos:" + position);
				} else {
					// something went wrong
				}
			}
		});

	}
}