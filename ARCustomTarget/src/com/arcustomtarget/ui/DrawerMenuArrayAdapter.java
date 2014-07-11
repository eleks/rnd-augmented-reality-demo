package com.arcustomtarget.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcustomtarget.R;

public class DrawerMenuArrayAdapter extends ArrayAdapter<DrawerMenuItem> {
	private final Context context;
	private final DrawerMenuItem[] values;

	public DrawerMenuArrayAdapter(Context context, DrawerMenuItem[] values) {
		super(context, R.id.content_frame, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.drawer_list_icon_item, parent,
				false);

		// icon
		ImageView icon = (ImageView) rowView.findViewById(R.id.imageDrawerItem);
		if (null != values[position].mDrawable)
			icon.setImageDrawable(values[position].mDrawable);

		// caption
		TextView caption = (TextView) rowView.findViewById(R.id.textCaption);
		caption.setText(values[position].mCaption);

		return rowView;
	}

}
