package com.fahadaltimimi.model;

import java.io.File;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class LoadFileImageTask extends AsyncTask<String, Void, Bitmap> {

	private static final String TAG = "LoadFileImageTask";

	private ImageView mImageView;
	private Object mTag;

	public LoadFileImageTask(ImageView imageView) {
		mImageView = imageView;
	}

	@Override
	protected Bitmap doInBackground(String... files) {
		return getImageFromFile(files[0]);
	}

	public static Bitmap getImageFromFile(String path) {

		File imgFile = new File(path);

		final int IMAGE_MAX_SIZE = 1200000; // 1.2MP

		// Decode image size
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);

		int scale = 1;
		while ((options.outWidth * options.outHeight)
				* (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
			scale++;
		}
		Log.d(TAG, "scale = " + scale + ", " + "orig-width: "
				+ options.outWidth + ", orig-height: " + options.outHeight);

		Bitmap bitmap = null;
		if (scale > 1) {
			scale--;
			// scale to max possible inSampleSize that still yields an image
			// larger than target
			options = new BitmapFactory.Options();
			options.inSampleSize = scale;
			bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

			// resize to desired dimensions
			int height = bitmap.getHeight();
			int width = bitmap.getWidth();
			Log.d(TAG, "1th scale operation dimenions - width: " + width
					+ ", height: " + height);

			double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
			double x = (y / height) * width;

			Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, (int) x,
					(int) y, true);
			bitmap.recycle();
			bitmap = scaledBitmap;

			System.gc();
		} else {
			bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		}

		Log.d(TAG, "bitmap size - width: " + bitmap.getWidth() + ", height: "
				+ bitmap.getHeight());
		return bitmap;
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
