package com.parse.bitmap;

import com.parse.helper.Helper;
import com.parse.helper.SharedData;
import com.parse.objects.AnswerTemplates;
import com.parse.test.TestData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

// This class takes one bitmap or a set of bitmaps and places them in order
// under the questions statement and options.
public class BitmapTextStitcher {

	static Bitmap getThisColorBitmap(int color, int size, Config config) {
		Bitmap blank = Bitmap.createBitmap(size, size, config);
		Canvas canvas = new Canvas(blank);
		canvas.drawColor(color);
		return blank;
	}

	static void drawQuestionOnBitmap(Bitmap finalBitmap, Context con) {
		int scale = 20;
		Canvas canvas = new Canvas(finalBitmap);
		canvas.drawColor(Color.WHITE);

		// new antialised Paint
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG
				| Paint.SUBPIXEL_TEXT_FLAG);
		// text color - #3D3D3D
		paint.setColor(Color.rgb(61, 61, 61));
		// text size in pixels
		paint.setTextSize((int) (scale));
		// text shadow
		// paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
		paint.setTypeface(Typeface.DEFAULT_BOLD);

		// draw question statement.
		Rect bounds = new Rect();
		String textToDraw = "- " + Helper
		.setFirstCharacterToCaps(SharedData.focus_question.get(
				"statement").toString());
		
		paint.getTextBounds(textToDraw, 0, textToDraw.length(), bounds);
		int x = 25;
		int y = 75;
		int spacing = 10;
		int i = 0;
		int maxDisplayLimit = 30;
		
		paint.setColor(Color.rgb(79, 166, 170));
		paint.setTypeface(Typeface.SERIF);
		canvas.drawText("Generated using \"RaiPoll\", only on Android", x, 25, paint);
		
		paint.setColor(Color.rgb(61, 61, 61));
		paint.setTypeface(Typeface.DEFAULT_BOLD);

		// If the questions statement fits in 1 line, all good, other wise
		// write the question in multiple lines.
		if (textToDraw.length() < maxDisplayLimit) {
			canvas.drawText(textToDraw, x, y, paint);
		} else {

			while (textToDraw.length() != 0) {
				String drawme = textToDraw.substring(0,
						Math.min(maxDisplayLimit, textToDraw.length()));
				canvas.drawText(drawme, x, y + (bounds.height() + spacing)
						* (i + 1), paint);
				i++;
				textToDraw = textToDraw.substring(
						Math.min(maxDisplayLimit, textToDraw.length()),
						textToDraw.length());
			}

		}

		int offset = 2;
		// Draw Border
		canvas.drawLine(0 + offset, 0 + offset, 0 + offset,
				finalBitmap.getHeight() - offset, paint);
		canvas.drawLine(0 + offset, 0 + offset,
				finalBitmap.getWidth() - offset, 0 + offset, paint);
		canvas.drawLine(0 + offset, finalBitmap.getHeight() - offset,
				finalBitmap.getWidth() - offset, finalBitmap.getHeight()
						- offset, paint);
		canvas.drawLine(finalBitmap.getWidth() - offset, 0 + offset,
				finalBitmap.getWidth() - offset, finalBitmap.getHeight()
						- offset, paint);

		paint.setTypeface(Typeface.DEFAULT);
		String[] options = Helper.extractFocusQuestionOptionsAsStringArray();

