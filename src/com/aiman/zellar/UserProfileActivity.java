package com.aiman.zellar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.aiman.library.Constants;
import com.aiman.library.DatabaseHandler;
import com.aiman.library.GPSTracker;
import com.aiman.library.ImageAdapter;
import com.aiman.library.JSONParser;
import com.aiman.library.UserFunctions;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends Activity {
	// AlertDialogManager ad;
	DatabaseHandler db;
	String user_name, uid;
	TextView txtUserDetails;
	TextView txtUserLocation;
	ImageView imgUserProfile;
	GridView mGridView;

	// GPSTracker class
	GPSTracker gps;
	StringBuilder sb = new StringBuilder();
	UserFunctions userFunctions;

	JSONArray grid = null;

	JSONParser jsonParser = new JSONParser();

	ArrayList<HashMap<String, String>> grid_result;

	private static String TAG_STATUS = "status";
	private static String TAG_GRID = "items";
	private static String TAG_ITEM_NAME = "item_name";
	public static String TAG_ITEM_PHOTO = "item_photo";
	private static String grid_tag = "item_grid";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		


		// get intent data
		Intent i = getIntent();
		userFunctions = new UserFunctions();
		db = new DatabaseHandler(getApplicationContext());
		// ad = new AlertDialogManager();

		// Check toast which user is logged in
		user_name = db.getUserDetails().get("name").toString();
		uid = db.getUserDetails().get("uid").toString();
		// TODO create a DBHandler for this ffs
		// DIR_IMG_PROFILE =
		// db.getUserDetails().get("profile_photo_uri").toString();

		grid_result = new ArrayList<HashMap<String, String>>();

		mGridView = (GridView) findViewById(R.id.grid_view);

		txtUserDetails = (TextView) findViewById(R.id.txtUsername);
		txtUserDetails.setText(user_name);

		imgUserProfile = (ImageView) findViewById(R.id.imgUserProfile);

		// Selected image id
		Picasso.with(this) //
				.load(Constants.ROOT_SERVER_URL + Constants.DIR_IMG_UNKNOWN) //
				.placeholder(R.drawable.placeholder) //
				.error(R.drawable.error) //
				.fit().centerCrop()//
				.into(imgUserProfile);

		// create class object
		gps = new GPSTracker(UserProfileActivity.this);

		// check if GPS enabled
		if (gps.canGetLocation()) {

			double latitude = gps.getLatitude();
			double longitude = gps.getLongitude();

			Geocoder gc = new Geocoder(this, Locale.getDefault());
			try {
				List<Address> addresses = gc.getFromLocation(latitude,
						longitude, 1);

				if (addresses.size() > 0) {
					Address address = addresses.get(0);

					for (int j = 0; j < address.getMaxAddressLineIndex(); j++) {
						// sb.append(address.getAddressLine(j)).append("\n");
						sb.append(address.getLocality()).append("\n");
						// sb.append(address.getPostalCode()).append("\n");
						// sb.append(address.getCountryName());
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			txtUserLocation = (TextView) findViewById(R.id.txtLocation);
			txtUserLocation.setText(sb.toString());

		} else {
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings
			gps.showSettingsAlert();
		}
		
		

		new LoadUserGrid().execute();
	}

	private class LoadUserGrid extends AsyncTask<Void, Void, JSONObject> {
		@Override
		protected JSONObject doInBackground(Void... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("tag", grid_tag));
			params.add(new BasicNameValuePair("username", user_name));
			params.add(new BasicNameValuePair("uid", uid));

			JSONObject json_grid = jsonParser.getJSONFromUrl(Constants.USER_ITEMS_URL,
					params);

			return json_grid;
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);

			try {
				int success = result.getInt(TAG_STATUS);

				if (success == 1) {
					grid = result.getJSONArray(TAG_GRID);

					for (int i = 0; i < grid.length(); i++) {
						JSONObject c = grid.getJSONObject(i);

						String item_name = c.getString(TAG_ITEM_NAME);
						String item_photo = c.getString(TAG_ITEM_PHOTO);

						HashMap<String, String> map = new HashMap<String, String>();

						map.put(TAG_ITEM_NAME, item_name);
						map.put(TAG_ITEM_PHOTO, item_photo);

						grid_result.add(map);
					}

					ImageAdapter adapter = new ImageAdapter(
							UserProfileActivity.this, grid_result);
					mGridView.setAdapter(adapter);
				} else if (success == 0) {
					// Error in login
					// txt_no_comments.setText("No Comments");
				}
				
				// TODO: Send item_id and category_id
		        mGridView.setOnItemClickListener(new OnItemClickListener() {
		            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		                // Sending image id to FullScreenActivity
		                Intent i = new Intent(getApplicationContext(), CategoryActivity.class);
		                startActivity(i);
		            }
		        });
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_common, menu);
		getMenuInflater().inflate(R.menu.user_profile, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_logout:
			userFunctions.logoutUser(getApplicationContext());
			Intent login = new Intent(getApplicationContext(),
					LoginActivity.class);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(login);
			// Closing dashboard screen
			finish();
			return true;
		case R.id.action_settings:
			Intent user_settings = new Intent(getApplicationContext(),
					UserSettingsActivity.class);
			user_settings.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(user_settings);
			return true;
		case R.id.action_camera:
			Intent upload = new Intent(getApplicationContext(),
					UploadImageActivity.class);
			upload.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(upload);
			return true;
		case R.id.action_category:
			Intent categoryActivity = new Intent(getApplicationContext(),
					CategoryActivity.class);
			categoryActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(categoryActivity);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
