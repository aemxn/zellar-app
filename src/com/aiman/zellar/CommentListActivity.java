package com.aiman.zellar;

import java.io.IOException;
import java.io.InputStream;
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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.aiman.library.CommentListAdapter;
import com.aiman.library.Constants;
import com.aiman.library.DatabaseHandler;
import com.aiman.library.JSONParser;
import com.aiman.zellar.R;

public class CommentListActivity extends ListActivity {
	
	private Button submit;
	private EditText inputComment;
	private ProgressDialog pDialog, mDialog;
	String user_id;
	String item_id = null;
	String responseString;
	
	TextView txt_no_comments;
	
	InputStream inputStream;
	
	JSONParser jsonParser = new JSONParser();
	
	DatabaseHandler db;
	
	ArrayList<HashMap<String, String>> commentList;
	JSONArray comments = null;
	ListView lv;
	CommentListAdapter adapter;
	
	private static String view_comments = "view_comment";
	private static String submit_comment = "submit_comment";
	public static final String TAG_USERNAME = "user_name";
	public static final String TAG_COMMENT = "comment";
	private static final String TAG_COMMENTS = "comments";
	private static final String TAG_STATUS = "status";
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_list);
		
		db = new DatabaseHandler(getApplicationContext());
		
		commentList = new ArrayList<HashMap<String, String>>();
		
		Intent i = getIntent();
		item_id = i.getStringExtra("item_id");
		
		// locate
		inputComment = (EditText) findViewById(R.id.editComment);
		// locate Submit button in thirdview.xml layout
		submit = (Button) findViewById(R.id.btnSubmitComment);
		
		new LoadComments().execute();
		
		// Listen for button to be clickednavi
		submit.setOnClickListener(new OnClickListener() {
			// upon clicked do this
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				try {
					executeSubmission();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	public void executeSubmission() throws Exception {

		// Submit comment on AsyncTask
		class SubmitComment extends AsyncTask<String, String, String> {
			String comment;

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				comment = inputComment.getText().toString();
				user_id = db.getUserDetails().get("uid").toString();

				pDialog = new ProgressDialog(CommentListActivity.this);
				pDialog.setMessage("Submitting comment...");
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}

			/**
			 * Sending comment String to server using JSON
			 * */
			protected String doInBackground(String... args) {
				List<NameValuePair> params = new ArrayList<NameValuePair>();

				params.add(new BasicNameValuePair("comment", comment));
				params.add(new BasicNameValuePair("tag", submit_comment));
				params.add(new BasicNameValuePair("user_id", user_id));
				params.add(new BasicNameValuePair("item_id", item_id));
				
				//Log.d("doinbg param: ", params.toString());

				try {
					HttpClient httpclient = new DefaultHttpClient();
					/*
					 * HttpPost(parameter): Server URI
					 */
					HttpPost httppost = new HttpPost(Constants.URL_COMMENT);
					httppost.setEntity(new UrlEncodedFormEntity(params));
					HttpResponse response = httpclient.execute(httppost);
					responseString = convertResponseToString(response);
				} catch (Exception e) {
					e.printStackTrace();
				}

				return responseString;
			}

			/**
			 * After completing background task Dismiss the progress dialog
			 * **/
			protected void onPostExecute(String result) {
				super.onPostExecute(result);

				pDialog.dismiss();
				
				//Log.d("onpostexecute: ", result);
/*				Toast.makeText(ItemDetailActivity.this, "Comment Submitted! > " + result,
						Toast.LENGTH_LONG).show();*/

				new LoadComments().execute();
				inputComment.setText(null);
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

		SubmitComment exec = new SubmitComment();
		exec.execute();
	}
	
	class LoadComments extends AsyncTask<String, Void, JSONObject> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			txt_no_comments = (TextView) findViewById(R.id.txtNoComment);
			txt_no_comments.setText(null);
			
			mDialog = new ProgressDialog(CommentListActivity.this);
			mDialog.setMessage("Loading Comments...");
			mDialog.setIndeterminate(false);
			mDialog.setCancelable(false);
			mDialog.show();
		}

		protected JSONObject doInBackground(String... args) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			// TODO: http://androidadapternotifiydatasetchanged.blogspot.com/
			commentList.clear();

			params.add(new BasicNameValuePair("tag", view_comments));
			params.add(new BasicNameValuePair("item_id", item_id));
			//Log.d("doinbg-itemid: ", params.toString());

			JSONObject json_comment = jsonParser.getJSONFromUrl(Constants.URL_COMMENT,
					params);

			return json_comment;
		}

		@Override
		protected void onPostExecute(JSONObject json_comment) {
			// Log.d("onpostexecute > ", commentList.size() + "");
			try {
				int success = json_comment.getInt(TAG_STATUS);

				if (success == 1) {
					comments = json_comment.getJSONArray(TAG_COMMENTS);

					for (int i = 0; i < comments.length(); i++) {
						JSONObject c = comments.getJSONObject(i);

						String username = c.getString(TAG_USERNAME);
						String comment = c.getString(TAG_COMMENT);

						HashMap<String, String> map = new HashMap<String, String>();

						map.put(TAG_USERNAME, username);
						map.put(TAG_COMMENT, comment);

						// Log.d("Comments > ", map.toString());

						commentList.add(map);
					}

					lv = getListView();
					adapter = new CommentListAdapter(CommentListActivity.this,
							commentList);
					adapter.notifyDataSetChanged();
					lv.setAdapter(adapter);

					mDialog.dismiss();
				} else if (success == 0) {
					// Error in login
					mDialog.dismiss();
					txt_no_comments.setText("No Comments");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.comment_list, menu);
		return true;
	}

}