		// j counts the index of the question options, it is independent of the
		// idsplay position etc.
		for (int j = 0; j < options.length; i++, j++)
			canvas.drawText(AnswerTemplates.questionIndex[j] + options[j], x, y
					+ (bounds.height() + spacing) * (i + 1) + 15, paint);

	}

	public static Bitmap forceRescaleFixWidth(Bitmap resizeMe, int setWidth) {
		if (resizeMe.getWidth() > resizeMe.getHeight()) {
			int correctHeight = setWidth / 2;
			return Bitmap.createScaledBitmap(resizeMe, setWidth, correctHeight,
					true);
		} else {
			int correctHeight = setWidth;
			return Bitmap.createScaledBitmap(resizeMe, setWidth, correctHeight,
					true);
		}
	}

	public static Bitmap forceRescaleFixHeight(Bitmap resizeMe, int setHeight) {
		if (resizeMe.getHeight() > resizeMe.getWidth()) {
			int correctWidth = setHeight / 2;
			return Bitmap.createScaledBitmap(resizeMe, correctWidth, setHeight,
					true);
		} else {
			int correctWidth = setHeight;
			return Bitmap.createScaledBitmap(resizeMe, correctWidth, setHeight,
					true);
		}
	}

	public static Bitmap getPerfectScaledImage(Bitmap resizeMe, int maxAllowed) {
		int allowed = maxAllowed;

		// If the width and height are less than the permitted amount, just
		// return the image.
		if (resizeMe.getWidth() < maxAllowed
				&& resizeMe.getHeight() < maxAllowed)
			return resizeMe;

		// One or both of height/width is greater than the allowed, hence resize
		float aspectRatio = ((float) resizeMe.getWidth())
				/ resizeMe.getHeight();

		if (resizeMe.getHeight() > resizeMe.getWidth()) {
			int correctWidth = (int) (aspectRatio * allowed);
			return Bitmap.createScaledBitmap(resizeMe, correctWidth, allowed,
					true);
		} else {
			int correctHeight = (int) (1 / aspectRatio * allowed);
			return Bitmap.createScaledBitmap(resizeMe, allowed, correctHeight,
					true);
		}
	}

	public static Bitmap CombineLeftToRight(int finalWidth, Bitmap... image) {
		Bitmap answer = Bitmap.createBitmap(finalWidth, image[0].getHeight(),
				image[0].getConfig());

		for (int i = 0; i < image.length; i++) {
			image[i] = forceRescaleFixWidth(image[i], finalWidth / image.length);
		}

		for (int i = 0; i < image.length; i++) {
			int[] copydata = new int[image[i].getWidth() * image[i].getHeight()];
			image[i].getPixels(copydata, 0, image[i].getWidth(), 0, 0,
					image[i].getWidth(), image[i].getHeight());

			// Enter in the right place
			answer.setPixels(copydata, 0, image[i].getWidth(),
					image[i].getWidth() * i, 0, image[i].getWidth(),
					image[i].getHeight());

		}
		return answer;

	}

	public static Bitmap CombineTopToBottom(int finalHeight, Bitmap... image) {
		Bitmap answer = Bitmap.createBitmap(image[0].getWidth(), finalHeight,
				image[0].getConfig());

		for (int i = 0; i < image.length; i++) {
			int[] copydata = new int[image[i].getWidth() * image[i].getHeight()];
			image[i].getPixels(copydata, 0, image[i].getWidth(), 0, 0,
					image[i].getWidth(), image[i].getHeight());

			// Enter in the right place
			answer.setPixels(copydata, 0, image[i].getWidth(), 0,
					image[i].getHeight() * i, image[i].getWidth(),
					image[i].getHeight());
		}
		return answer;
	}

	public static Bitmap stitchQuestionOnTop(Context con, Bitmap... image) {

		Bitmap[] shareThese;
		// Check to see if the question has image or not.
		if (image[0] == null) {
			shareThese = new Bitmap[image.length - 1];
			for (int i = 0; i < image.length - 1; i++) {
				shareThese[i] = image[i + 1];
			}
		} else {
			shareThese = image;
		}
		int singleTileSize = 512;
		Config config = shareThese[0].getConfig();
		Bitmap questionTextBitmap = Bitmap.createBitmap(singleTileSize,
				singleTileSize, config);
		drawQuestionOnBitmap(questionTextBitmap, con);

		Bitmap finalShare = null;
		Bitmap secondRow = null;
		Bitmap firstRow = null;
		Bitmap thirdRow = null;

		switch (shareThese.length) {
			case 1 :
				finalShare = CombineLeftToRight(singleTileSize * 2,
						questionTextBitmap, Bitmap.createScaledBitmap(
								shareThese[0], singleTileSize, singleTileSize,
								true));
				break;
			case 2 :
				firstRow = CombineLeftToRight(singleTileSize * 2,
						questionTextBitmap, Bitmap.createScaledBitmap(
								shareThese[0], singleTileSize, singleTileSize,
								true));
				secondRow = CombineLeftToRight(singleTileSize * 2,
						Bitmap.createScaledBitmap(shareThese[1],
								singleTileSize, singleTileSize, true),
						getThisColorBitmap(Color.WHITE, singleTileSize, config));
				finalShare = CombineTopToBottom(singleTileSize * 2, firstRow,
						secondRow);
				break;
			case 3 :
				firstRow = CombineLeftToRight(singleTileSize * 2,
						questionTextBitmap, Bitmap.createScaledBitmap(
								shareThese[0], singleTileSize, singleTileSize,
								true));
				secondRow = CombineLeftToRight(singleTileSize * 2,
						Bitmap.createScaledBitmap(shareThese[1],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[2],
								singleTileSize, singleTileSize, true));
				finalShare = CombineTopToBottom(singleTileSize * 2, firstRow,
						secondRow);
				break;
			case 4 :
				firstRow = CombineLeftToRight(singleTileSize * 3,
						questionTextBitmap, Bitmap.createScaledBitmap(
								shareThese[0], singleTileSize, singleTileSize,
								true), Bitmap.createScaledBitmap(shareThese[1],
								singleTileSize, singleTileSize, true));
				secondRow = CombineLeftToRight(singleTileSize * 3,
						Bitmap.createScaledBitmap(shareThese[2],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[3],
								singleTileSize, singleTileSize, true),
						getThisColorBitmap(Color.WHITE, singleTileSize, config));

				finalShare = CombineTopToBottom(singleTileSize * 2, firstRow,
						secondRow);
				break;
			case 5 :
				firstRow = CombineLeftToRight(singleTileSize * 3,
						questionTextBitmap, Bitmap.createScaledBitmap(
								shareThese[0], singleTileSize, singleTileSize,
								true), Bitmap.createScaledBitmap(shareThese[1],
								singleTileSize, singleTileSize, true));
				secondRow = CombineLeftToRight(singleTileSize * 3,
						Bitmap.createScaledBitmap(shareThese[2],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[3],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[4],
								singleTileSize, singleTileSize, true));

				finalShare = CombineTopToBottom(singleTileSize * 2, firstRow,
						secondRow);

				break;
			case 6 :
				firstRow = CombineLeftToRight(singleTileSize * 3,
						questionTextBitmap, Bitmap.createScaledBitmap(
								shareThese[0], singleTileSize, singleTileSize,
								true), Bitmap.createScaledBitmap(shareThese[1],
								singleTileSize, singleTileSize, true));
				secondRow = CombineLeftToRight(singleTileSize * 3,
						Bitmap.createScaledBitmap(shareThese[2],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[3],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[4],
								singleTileSize, singleTileSize, true));
				thirdRow = CombineLeftToRight(
						singleTileSize * 3,
						Bitmap.createScaledBitmap(shareThese[5],
								singleTileSize, singleTileSize, true),
						getThisColorBitmap(Color.WHITE, singleTileSize, config),
						getThisColorBitmap(Color.WHITE, singleTileSize, config));

				finalShare = CombineTopToBottom(singleTileSize * 3, firstRow,
						secondRow, thirdRow);

				break;
			case 7 :
				firstRow = CombineLeftToRight(singleTileSize * 3,
						questionTextBitmap, Bitmap.createScaledBitmap(
								shareThese[0], singleTileSize, singleTileSize,
								true), Bitmap.createScaledBitmap(shareThese[1],
								singleTileSize, singleTileSize, true));
				secondRow = CombineLeftToRight(singleTileSize * 3,
						Bitmap.createScaledBitmap(shareThese[2],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[3],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[4],
								singleTileSize, singleTileSize, true));
				thirdRow = CombineLeftToRight(singleTileSize * 3,
						Bitmap.createScaledBitmap(shareThese[5],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[6],
								singleTileSize, singleTileSize, true),
						getThisColorBitmap(Color.WHITE, singleTileSize, config));

				finalShare = CombineTopToBottom(singleTileSize * 3, firstRow,
						secondRow, thirdRow);

				break;
			case 8 :
				firstRow = CombineLeftToRight(singleTileSize * 3,
						questionTextBitmap, Bitmap.createScaledBitmap(
								shareThese[0], singleTileSize, singleTileSize,
								true), Bitmap.createScaledBitmap(shareThese[1],
								singleTileSize, singleTileSize, true));
				secondRow = CombineLeftToRight(singleTileSize * 3,
						Bitmap.createScaledBitmap(shareThese[2],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[3],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[4],
								singleTileSize, singleTileSize, true));
				thirdRow = CombineLeftToRight(singleTileSize * 3,
						Bitmap.createScaledBitmap(shareThese[5],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[6],
								singleTileSize, singleTileSize, true),
						Bitmap.createScaledBitmap(shareThese[7],
								singleTileSize, singleTileSize, true));

				finalShare = CombineTopToBottom(singleTileSize * 3, firstRow,
						secondRow, thirdRow);
				break;

			default :
				finalShare = getThisColorBitmap(Color.WHITE, singleTileSize,
						config);

		}

		return finalShare;
	}
}
