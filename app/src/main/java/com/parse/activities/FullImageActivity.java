package com.parse.activities;

import java.io.File;
import java.io.FileOutputStream;

import com.parse.customui.ZoomableImageView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebView;
import android.widget.ImageView;

public class FullImageActivity extends Activity {

	public static Bitmap questionImage = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_screen_image);

		if (questionImage != null) {
			ZoomableImageView imageView = (ZoomableImageView) findViewById(R.id.full_image_view);
			imageView.setImageBitmap(questionImage);
			
			//--------------
			// TODO(Han): use webview to show image
			// it has free pinch zoom
			File save_dir = Environment.getExternalStorageDirectory();
			FileOutputStream out;
			String filename = save_dir + "/Vote_Master_" + ".jpg";
			try {
				out = new FileOutputStream(filename);
				questionImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			WebView wv = (WebView) findViewById(R.id.yourwebview);
			wv.getSettings().setBuiltInZoomControls(true);
			wv.loadUrl("file:///" + filename);
			
		}

	}
}
