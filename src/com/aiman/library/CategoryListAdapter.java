package com.aiman.library;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aiman.zellar.CategoryActivity;
import com.aiman.zellar.R;
import com.haarman.listviewanimations.ArrayAdapter;
import com.squareup.picasso.Picasso;

public class CategoryListAdapter extends ArrayAdapter<HashMap<String, String>> {
	private Context mContext;
	LayoutInflater inflater;
	private final ArrayList<HashMap<String, String>> urls;
	HashMap<String, String> resultp = new HashMap<String, String>();

	public CategoryListAdapter(Context context,
			ArrayList<HashMap<String, String>> items) {
		super(items);
		mContext = context;
		urls = items;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView category_name;
		ImageView category_logo;
		TextView item_count;
		TextView item_id;

		inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.custom_list_category, parent,
				false);

		resultp = urls.get(position);

		category_name = (TextView) view.findViewById(R.id.category_name);
		category_logo = (ImageView) view.findViewById(R.id.img_category_logo);
		item_count = (TextView) view.findViewById(R.id.songs_count);
		item_id = (TextView) view.findViewById(R.id.category_id);

		category_name.setText(resultp.get(CategoryActivity.TAG_NAME));
		item_count.setText(resultp.get(CategoryActivity.TAG_CATEGORIES_COUNT));
		item_id.setText(resultp.get(CategoryActivity.TAG_ID));

		Picasso.with(mContext)
				//
				.load(Constants.CATEGORIES_LOGO
						+ resultp.get(CategoryActivity.TAG_CATEGORIES_LOGO)) //
				.placeholder(R.drawable.placeholder) //
				.error(R.drawable.error) //
				.fit().centerCrop() //
				.into(category_logo);

		return view;
	}
}
