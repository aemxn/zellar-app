/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package com.aiman.zellar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.aiman.library.DatabaseHandler;
import com.aiman.library.ImageAdapter;
import com.aiman.library.UserFunctions;
import com.aiman.zellar.R;

public class DashboardActivity extends Activity {
	UserFunctions userFunctions;
	//String userDetails;
	DatabaseHandler db;
	Button btnLogout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 
		// Check login status in database
		userFunctions = new UserFunctions();
		db = new DatabaseHandler(getApplicationContext());
		
		if (userFunctions.isUserLoggedIn(getApplicationContext())) {
		    setContentView(R.layout.activity_dashboard);
		    
		    // Check toast which user is logged in
/*		    userDetails = db.getUserDetails().toString();
		    
			Toast.makeText(DashboardActivity.this, "Logged in: " + userDetails,
					Toast.LENGTH_LONG).show();*/
		    
		    GridView gv = (GridView) findViewById(R.id.grid_view);
		    //gv.setAdapter(new ImageAdapter(this));
		    
	        /**
	         * On Click event for Single Gridview Item
	         * */
	        gv.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	                // Sending image id to FullScreenActivity
	                Intent i = new Intent(getApplicationContext(), UserProfileActivity.class);
	                // passing array index
	                i.putExtra("id", position);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.actionbar_common, menu);
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
		case R.id.action_camera:
			Intent upload = new Intent(getApplicationContext(),
					UploadImageActivity.class);
			upload.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(upload);
			return true;
		case R.id.action_category:
			Intent category = new Intent(getApplicationContext(),
					CategoryActivity.class);
			category.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(category);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}