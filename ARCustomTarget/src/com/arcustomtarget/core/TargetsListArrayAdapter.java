package com.arcustomtarget.core;

import java.util.Vector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcustomtarget.R;

public class TargetsListArrayAdapter extends ArrayAdapter<TargetsListItem> {
	private final Context _context;
	private final Vector<TargetsListItem> _values;

	public TargetsListArrayAdapter(Context aContext, Vector<TargetsListItem> aValues) {
		super(aContext, R.id.targets_layout, aValues);
		this._context = aContext;
		this._values = aValues;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.targets_list_icon_item,
				parent, false);

		TargetsListItem item = _values.get(position);
		
		// icon
		ImageView icon = (ImageView) rowView.findViewById(R.id.imageTargetItem);
		// if (null != _values[position].mDrawable)
		// icon.setImageDrawable(values[position].mDrawable);
		int icon_id = item.getDrawableId();

		icon.setImageDrawable(_context.getResources().getDrawable(icon_id));

		// caption
		TextView caption = (TextView) rowView.findViewById(R.id.textTargetItem);
		caption.setText(item.mCaption);

		return rowView;
	}

}
