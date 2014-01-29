package com.aiman.zellar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.aiman.library.AlertDialogManager;
import com.aiman.library.CategoryListAdapter;
import com.aiman.library.ConnectionDetector;
import com.aiman.library.Constants;
import com.aiman.library.ItemListAdapter;
import com.aiman.library.JSONParser;
import com.aiman.zellar.R;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class ItemListActivity extends ListActivity {

	// Progress Dialog
	private ProgressDialog pDialog;

	// Connection detector class
	ConnectionDetector cd;
	AlertDialogManager ad;

	ItemListAdapter adapter;

	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();

	ListView lv;
	
	TextView txt_no_items;

	ArrayList<HashMap<String, String>> itemsList;

	// tracks JSONArray
	JSONArray categories = null;

	// Album id
	public static String category_id;

	String category_name;

	String item_photo;

	// ALL JSON node names. See database table
	public static final String TAG_CATEGORY_ID = "category_id";
	public static final String TAG_ITEMS = "items";
	public static final String TAG_ID = "id";
	public static final String TAG_NAME = "name";
	public static final String TAG_CATEGORY = "category";
	public static final String TAG_PRICE = "price";
	public static final String TAG_TRACK_NO = "track_no";
	public static final String TAG_THUMB_ITEM = "item_photo";
	
	private BaseAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);

		// creating connection detector class instance
		cd = new ConnectionDetector(getApplicationContext());
		ad = new AlertDialogManager();
		
		setTitle("Item List");

		// Get album id
		Intent i = getIntent();
		category_id = i.getStringExtra("category_id");

		// Hashmap for ListView
		itemsList = new ArrayList<HashMap<String, String>>();

		// Loading tracks in Background Thread
		new NetCheck().execute();

	}

	private class NetCheck extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... args) {
			// get Internet status
			return cd.isConnectingToInternet();
		}

		protected void onPostExecute(Boolean th) {
			if (th == true) {
				new LoadItems().execute();
			} else {
				ad.showDialog(ItemListActivity.this, "No Internet Connection",
						"Please Connect to the Internet", false);
			}
		}
	}

	/**
	 * Background Async Task to Load all tracks under one album
	 * */
	private class LoadItems extends AsyncTask<Void, Void, Void> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			txt_no_items = (TextView) findViewById(R.id.txt_no_items);
			txt_no_items.setText(null);
			
			pDialog = new ProgressDialog(ItemListActivity.this);
			pDialog.setMessage("Loading Items...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting tracks json and parsing
		 * @return 
		 * */
		protected Void doInBackground(Void... params) {
			// Building Parameters
			List<NameValuePair> parameter = new ArrayList<NameValuePair>();

			// post album id as GET parameter
			parameter.add(new BasicNameValuePair(TAG_ID, category_id));

			// getting JSON string from URL
			String json = jsonParser.makeHttpRequest(Constants.URL_ITEM_LIST, "GET",
					parameter);

			// Check your log cat for JSON reponse
			Log.d("Item List JSON: ", json);

			try {
				JSONObject jObj = new JSONObject(json);
				if (jObj != null) {
					String category_id = jObj.getString(TAG_ID);
					category_name = jObj.getString(TAG_CATEGORY);
					categories = jObj.getJSONArray(TAG_ITEMS);

					if (categories != null) {
						// looping through All songs
						for (int i = 0; i < categories.length(); i++) {
							JSONObject c = categories.getJSONObject(i);

							// Storing each json item in variable
							String item_id = c.getString(TAG_ID);
							// track no - increment i value
							String track_no = String.valueOf(i + 1);
							String name = c.getString(TAG_NAME);
							String price = c.getString(TAG_PRICE);
							String thumb_item = c.getString(TAG_THUMB_ITEM);

							// creating new HashMap
							HashMap<String, String> map = new HashMap<String, String>();

							// adding each child node to HashMap key => value
							map.put(TAG_CATEGORY_ID, category_id);
							map.put(TAG_ID, item_id);
							map.put(TAG_TRACK_NO, track_no + ".");
							map.put(TAG_NAME, name);
							map.put(TAG_PRICE, price);
							map.put(TAG_THUMB_ITEM, thumb_item);

							// adding HashList to ArrayList
							itemsList.add(map);
						}
					} else {
						//Log.d("Categories: ", "null");
						
						pDialog.dismiss();
						txt_no_items.setText("No Items Liao");
					}
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void args) {
/*			lv = getListView();
			// Pass the results into ListViewAdapter.java
			adapter = new ItemListAdapter(ItemListActivity.this, itemsList);
			
			// Set the adapter to the ListView
			lv.setAdapter(adapter);*/
			
			mAdapter = new ItemListAdapter(ItemListActivity.this, itemsList);
			AnimationAdapter animAdapter = new AlphaInAnimationAdapter(mAdapter);
            animAdapter.setAbsListView(getListView());
            getListView().setAdapter(animAdapter);
			
			// Close the progressdialog
			pDialog.dismiss();
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