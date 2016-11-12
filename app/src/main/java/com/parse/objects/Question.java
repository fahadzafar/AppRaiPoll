package com.parse.objects;

import org.json.JSONArray;

import android.content.Context;
import android.graphics.Bitmap;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.helper.Helper;
import com.parse.operation.ParseOperation;

public class Question {
	public static final String CLASS_TITLE = "Question";

	public ParseObject posted_by;
	public String statement = "";
	public JSONArray user_entered_options;
	public boolean has_image = false;
	public Bitmap image = null;
	ParseFile file;
	public int category = 0;

	public void UploadAsParseObject(Context con) {
		ParseObject object = new ParseObject(CLASS_TITLE);
		object.put("statement", statement);
		object.put("posted_by", posted_by);
		// object.put("total_votes", 0);
		object.put("total_flags", 0);
		object.put("type", 0);
		// Add the options
		object.put("total_options", user_entered_options.length());
		object.add("options_list", user_entered_options);

		// Add the category
		object.put("category", category);
		try {
			if (has_image) {
				file = new ParseFile("question_image_data.jpg",
						Helper.ConverBitmapToByteArray(image));
				file.save();
				object.put("image", file);
			}

			// If there is no file, then a false is saved, otherwise a true is
			// saved and the file is referenced.
			object.put("has_image", has_image);
			ParseACL acl = ParseOperation.getFullAccessACL();
			object.setACL(acl);
			object.save();

			// After the successful save, the objectID has been filled, so
			// insert the empty vote table records.
			// One extra is inserted for the last forced option.
			for (int i = 0; i < user_entered_options.length(); i++)
				ParseOperation.InsertEmptyVoteTableRecord(object.getObjectId(),
						i, con);

			// Total votes record for the question.
			ParseOperation.InsertEmptyALLQuestionVoteTableRecord(object, con);
		} catch (ParseException e) {
			Helper.ShowDialogue("Uploading Image Eror:",
					"Upload image failed.", con);
			e.printStackTrace();
		}
	}

}
