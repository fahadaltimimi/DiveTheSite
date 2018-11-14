package com.fahadaltimimi.divethesite.model;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fahadaltimimi.divethesite.model.DiveSite;

public class DiveSiteImageAdapter extends PagerAdapter {

	private static final int DEFAULT_IMAGEVIEW_PADDING = 10;

	Context mContext;
	DiveSite mDiveSite;

	DiveSiteImageAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		if (mDiveSite == null) {
			return 0;
		} else {
			return mDiveSite.getPicturesCount();
		}
	}

	public void setDiveSite(DiveSite diveSite) {
		mDiveSite = diveSite;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((ImageView) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ImageView imageView = new ImageView(mContext);

		File imgFile = new File(mDiveSite.getPicture(position)
				.getBitmapFilePath());
		if (imgFile.exists()) {
			Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
					.getAbsolutePath());

			imageView.setImageBitmap(myBitmap);
			// imageView.setPadding(DEFAULT_IMAGEVIEW_PADDING, 0,
			// DEFAULT_IMAGEVIEW_PADDING, 0);

			((ViewPager) container).addView(imageView, 0);
		}
		return imageView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((ImageView) object);
	}
}
