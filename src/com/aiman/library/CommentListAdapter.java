package com.aiman.library;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aiman.zellar.R;
import com.aiman.zellar.ItemDetailActivity;

public class CommentListAdapter extends BaseAdapter {

	Context context;
	LayoutInflater inflater;
	private final ArrayList<HashMap<String, String>> urls;
	HashMap<String, String> resultp = new HashMap<String, String>();

	public CommentListAdapter(Context context, ArrayList<HashMap<String, String>> arraylist) {
		this.context = context;
		urls = arraylist;
	}

	@Override
	public int getCount() {
		return urls.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView username;
		TextView comment;

		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		View view = inflater.inflate(R.layout.custom_item_detail_comment, parent, false);

		resultp = urls.get(position);

		username = (TextView) view.findViewById(R.id.txtUsername);
		comment = (TextView) view.findViewById(R.id.txtComment);

		username.setText(resultp.get(ItemDetailActivity.TAG_USERNAME));
		comment.setText(resultp.get(ItemDetailActivity.TAG_COMMENT));

		return view;
	}

}
