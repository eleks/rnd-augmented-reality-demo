package com.arcustomtarget.core;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcustomtarget.R;

public class TargetsListArrayAdapter  extends ArrayAdapter<TargetsListItem> {
	private final Context _context;
	private final TargetsListItem[] _values;

	public TargetsListArrayAdapter(Context aContext, TargetsListItem[] aValues) {
		super(aContext, R.id.targets_layout, aValues);
		this._context = aContext;
		this._values = aValues;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.targets_list_icon_item, parent,
				false);

		// icon
		ImageView icon = (ImageView) rowView.findViewById(R.id.imageTargetItem);
//		if (null != _values[position].mDrawable)
//			icon.setImageDrawable(values[position].mDrawable);
		icon.setImageDrawable(_context.getResources().getDrawable(R.drawable.icon));

		// caption
		TextView caption = (TextView) rowView.findViewById(R.id.textTargetItem);
		caption.setText(_values[position].mCaption);

		return rowView;
	}

}
