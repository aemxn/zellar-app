/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package com.aiman.zellar;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aiman.library.AlertDialogManager;
import com.aiman.library.ConnectionDetector;
import com.aiman.library.DatabaseHandler;
import com.aiman.library.UserFunctions;

public class LoginActivity extends Activity {
	// flag for Internet connection status
	Boolean isInternetPresent = false;
	
	private ProgressDialog nDialog, pDialog;

	// Connection detector class
	ConnectionDetector cd;
	AlertDialogManager ad;

	Button btnLogin;
	Button btnLinkToRegister;
	EditText inputEmail;
	EditText inputPassword;
	TextView loginErrorMsg;

	// JSON Response node names
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static String KEY_UID = "uid";
	private static String KEY_NAME = "name";
	private static String KEY_FULLNAME = "full_name";
	private static String KEY_EMAIL = "email";
	private static String KEY_CREATED_AT = "created_at";

	// private static String KEY_PROF_PHOTO_URI = "profile_photo_uri";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Importing all assets like buttons, text fields
		inputEmail = (EditText) findViewById(R.id.loginEmail);
		inputPassword = (EditText) findViewById(R.id.loginPassword);
		btnLogin = (Button) findViewById(R.id.btnLogin);
		btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
		loginErrorMsg = (TextView) findViewById(R.id.login_error);

		// creating connection detector class instance
		cd = new ConnectionDetector(getApplicationContext());
		ad = new AlertDialogManager();

		// Login button Click Event
		btnLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				if ((!inputEmail.getText().toString().equals(""))
						&& (!inputPassword.getText().toString().equals(""))) {
					NetAsync(view);
				} else if ((!inputEmail.getText().toString().equals(""))) {
					Toast.makeText(getApplicationContext(),
							"Password field empty", Toast.LENGTH_SHORT).show();
				} else if ((!inputPassword.getText().toString().equals(""))) {
					Toast.makeText(getApplicationContext(),
							"Email field empty", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Email and Password field are empty",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// Link to Register Screen
		btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						RegisterActivity.class);
				startActivity(i);
				finish();
			}
		});

	}

	private class NetCheck extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... args) {
			// get Internet status
			return cd.isConnectingToInternet();
		}

		protected void onPostExecute(Boolean th) {
			if (th == true) {
				new ProcessLogin().execute();
			} else {
				ad.showDialog(LoginActivity.this, "No Internet Connection",
						"Please Connect to the Internet", false);
			}
		}
	}

	private class ProcessLogin extends AsyncTask<String, Void, JSONObject> {
		String email;
		String password;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			// Importing all assets like buttons, text fields
			inputEmail = (EditText) findViewById(R.id.loginEmail);
			inputPassword = (EditText) findViewById(R.id.loginPassword);
			email = inputEmail.getText().toString();
			password = inputPassword.getText().toString();
			pDialog = new ProgressDialog(LoginActivity.this);
			pDialog.setMessage("Logging in...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... args) {

			UserFunctions userFunction = new UserFunctions();
			// Log.d("Button", "Login");
			JSONObject json = userFunction.loginUser(email, password);
			return json;
		}

		protected void onPostExecute(JSONObject json) {
			try {
				if (json.getString(KEY_SUCCESS) != null) {

					String res = json.getString(KEY_SUCCESS);

					if (Integer.parseInt(res) == 1) {
						// user successfully logged in
						// Store user details in SQLite Database
						DatabaseHandler db = new DatabaseHandler(
								getApplicationContext());
						JSONObject json_user = json.getJSONObject("user");

						// Clear all previous data in database
						UserFunctions logout = new UserFunctions();
						logout.logoutUser(getApplicationContext());
						// TODO exclude a parameter maybe?
						db.addUser(json_user.getString(KEY_NAME),
								json_user.getString(KEY_FULLNAME),
								json_user.getString(KEY_EMAIL),
								json.getString(KEY_UID),
								json_user.getString(KEY_CREATED_AT)
						// ,json_user.getString(KEY_PROF_PHOTO_URI)
						);

						String u_email = json_user.getString(KEY_EMAIL);

						// Launch Dashboard Screen
						Intent dashboard = new Intent(getApplicationContext(),
								CategoryActivity.class);

						// Close all views before launching Dashboard
						dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						dashboard.putExtra("id", u_email);
						pDialog.dismiss();
						startActivity(dashboard);

						// Close Login Screen
						finish();
					} else {
						// Error in login
						pDialog.dismiss();
						loginErrorMsg.setText("Incorrect username/password");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void NetAsync(View view) {
		new NetCheck().execute();
	}

}
