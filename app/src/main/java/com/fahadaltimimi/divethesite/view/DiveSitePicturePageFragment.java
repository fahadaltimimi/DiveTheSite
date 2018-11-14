package com.fahadaltimimi.divethesite.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.fahadaltimimi.divethesite.data.DiveCursorLoaders;
import com.fahadaltimimi.divethesite.data.DiveCursorLoaders.PicturesListCursorLoader;
import com.fahadaltimimi.divethesite.data.DiveSiteDatabaseHelper.PictureCursor;
import com.fahadaltimimi.model.LoadFileImageTask;
import com.fahadaltimimi.model.LoadOnlineImageTask;
import com.fahadaltimimi.divethesite.R;
import com.fahadaltimimi.divethesite.model.DiveLog;
import com.fahadaltimimi.divethesite.model.DiveSite;
import com.fahadaltimimi.divethesite.model.DiveSitePicture;

public class DiveSitePicturePageFragment extends DiveSitePageFragment implements
		LoaderCallbacks<Cursor> {

    private static final String timeStampDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

	private static final String TAG = "DiveSitePage2Fragment";
	private static final String ARG_DIVESITE = "DIVESITE";

	public static final int REQUEST_PICK_IMAGE = 0;
	public static final int REQUEST_CAPTURE_IMAGE = 1;

	private PictureCursor mPictureCursor;

	private ViewGroup mDiveSitePictureViewContainer;
	private Button mDiveSiteAddPictureGallery, mDiveSiteAddPictureCamera;
	private ViewGroup mDiveSiteImageGallery;
	private ImageView mDiveSiteSelectedImage;
	private View mDiveSiteSelectedImageProgress;
	private ImageButton mDiveSiteDeletePicture;

	private String mLastPictureFileCreated;

	private HashMap<DiveSitePicture, View> mDiveSitePictureViews = new HashMap<DiveSitePicture, View>();

	public static DiveSitePicturePageFragment newInstance(DiveSite diveSite,
			DiveLog diveLog) {
		Bundle args = new Bundle();
		args.putParcelable(ARG_DIVESITE, diveSite);
		args.putParcelable(DiveLogFragment.ARG_DIVELOG, diveLog);
		DiveSitePicturePageFragment rf = new DiveSitePicturePageFragment();
		rf.setArguments(args);
		return rf;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_divesite_pictures,
				parent, false);

		mDiveSitePictureViewContainer = (ViewGroup) view
				.findViewById(R.id.divesite_pictures_view_container);

		mDiveSiteAddPictureGallery = (Button) view
				.findViewById(R.id.divesite_add_picture_gallery);
		mDiveSiteAddPictureGallery
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Ask user to select an image on their phone to add to
						// the dive site
						Intent intent = new Intent();
						intent.setType("image/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								REQUEST_PICK_IMAGE);
					}
				});

		mDiveSiteAddPictureCamera = (Button) view
				.findViewById(R.id.divesite_add_picture_camera);
		if (getActivity().getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			mDiveSiteAddPictureCamera.setVisibility(View.VISIBLE);
			mDiveSiteAddPictureCamera
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Intent takePictureIntent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);

							// Ensure that there's a camera activity to handle
							// the intent
							if (takePictureIntent.resolveActivity(getActivity()
									.getPackageManager()) != null) {
								// Create the File where the photo should go
								File photoFile = null;
								try {
									photoFile = createImageFile();
								} catch (IOException e) {
									// Error occurred while creating the File
									e.printStackTrace();
								}
								// Continue only if the File was successfully
								// created
								if (photoFile != null) {
									mLastPictureFileCreated = photoFile
											.getAbsolutePath();

									takePictureIntent.putExtra(
											MediaStore.EXTRA_OUTPUT,
											Uri.fromFile(photoFile));
									startActivityForResult(takePictureIntent,
											REQUEST_CAPTURE_IMAGE);
								}
							}
						}
					});
		} else {
			// Hide picture button since camera not available
			mDiveSiteAddPictureCamera.setVisibility(View.GONE);
		}

		mDiveSiteDeletePicture = (ImageButton) view
				.findViewById(R.id.delete_dive_site_picture);
		mDiveSiteDeletePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DiveSitePicture picture = (DiveSitePicture) mDiveSiteDeletePicture
						.getTag();
				if (picture != null) {
					mDiveSite.removePicture(picture);
					if (picture.getLocalId() != -1) {
						mDiveSiteManager.deleteDiveSitePicture(picture
								.getLocalId());
					}

					View diveSitePictureImage = mDiveSitePictureViews
							.remove(picture);
					mDiveSiteImageGallery.removeView(diveSitePictureImage);
					mDiveSiteSelectedImage.setImageDrawable(null);

					mDiveSiteDeletePicture.setVisibility(View.INVISIBLE);

					mDiveSite.setPublished(false);
					mDiveSiteManager.saveDiveSite(mDiveSite);

					updateUI();
				}
			}
		});

		mDiveSiteSelectedImage = (ImageView) view
				.findViewById(R.id.divesite_selected_image);
		mDiveSiteSelectedImageProgress = view
				.findViewById(R.id.divesite_picture_progress_bar);

		mDiveSiteImageGallery = (ViewGroup) view
				.findViewById(R.id.divesite_image_gallery);
		mDiveSiteImageGallery.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mDiveSitePictureViewContainer
						.requestDisallowInterceptTouchEvent(true);
				return false;
			}
		});

		setDiveSiteAvailable(mDiveSite);

		updateUI();

		return view;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// Hide edit mode, don't need it for this fragment
		MenuItem editModeMenuItem = menu.findItem(R.id.menu_item_edit_mode);
		editModeMenuItem.setVisible(false);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		if (requestCode == REQUEST_PICK_IMAGE
				&& resultCode == Activity.RESULT_OK) {
			// User has selected an image and came back
			// Add the image to the dive site, gallery and selected image view
			Uri selectedImage = data.getData();
			String imagePathToSave = null;
			Bitmap image = null;
			try {
				image = BitmapFactory.decodeStream(getActivity()
						.getContentResolver().openInputStream(selectedImage));
				imagePathToSave = mDiveSiteManager.saveImageInternalStorage(
						image, "DiveSiteImage");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (imagePathToSave != null) {
				DiveSitePicture diveSitePicture = new DiveSitePicture();
				diveSitePicture.setBitmapFilePath(imagePathToSave);

				// Save dive site first, with publish flag set
				mDiveSite.setPublished(false);
				mDiveSite.addPicture(diveSitePicture);
				mDiveSiteManager.saveDiveSite(mDiveSite);

				setDiveSitePicture(diveSitePicture);

				updateUI();
			}
		} else if (requestCode == REQUEST_CAPTURE_IMAGE
				&& resultCode == Activity.RESULT_OK) {
			DiveSitePicture diveSitePicture = new DiveSitePicture();
			diveSitePicture.setBitmapFilePath(mLastPictureFileCreated);

			// Save dive site first, with publish flag set
			mDiveSite.setPublished(false);
			mDiveSite.addPicture(diveSitePicture);
			mDiveSiteManager.saveDiveSite(mDiveSite);

			setDiveSitePicture(diveSitePicture);

			updateUI();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void DoDiveSiteAvailable() {
		super.DoDiveSiteAvailable();

		// Initialize the loader to load the list of Dive Site Pictures
		getLoaderManager().initLoader(DiveCursorLoaders.LOAD_DIVESITE_PICTURES,
				null, this);
	}

	@Override
	protected void updateUI() {
		super.updateUI();

		if (mDiveSite != null) {
			if (mDiveSite.getUserId() == mDiveSiteManager.getLoggedInDiverId()) {
				mDiveSiteAddPictureGallery.setVisibility(View.VISIBLE);
				mDiveSiteAddPictureCamera.setVisibility(View.VISIBLE);
			} else {
				mDiveSiteAddPictureGallery.setVisibility(View.GONE);
				mDiveSiteAddPictureCamera.setVisibility(View.GONE);
			}
		}
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String imageFileName = "JPEG_" + timeStampDateFormat + "_";
		File storageDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		return image;
	}

	private void updateLocalPictures() {
		// First add retrieved sites, making sure not to duplicate
		mPictureCursor.moveToFirst();
		while (!mPictureCursor.isAfterLast()) {
			DiveSitePicture diveSitePicture = mPictureCursor
					.getDiveSitePicture();
			int diveSitePictureIndex = -1;
			if (diveSitePicture.getOnlineId() != -1) {
				diveSitePictureIndex = mDiveSite
						.getPictureIndexOnlineID(diveSitePicture.getOnlineId());
			}
			if (diveSitePictureIndex != -1) {
				// Picture already exists, replace detail with local
				DiveSitePicture existingDiveSitePicture = mDiveSite
						.getPicture(diveSitePictureIndex);
				existingDiveSitePicture.setBitmapFilePath(diveSitePicture
						.getBitmapFilePath());
				existingDiveSitePicture.setBitmapURL(diveSitePicture
						.getBitmapURL());
				existingDiveSitePicture.setPictureDescription(diveSitePicture
						.getPictureDescription());
			} else {
				mDiveSite.addPicture(diveSitePicture);
			}

			mPictureCursor.moveToNext();
		}

		// Now set pictures
		for (int i = 0; i < mDiveSite.getPicturesCount(); i++) {
			setDiveSitePicture(mDiveSite.getPicture(i));
		}
	}

	private void setDiveSitePicture(DiveSitePicture diveSitePicture) {
		DiveSitePicture existingPicture = null;
		if (diveSitePicture.getLocalId() == -1) {
			existingPicture = getPictureViewOnlineId(diveSitePicture
					.getOnlineId());
		} else {
			existingPicture = getPictureViewLocalId(diveSitePicture
					.getLocalId());
		}

		View diveSitePictureView = null;
		if (existingPicture == null) {
			// Create new view
			LayoutInflater layoutInflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			diveSitePictureView = layoutInflater.inflate(
					R.layout.divesite_picture_item, null);
			mDiveSitePictureViews.put(diveSitePicture, diveSitePictureView);
			mDiveSiteImageGallery.addView(diveSitePictureView);
		} else {
			diveSitePictureView = mDiveSitePictureViews.get(existingPicture);
			existingPicture.setBitmapFilePath(diveSitePicture
					.getBitmapFilePath());
			existingPicture.setBitmapURL(diveSitePicture.getBitmapURL());
			existingPicture.setPictureDescription(diveSitePicture
					.getPictureDescription());

			diveSitePicture = existingPicture;
		}

		ImageView imageView = (ImageView) diveSitePictureView
				.findViewById(R.id.divesite_picture);
		imageView.setTag(null);

		if (diveSitePicture.getBitmapFilePath() != null
				&& !diveSitePicture.getBitmapFilePath().isEmpty()
				&& new File(diveSitePicture.getBitmapFilePath()).exists()) {
			// Load image from file path
			imageView.setTag(diveSitePicture);

			LoadFileImageTask task = new LoadFileImageTask(imageView) {

				@Override
				protected void onPostExecute(Bitmap result) {
					super.onPostExecute(result);
					View diveSitePictureView = (View) getTag();

					View progress = diveSitePictureView
							.findViewById(R.id.divesite_picture_progress_bar);
					progress.setVisibility(View.GONE);

					getImageView().setVisibility(View.VISIBLE);
				}

			};
			task.setTag(diveSitePictureView);
			task.execute(diveSitePicture.getBitmapFilePath());

			imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (v.getTag() != null) {
						DiveSitePicture picture = (DiveSitePicture) v.getTag();

						LoadFileImageTask task = new LoadFileImageTask(
								mDiveSiteSelectedImage) {

							@Override
							protected void onPostExecute(Bitmap result) {
								super.onPostExecute(result);
								mDiveSiteSelectedImageProgress
										.setVisibility(View.INVISIBLE);
								mDiveSiteSelectedImage
										.setVisibility(View.VISIBLE);
							}

						};

						mDiveSiteSelectedImage.setVisibility(View.INVISIBLE);
						mDiveSiteSelectedImageProgress
								.setVisibility(View.VISIBLE);
						task.execute(picture.getBitmapFilePath());

						if (mDiveSite.getUserId() == mDiveSiteManager
								.getLoggedInDiverId()) {
							mDiveSiteDeletePicture.setVisibility(View.VISIBLE);
						}

						if (v.getTag() != null) {
							mDiveSiteDeletePicture.setTag(v.getTag());
						}
					}
				}
			});

		} else if (diveSitePicture.getBitmapURL() != null
				&& !diveSitePicture.getBitmapURL().isEmpty()) {
			// Load image from online URL
			imageView.setTag(diveSitePicture);

			LoadOnlineImageTask task = new LoadOnlineImageTask(imageView) {

				@Override
				protected void onPostExecute(Bitmap result) {
					super.onPostExecute(result);
					View diveSitePictureView = (View) getTag();

					View progress = diveSitePictureView
							.findViewById(R.id.divesite_picture_progress_bar);
					progress.setVisibility(View.GONE);

                    getImageView().setVisibility(View.VISIBLE);
				}
			};
			task.setTag(diveSitePictureView);
			task.execute(diveSitePicture.getBitmapURL());

			imageView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (v.getTag() != null) {
						DiveSitePicture picture = (DiveSitePicture) v.getTag();

						LoadOnlineImageTask task = new LoadOnlineImageTask(
								mDiveSiteSelectedImage) {

							@Override
							protected void onPostExecute(Bitmap result) {
								super.onPostExecute(result);
								mDiveSiteSelectedImageProgress
										.setVisibility(View.INVISIBLE);
								mDiveSiteSelectedImage
										.setVisibility(View.VISIBLE);
							}

						};

						mDiveSiteSelectedImage.setVisibility(View.INVISIBLE);
						mDiveSiteSelectedImageProgress
								.setVisibility(View.VISIBLE);
						task.execute(picture.getBitmapURL());

						if (mDiveSite.getUserId() == mDiveSiteManager
								.getLoggedInDiverId()) {
							mDiveSiteDeletePicture.setVisibility(View.VISIBLE);
						}

						if (v.getTag() != null) {
							mDiveSiteDeletePicture.setTag(v.getTag());
						}
					}
				}
			});
		} else {
			View progress = diveSitePictureView
					.findViewById(R.id.divesite_picture_progress_bar);
			progress.setVisibility(View.GONE);
		}
	}

	private DiveSitePicture getPictureViewOnlineId(long onlineId) {
		DiveSitePicture picture = null;
		Object[] diveSitePictures = mDiveSitePictureViews.keySet().toArray();
		for (int i = 0; i < diveSitePictures.length; i++) {
			if (((DiveSitePicture) diveSitePictures[i]).getOnlineId() == onlineId) {
				picture = (DiveSitePicture) diveSitePictures[i];
				break;
			}
		}
		return picture;
	}

	private DiveSitePicture getPictureViewLocalId(long localId) {
		DiveSitePicture picture = null;
		Object[] diveSitePictures = mDiveSitePictureViews.keySet().toArray();
		for (int i = 0; i < diveSitePictures.length; i++) {
			if (((DiveSitePicture) diveSitePictures[i]).getLocalId() == localId) {
				picture = (DiveSitePicture) diveSitePictures[i];
				break;
			}
		}
		return picture;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new PicturesListCursorLoader(getActivity(),
				mDiveSite.getLocalId());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mPictureCursor = (PictureCursor) cursor;

		updateLocalPictures();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Stop using the data
		mPictureCursor.close();
		mPictureCursor = null;
	}
}
