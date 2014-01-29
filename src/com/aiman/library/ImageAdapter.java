package com.aiman.library;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.aiman.zellar.R;
import com.aiman.zellar.UserProfileActivity;
import com.squareup.picasso.Picasso;

public class ImageAdapter extends BaseAdapter {
	Context mContext;
	LayoutInflater inflater;
	private final ArrayList<HashMap<String, String>> urls;
	HashMap<String, String> resultp = new HashMap<String, String>();

	public ImageAdapter(Context context, ArrayList<HashMap<String, String>> grid_result) {
		urls = grid_result;
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SquaredImageView view = (SquaredImageView) convertView;
		if (view == null) {
			view = new SquaredImageView(mContext);
			view.setScaleType(CENTER_CROP);
		}

		// Get the image URL for the current position.
		resultp = urls.get(position);
		
		String url = resultp.get(UserProfileActivity.TAG_ITEM_PHOTO);
		
		// Trigger the download of the URL asynchronously into the image view.
		Picasso.with(mContext) //
				.load(Constants.ROOT_SERVER_URL + url) //
				.placeholder(R.drawable.placeholder) //
				.error(R.drawable.error) //
				.fit() //
				.into(view);

		return view;
	}

	@Override
	public int getCount() {
		return urls.size();
	}

	@Override
	public String getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
