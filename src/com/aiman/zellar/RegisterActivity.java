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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.aiman.library.AlertDialogManager;
import com.aiman.library.ConnectionDetector;
import com.aiman.library.DatabaseHandler;
import com.aiman.library.UserFunctions;
import com.aiman.zellar.R;

public class RegisterActivity extends Activity {
	// flag for Internet connection status
	Boolean isInternetPresent = false;

	private ProgressDialog nDialog, pDialog;

	Button btnRegister;
	Button btnLinkToLogin;
	EditText inputUserName;
	EditText inputFullName;
	EditText inputEmail;
	EditText inputPassword;
	TextView registerErrorMsg;

	// Connection detector class
	ConnectionDetector cd;
	AlertDialogManager ad;

	// JSON Response node names
	private static String KEY_SUCCESS = "success";
	private static String KEY_ERROR = "error";
	private static String KEY_ERROR_MSG = "error_msg";
	private static String KEY_UID = "uid";
	private static String KEY_FULLNAME = "full_name";
	private static String KEY_NAME = "name";
	private static String KEY_EMAIL = "email";
	private static String KEY_CREATED_AT = "created_at";

	// private static String KEY_PROF_PHOTO_URI = "profile_photo_uri";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		// Importing all assets like buttons, text fields
		inputUserName = (EditText) findViewById(R.id.registerName);
		inputFullName = (EditText) findViewById(R.id.registerFullName);
		inputEmail = (EditText) findViewById(R.id.registerEmail);
		inputPassword = (EditText) findViewById(R.id.registerPassword);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
		registerErrorMsg = (TextView) findViewById(R.id.register_error);

		// creating connection detector class instance
		cd = new ConnectionDetector(getApplicationContext());
		ad = new AlertDialogManager();

		// Register Button Click event
		btnRegister.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if ((!inputUserName.getText().toString().equals(""))
						&& (!inputFullName.getText().toString().equals(""))
						&& (!inputPassword.getText().toString().equals(""))
						&& (!inputEmail.getText().toString().equals(""))) {
					if (inputUserName.getText().toString().length() > 4) {
						NetAsync(view);

					} else {
						Toast.makeText(getApplicationContext(),
								"Username should be minimum 5 characters",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"One or more fields are empty", Toast.LENGTH_SHORT)
							.show();
				}
			}
		});

		// Link to Login Screen
		btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(),
						LoginActivity.class);
				startActivity(i);
				// Close Registration View
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
				new ProcessRegister().execute();
			} else {
				ad.showDialog(RegisterActivity.this, "No Internet Connection",
						"Please Connect to the Internet", false);
			}
		}
	}

	private class ProcessRegister extends AsyncTask<String, Void, JSONObject> {
		String email, password, username, fullname;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			inputUserName = (EditText) findViewById(R.id.registerName);
			inputFullName = (EditText) findViewById(R.id.registerFullName);
			inputEmail = (EditText) findViewById(R.id.registerEmail);
			inputPassword = (EditText) findViewById(R.id.registerPassword);

			username = inputUserName.getText().toString();
			fullname = inputFullName.getText().toString();
			email = inputEmail.getText().toString();
			password = inputPassword.getText().toString();

			pDialog = new ProgressDialog(RegisterActivity.this);
			pDialog.setMessage("Registering...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected JSONObject doInBackground(String... args) {
			UserFunctions userFunction = new UserFunctions();
			JSONObject json = userFunction.registerUser(username, fullname,
					email, password);

			return json;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			// check for login response
			try {

				if (json.getString(KEY_SUCCESS) != null) {
					registerErrorMsg.setText("");

					String res = json.getString(KEY_SUCCESS);
					String red = json.getString(KEY_ERROR);

					if (Integer.parseInt(res) == 1) {

						// user successfully registred
						// Store user details in SQLite Database
						DatabaseHandler db = new DatabaseHandler(
								getApplicationContext());
						JSONObject json_user = json.getJSONObject("user");

						// Clear all previous data in database
						UserFunctions logout = new UserFunctions();
						logout.logoutUser(getApplicationContext());
						
						db.addUser(json_user.getString(KEY_NAME),
								json_user.getString(KEY_FULLNAME),
								json_user.getString(KEY_EMAIL),
								json.getString(KEY_UID),
								json_user.getString(KEY_CREATED_AT)
						// ,json_user.getString(KEY_PROF_PHOTO_URI)
						);

						// Launch Dashboard Screen
						Intent dashboard = new Intent(getApplicationContext(),
								CategoryActivity.class);

						// Close all views before launching Dashboard
						dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						pDialog.dismiss();
						startActivity(dashboard);

						// Close Registration Screen
						finish();
					}

					else if (Integer.parseInt(red) == 2) {
						pDialog.dismiss();
						registerErrorMsg.setText("Email existed. Pls login");
					} else if (Integer.parseInt(red) == 3) {
						pDialog.dismiss();
						registerErrorMsg.setText("Username exists");
					} else if (Integer.parseInt(red) == 4) {
					pDialog.dismiss();
					registerErrorMsg.setText("Invalid Email");
					}

				} else {
					pDialog.dismiss();

					registerErrorMsg.setText("Error occured in registration. Pls contact support");
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
