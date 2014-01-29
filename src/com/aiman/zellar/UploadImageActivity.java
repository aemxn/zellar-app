//http://sptechnolab.files.wordpress.com/2012/06/anroid.png

package com.aiman.zellar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.aiman.library.Base64;
import com.aiman.library.CategorySpinner;
import com.aiman.library.Constants;
import com.aiman.library.DatabaseHandler;
import com.aiman.library.JSONParser;

public class UploadImageActivity extends Activity implements
		OnItemSelectedListener {

	// onPreExecute dialog
	ProgressDialog pDialog;

	// database handling
	DatabaseHandler db;

	// Layout listener
	InputStream inputStream;
	private ImageView imageView;
	private EditText inputItemName;
	private EditText inputItemPrice;
	private EditText inputDescription;

	// general vars
	String the_string_response;
	String userDetails;

	// keep track of cropping intent
	private Uri mImageCaptureUri;
	Bitmap photo;

	// tag pairing with server side scripts
	ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

	// idk wtf is this
	private static final int CAMERA_REQUEST = 1;
	private static final int PIC_CROP = 2;
	private static final int SELECT_PICTURE = 3;
	private static final String TEMP_FILE = "temp.jpg";

	private Spinner spinner;
	List<String> lables;
	private ArrayList<CategorySpinner> categoriesList;
	JSONArray categories = null;

	JSONParser jsonParser = new JSONParser();

	public static final String TAG_ID = "id";
	public static final String TAG_NAME = "category";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_image);

		db = new DatabaseHandler(getApplicationContext());
		userDetails = db.getUserDetails().get("name").toString();
		nameValuePairs.add(new BasicNameValuePair("user_name", userDetails));

		imageView = (ImageView) findViewById(R.id.imgUpload);
		inputItemName = (EditText) findViewById(R.id.txtInputItemName);
		inputItemPrice = (EditText) findViewById(R.id.txtInputPrice);
		inputDescription = (EditText) findViewById(R.id.txtInputDescription);

		// Spinner element
		spinner = (Spinner) findViewById(R.id.spinnerItem);

		// Spinner click listener
		spinner.setOnItemSelectedListener(this);

		categoriesList = new ArrayList<CategorySpinner>();

		// Spinner Drop down elements
		/* categories = new JSONArray(); */
		/*
		 * categories.add("Leather"); categories.add("Plastics");
		 * categories.add("Collectibles"); categories.add("Electronics");
		 * categories.add("Foods"); categories.add("Papers");
		 * categories.add("Furniture"); categories.add("Kitchen");
		 * categories.add("Misc");
		 */

		new GetCategories().execute();
	}

	private class GetCategories extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			lables = new ArrayList<String>();
		}

		@Override
		protected String doInBackground(String... args) {

			JSONParser jsonParser = new JSONParser();
			String json = jsonParser.makeHttpRequest(Constants.URL_CATEGORY,
					"GET");

			// Check your log cat for JSON reponse
			Log.d("Categories JSON: ", "> " + json);

			return json;
		}

		@Override
		protected void onPostExecute(String json) {
			try {
				categories = new JSONArray(json);
				if (categories != null) {
					for (int i = 0; i < categories.length(); i++) {
						JSONObject catObj = (JSONObject) categories.get(i);
						CategorySpinner cat = new CategorySpinner(
								catObj.getInt("id"),
								catObj.getString("category"));
						categoriesList.add(cat);
					}

					for (int i = 0; i < categoriesList.size(); i++) {
						lables.add(categoriesList.get(i).getName());
					}

					// Creating adapter for spinner
					ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
							UploadImageActivity.this,
							android.R.layout.simple_spinner_item, lables);

					// Drop down layout style - list view with radio button
					dataAdapter
							.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

					// attaching data adapter to spinner
					spinner.setAdapter(dataAdapter);
				} else {
					Log.e("JSON Data", "Didn't receive any data from server!");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// On selecting a spinner item
		String category = parent.getItemAtPosition(position).toString();
		
		//Log.d("Category selected: ", "> " + category);

		nameValuePairs.add(new BasicNameValuePair("category_name", category));
	}

	public void capturePhoto(View view) {
		try {
			Intent cameraIntent = new Intent(
					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			mImageCaptureUri = getTempUri();
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
			cameraIntent.putExtra("return-data", true);
			startActivityForResult(cameraIntent, CAMERA_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void pickPhoto(View view) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(
				Intent.createChooser(intent, "Select From Gallery"),
				SELECT_PICTURE);
	}

	private Uri getTempUri() {
		return Uri.fromFile(getTempFile());
	}

	private File getTempFile() {
		if (isSDCARDMounted()) {

			File f = new File(Environment.getExternalStorageDirectory(),
					TEMP_FILE);
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(this, "no card mounted", Toast.LENGTH_LONG)
						.show();
			}
			return f;
		} else {
			return null;
		}
	}

	private boolean isSDCARDMounted() {
		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED))
			return true;
		return false;
	}

	public class CropOptionAdapter extends ArrayAdapter<CropOption> {
		private ArrayList<CropOption> mOptions;
		private LayoutInflater mInflater;

		public CropOptionAdapter(Context context, ArrayList<CropOption> options) {
			super(context, R.layout.crop_selector, options);
			mOptions = options;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup group) {
			if (convertView == null)
				convertView = mInflater.inflate(R.layout.crop_selector, null);

			CropOption item = mOptions.get(position);

			if (item != null) {
				((ImageView) convertView.findViewById(R.id.iv_icon))
						.setImageDrawable(item.icon);
				((TextView) convertView.findViewById(R.id.tv_name))
						.setText(item.title);

				return convertView;
			}

			return null;
		}
	}

	public class CropOption {
		public CharSequence title;
		public Drawable icon;
		public Intent appIntent;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case CAMERA_REQUEST:
			performCrop();
			break;
		case SELECT_PICTURE:
			mImageCaptureUri = data.getData();
			performCrop();
			break;
		case PIC_CROP:
			Bundle extras = data.getExtras();
			/**
			 * After cropping the image, get the bitmap of the cropped image and
			 * display it on imageview.
			 */
			if (extras != null) {
				photo = extras.getParcelable("data");

				imageView.setImageBitmap(photo);
			}
			try {
				// create a folder named Zellar in client's sdcard
				String path = android.os.Environment
						.getExternalStorageDirectory()
						+ File.separator
						+ "Zellar";// + File.separator + "default";

				File filepath = new File(path);
				filepath.mkdirs();

				OutputStream outFile = null;
				// stores captured photo in Zellar folder
				File file = new File(path, String.valueOf(System
						.currentTimeMillis()) + ".jpg");

				try {
					outFile = new FileOutputStream(file);
					photo.compress(Bitmap.CompressFormat.JPEG, 100, outFile);
					outFile.flush();
					outFile.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			File temp = new File(mImageCaptureUri.getPath());
			/**
			 * Delete the temporary image
			 */
			if (temp.exists())
				temp.delete();

			break;
		}
	}

	/**
	 * Helper method to carry out crop operation
	 */
	private void performCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		/**
		 * Open image crop app by starting an intent
		 * ‘com.android.camera.action.CROP‘.
		 */
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		/**
		 * Check if there is image cropper app installed.
		 */
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();

		/**
		 * If there is no image cropper app, display warning message
		 */
		if (size == 0) {

			Toast.makeText(this, "Can not find image crop app",
					Toast.LENGTH_SHORT).show();

			return;
		} else {
			/**
			 * Specify the image path, crop dimension and scale
			 */
			intent.setData(mImageCaptureUri);

			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("scaleUpIfNeeded", true);
			intent.putExtra("return-data", true);

			/**
			 * There is posibility when more than one image cropper app exist,
			 * so we have to check for it first. If there is only one app, open
			 * then app.
			 */
			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, PIC_CROP);
			} else {
				/**
				 * If there are several app exist, create a custom chooser to
				 * let user selects the app.
				 */
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(
										cropOptions.get(item).appIntent,
										PIC_CROP);
							}
						});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {
							getContentResolver().delete(mImageCaptureUri, null,
									null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}

	/**
	 * Photo uploader with Base64 encoder
	 * 
	 * @param view
	 *            Button listener from activity_upload_image.xml
	 */

	public void uploadPhoto(View view) {
		try {
			executeMultipartPost();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * HTTP Upload in AsyncTask for the fucking win
	 * 
	 * @throws Exception
	 */
	public void executeMultipartPost() throws Exception {

		class execMultiPostAsync extends AsyncTask<String, Void, String> {
			String strItemName;
			String strDescription;
			String strPrice;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				strItemName = inputItemName.getText().toString();
				strDescription = inputDescription.getText().toString();
				strPrice = inputItemPrice.getText().toString();

				pDialog = new ProgressDialog(UploadImageActivity.this);
				// pDialog.setTitle("Contacting Servers");
				pDialog.setMessage("Submitting item...");
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}

			@Override
			protected String doInBackground(String... params) {

				// Choose image here
				BitmapDrawable drawable = (BitmapDrawable) imageView
						.getDrawable();
				Bitmap bitmap = drawable.getBitmap();
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// compress to which format you want.
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] byte_arr = stream.toByteArray();
				String image_str = Base64.encodeBytes(byte_arr);

				nameValuePairs.add(new BasicNameValuePair("image", image_str));
				nameValuePairs.add(new BasicNameValuePair("item_name",
						strItemName));
				nameValuePairs.add(new BasicNameValuePair("item_desc",
						strDescription));
				nameValuePairs.add(new BasicNameValuePair("item_price",
						strPrice));

				try {
					HttpClient httpclient = new DefaultHttpClient();
					/*
					 * HttpPost(parameter): Server URI
					 */

					HttpPost httppost = new HttpPost(
							Constants.SERVER_UPLOAD_URI);
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					the_string_response = convertResponseToString(response);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return the_string_response;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);

				pDialog.dismiss();

				Toast.makeText(UploadImageActivity.this, "Item Submitted!",
						Toast.LENGTH_LONG).show();

				// TODO: Uploaded image goes to Item List Activity.
				// Tarik category_id dari server
				Intent userProfile = new Intent(getApplicationContext(),
						UserProfileActivity.class);
				userProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(userProfile);
			}

			public String convertResponseToString(HttpResponse response)
					throws IllegalStateException, IOException {

				String res = "";
				StringBuffer buffer = new StringBuffer();
				inputStream = response.getEntity().getContent();
				// getting content length
				int contentLength = (int) response.getEntity()
						.getContentLength();

				// Log.d("Rain", "heavy as fuck");
				if (contentLength < 0) {
				} else {
					byte[] data = new byte[512];
					int len = 0;
					try {
						while (-1 != (len = inputStream.read(data))) {
							// converting to string and appending to
							// stringbuffer
							buffer.append(new String(data, 0, len));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						inputStream.close(); // closing the stream
					} catch (IOException e) {
						e.printStackTrace();
					}
					res = buffer.toString(); // converting stringbuffer to
												// string
				}
				return res;
			}
		}

		execMultiPostAsync exec = new execMultiPostAsync();
		exec.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.upload_image, menu);
		return true;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}