package com.aiman.zellar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.aiman.library.AlertDialogManager;
import com.aiman.library.CommentListAdapter;
import com.aiman.library.ConnectionDetector;
import com.aiman.library.Constants;
import com.aiman.library.DatabaseHandler;
import com.aiman.library.JSONParser;
import com.aiman.zellar.R;
import com.squareup.picasso.Picasso;

public class ItemDetailActivity extends Activity {
	// Progress Dialog
	private ProgressDialog pDialog, nDialog;

	// Connection detector class
	ConnectionDetector cd;
	AlertDialogManager ad;

	// Creating JSON Parser object
	JSONParser jsonParser = new JSONParser();

	// tracks JSONArray
	JSONArray categories = null;

	// Album id
	String category_id = null;
	String item_id = null;

	String category_name, item_name, price, item_description, item_photo,
			user_name;

	// ALL JSON node names
	private static final String TAG_NAME = "name";
	private static final String TAG_PRICE = "price";
	private static final String TAG_CATEGORY = "category";
	private static final String TAG_ITEM_DESCRIPTION = "item_description";
	private static final String TAG_ITEM_PHOTO = "item_photo";
	private static final String TAG_USER_NAME = "user_name";

	public static final String TAG_USERNAME = "user_name";
	public static final String TAG_COMMENT = "comment";
	ArrayList<HashMap<String, String>> commentList;
	JSONArray comments = null;
	ListView lv;
	CommentListAdapter adapter;

	private Button submit;
	String responseString;
	InputStream inputStream;

	DatabaseHandler db;
	String user_id;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_detail);

		// creating connection detector class instance
		cd = new ConnectionDetector(getApplicationContext());
		ad = new AlertDialogManager();
		db = new DatabaseHandler(getApplicationContext());

		commentList = new ArrayList<HashMap<String, String>>();

		// Get album id, song id
		Intent i = getIntent();
		category_id = i.getStringExtra("category_id");
		item_id = i.getStringExtra("item_id");

		// Log.d("oncreate", item_id);

		// Loading tracks in Background Thread
		new NetCheck().execute();

		// locate Submit button in thirdview.xml layout
		submit = (Button) findViewById(R.id.btnComment);

		// Listen for button to be clickednavi
		submit.setOnClickListener(new OnClickListener() {
			// upon clicked do this
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					Intent i = new Intent(getApplicationContext(),
							CommentListActivity.class);
					// passing array index
					i.putExtra("item_id", item_id);
					startActivity(i);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	private class NetCheck extends AsyncTask<String, Void, Boolean> {

		protected void onPreExecute() {
			super.onPreExecute();
			nDialog = new ProgressDialog(ItemDetailActivity.this);
			nDialog.setMessage("Loading...");
			nDialog.setIndeterminate(false);
			nDialog.setCancelable(true);
			nDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... args) {
			// get Internet status
			return cd.isConnectingToInternet();
		}

		protected void onPostExecute(Boolean th) {
			if (th == true) {
				nDialog.dismiss();
				new LoadItemDetails().execute();
			} else {
				nDialog.dismiss();
				ad.showDialog(ItemDetailActivity.this,
						"No Internet Connection",
						"Please Connect to the Internet", false);
			}
		}
	}

	/**
	 * Background Async Task to get single song information
	 * */
	class LoadItemDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(ItemDetailActivity.this);
			pDialog.setMessage("Loading item details ...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting song json and parsing
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			// post album id, song id as GET parameters
			params.add(new BasicNameValuePair("category", category_id));
			params.add(new BasicNameValuePair("item", item_id));

			// Log.d("params for doinbg loaditem: ", params.toString());

			// getting JSON string from URL
			String json_load = jsonParser.makeHttpRequest(Constants.URL_DETAIL,
					"GET", params);

			// Check your log cat for JSON reponse
			// Log.d("Item details JSON: ", json_load);

			return json_load;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String json) {
			try {
				JSONObject jObj = new JSONObject(json);
				if (jObj != null) {
					item_name = jObj.getString(TAG_NAME);
					category_name = jObj.getString(TAG_CATEGORY);
					price = jObj.getString(TAG_PRICE);
					item_description = jObj.getString(TAG_ITEM_DESCRIPTION);
					item_photo = jObj.getString(TAG_ITEM_PHOTO);
					user_name = jObj.getString(TAG_USER_NAME);
				}

				TextView txt_item_name = (TextView) findViewById(R.id.item_name);
				TextView txt_category_name = (TextView) findViewById(R.id.category_name);
				TextView txt_price = (TextView) findViewById(R.id.price);
				TextView txt_item_description = (TextView) findViewById(R.id.item_description);
				TextView txt_user_name = (TextView) findViewById(R.id.user_name);

				ImageView img_item_photo = (ImageView) findViewById(R.id.item_photo);

				// displaying song data in view
				txt_item_name.setText(item_name);
				txt_category_name.setText(Html.fromHtml("<b>Category:</b> "
						+ category_name));
				txt_price.setText(Html.fromHtml("<b>Price:</b> RM " + price));
				txt_item_description.setText(Html
						.fromHtml("<b>Item Description:</b> "
								+ item_description));
				/*
				 * img_item_photo.setText(Html.fromHtml("<b>Item Photo:</b> " +
				 * item_photo));
				 */
				txt_user_name.setText(Html.fromHtml("<b>Uploaded by:</b> "
						+ user_name));

				Picasso.with(ItemDetailActivity.this) //
						.load(Constants.ROOT_SERVER_URL + item_photo) // http://localhost/webshopper/users/aiman/items/aiman_item_1.jpg
						.placeholder(R.drawable.placeholder) //
						.error(R.drawable.error) //
						.fit().centerCrop()//
						.into(img_item_photo);
				
				pDialog.dismiss();
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}
}
