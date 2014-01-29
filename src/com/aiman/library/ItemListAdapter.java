package com.aiman.library;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aiman.zellar.R;
import com.aiman.zellar.ItemDetailActivity;
import com.aiman.zellar.ItemListActivity;
import com.squareup.picasso.Picasso;

public class ItemListAdapter extends BaseAdapter {
	Context context;
	LayoutInflater inflater;
	private final ArrayList<HashMap<String, String>> urls;
	HashMap<String, String> resultp = new HashMap<String, String>();

	public ItemListAdapter(Context context,
			ArrayList<HashMap<String, String>> arraylist) {
		this.context = context;
		urls = arraylist;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ImageView thumb_photo;
		TextView track_no;
		TextView category_name;
		TextView item_price;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
        View view = inflater.inflate(R.layout.custom_list_itemlist, parent, false);
        // Get the position
        resultp = urls.get(position);

		track_no = (TextView) view.findViewById(R.id.track_no);
		category_name = (TextView) view.findViewById(R.id.category_name);
		item_price = (TextView) view.findViewById(R.id.item_price);
		thumb_photo = (ImageView) view.findViewById(R.id.item_photo_small);

		track_no.setText(resultp.get(ItemListActivity.TAG_TRACK_NO));
		category_name.setText(resultp.get(ItemListActivity.TAG_NAME));
		item_price.setText(resultp.get(ItemListActivity.TAG_PRICE));


		// Trigger the download of the URL asynchronously into the image view.
		Picasso.with(context) //
				.load(Constants.ROOT_SERVER_URL + resultp.get(ItemListActivity.TAG_THUMB_ITEM)) //
				.placeholder(R.drawable.placeholder) //
				.error(R.drawable.error) //
				.fit() //
				.into(thumb_photo);

		view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// Get the position
                		resultp = urls.get(position);

						// On selecting single track get song information
						Intent i = new Intent(context,
								ItemDetailActivity.class);

						// to get song information
						// both album id and song is needed
						String category_id = resultp.get(ItemListActivity.TAG_CATEGORY_ID);
						String item_id = resultp.get(ItemListActivity.TAG_ID);

						i.putExtra("category_id", category_id);
						i.putExtra("item_id", item_id);

						context.startActivity(i);
					}
				});

		return view;
	}

	@Override
	public int getCount() {
		return urls.size();
	}

	@Override
	public String getItem(int position) {
		// return urls.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}