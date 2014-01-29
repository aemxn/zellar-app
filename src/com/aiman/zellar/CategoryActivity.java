package com.aiman.zellar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aiman.library.AlertDialogManager;
import com.aiman.library.CategoryListAdapter;
import com.aiman.library.ConnectionDetector;
import com.aiman.library.Constants;
import com.aiman.library.DatabaseHandler;
import com.aiman.library.JSONParser;
import com.aiman.library.UserFunctions;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class CategoryActivity extends ListActivity {
	UserFunctions userFunctions;
	String userDetails;

	// Progress Dialog
	private ProgressDialog pDialog;

	// Connection detector class
	ConnectionDetector cd;
	AlertDialogManager ad;
	DatabaseHandler db;

	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();

	ArrayList<HashMap<String, String>> categoryList;

	// albums JSONArray
	JSONArray categories = null;

	// ALL JSON node names
	public static final String TAG_ID = "id";
	public static final String TAG_NAME = "category";
	public static final String TAG_CATEGORIES_COUNT = "categories_count";
	public static final String TAG_CATEGORIES_LOGO = "categories_logo";
	
	//private ListView lv;
	private BaseAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// creating connection detector class instance
		cd = new ConnectionDetector(getApplicationContext());
		ad = new AlertDialogManager();
		userFunctions = new UserFunctions();
		db = new DatabaseHandler(getApplicationContext());

		if (userFunctions.isUserLoggedIn(getApplicationContext())) {
			setContentView(R.layout.activity_category);
			

			// Check toast which user is logged in
			userDetails = db.getUserDetails().get("name").toString();

			Log.d("Logged in Name: ", userDetails.toString());
			
			setTitle(userDetails);

			// Hashmap for ListView
			categoryList = new ArrayList<HashMap<String, String>>();

			// Loading Albums JSON in Background Thread
			new NetCheck().execute();

			// get listview
			ListView lv = getListView();
			lv.setDivider(null);

			lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View view,
						int arg2, long arg3) {
					Intent i = new Intent(getApplicationContext(),
							ItemListActivity.class);

					String category_id = ((TextView) view
							.findViewById(R.id.category_id)).getText()
							.toString();
					i.putExtra("category_id", category_id);

					startActivity(i);
				}
			});
		} else {
			// user is not logged in show login screen
			Intent login = new Intent(getApplicationContext(),
					LoginActivity.class);
			login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(login);
			// Closing dashboard screen
			finish();
		}
	}

	private class NetCheck extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... args) {
			// get Internet status
			return cd.isConnectingToInternet();
		}

		protected void onPostExecute(Boolean th) {
			if (th == true) {
				new LoadCategories().execute();
			} else {
				Toast.makeText(CategoryActivity.this, "Unable to connect to server",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	class LoadCategories extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(CategoryActivity.this);
			pDialog.setMessage("Listing Categories...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			// getting JSON string from URL
			String json = jsonParser.makeHttpRequest(Constants.URL_CATEGORY, "GET",
					params);

			// Check your log cat for JSON reponse
			Log.d("Categories JSON: ", "> " + json);
			
			return json;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		@Override
		protected void onPostExecute(String json) {
			try {
				categories = new JSONArray(json);

				if (categories != null) {
					// looping through All albums
					for (int i = 0; i < categories.length(); i++) {
						JSONObject c = categories.getJSONObject(i);

						// Storing each json item values in variable
						String id = c.getString(TAG_ID);
						String name = c.getString(TAG_NAME);
						String songs_count = c.getString(TAG_CATEGORIES_COUNT);
						String category_logo = c.getString(TAG_CATEGORIES_LOGO);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_ID, id);
						map.put(TAG_NAME, name);
						map.put(TAG_CATEGORIES_COUNT, songs_count);
						map.put(TAG_CATEGORIES_LOGO, category_logo);

						// adding HashList to ArrayList
						categoryList.add(map);
					}
					
					mAdapter = new CategoryListAdapter(CategoryActivity.this, categoryList);
					AnimationAdapter animAdapter = new AlphaInAnimationAdapter(mAdapter);
		            animAdapter.setAbsListView(getListView());
		            getListView().setAdapter(animAdapter);
					
					// dismiss the dialog after getting all albums
					pDialog.dismiss();
				} else {
					Log.d("Categories: ", "null");
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_userprofile, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_camera:
			Intent upload = new Intent(getApplicationContext(),
					UploadImageActivity.class);
			upload.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(upload);
			return true;
		case R.id.action_category:
			Intent userProfile = new Intent(getApplicationContext(),
					UserProfileActivity.class);
			userProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(userProfile);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
