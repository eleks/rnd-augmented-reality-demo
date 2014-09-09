package com.arcustomtarget.core;

import java.util.Vector;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.arcustomtarget.R;

public class HintTargetsListArrayAdapter extends ArrayAdapter<TargetsListItem> {
	private final Context _context;
	private final Vector<TargetsListItem> _values;

	public HintTargetsListArrayAdapter(Context aContext,
			Vector<TargetsListItem> aValues) {
		super(aContext, R.id.mainRelativeLayout, aValues);
		this._context = aContext;
		this._values = aValues;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.hint_targets_list_icon_item,
				parent, false);

		TargetsListItem item = _values.get(position);

		// caption
		TextView caption = (TextView) rowView
				.findViewById(R.id.hintTextTargetItem);
		caption.setText(item.mCaption);

		if (item.mTracking == true)
			caption.setBackgroundColor(Color.YELLOW);
		else
			caption.setBackgroundColor(Color.WHITE);

		return rowView;
	}

}
