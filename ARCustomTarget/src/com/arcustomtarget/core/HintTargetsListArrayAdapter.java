package com.arcustomtarget.core;

import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
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

	@SuppressLint("NewApi")
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

		if (item.mTracking == true) {
			caption.setBackgroundColor(Color.YELLOW);
			AlphaAnimation animation1 = new AlphaAnimation(0.3f, 1.0f);
			animation1.setDuration(300);
			animation1.setStartOffset(150);
			caption.startAnimation(animation1);
		} else {
			caption.setBackgroundColor(Color.WHITE);
			caption.setAlpha(0.3f);
		}

		return rowView;
	}

}
