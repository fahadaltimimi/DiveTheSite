package com.fahadaltimimi.model;

import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class LoadOnlineImageTask extends AsyncTask<String, Void, Bitmap> {

	ImageView mImageView;
	Object mTag;

	public LoadOnlineImageTask(ImageView imageView) {
		mImageView = imageView;
	}

	@Override
	protected Bitmap doInBackground(String... urls) {
		String urlDisplay = urls[0];
		Bitmap imageResult = null;

		try {
			InputStream in = new URL(urlDisplay).openStream();
			imageResult = BitmapFactory.decodeStream(in);
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}

		return imageResult;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		mImageView.setImageBitmap(result);
	}

	public Object getTag() {
		return mTag;
	}

	public void setTag(Object object) {
		mTag = object;
	}

    public ImageView getImageView() {
        return mImageView;
    }
}
