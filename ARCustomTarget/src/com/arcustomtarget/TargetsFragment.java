package com.arcustomtarget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.arcustomtarget.core.TargetsListArrayAdapter;
import com.arcustomtarget.core.TargetsListItem;

public class TargetsFragment extends Fragment {
	private String LOGTAG = "TargetsFragment";
	public static final String ARG_NUMBER = "menu_item_number";

	private ListView _targetsListView;
	private TargetsListItem[] _targetsList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.second_fragment, container,
				false);
		// int i = getArguments().getInt(ARG_NUMBER);
		String menuItemName = "Some name";
		// getResources().getStringArray(
		// R.array.menu_drawer_array)[i];

		getActivity().setTitle(menuItemName);

		// targets
		TargetsListItem item1 = new TargetsListItem("abcd", "smth.",
				TargetsListItem.TARGET_TEXT);
		TargetsListItem item2 = new TargetsListItem("abcd 2", "smth.",
				TargetsListItem.TARGET_URL);
		TargetsListItem item3 = new TargetsListItem("abcd 3", "smth.",
				TargetsListItem.TARGET_VIDEO);
		TargetsListItem item4 = new TargetsListItem("abcd 3", "smth.",
				TargetsListItem.TARGET_VIDEO);
		TargetsListItem item5 = new TargetsListItem("abcd 3", "smth.", 10);

		_targetsList = new TargetsListItem[] { item1, item2, item3, item4,
				item5 };

		_targetsListView = (ListView) rootView.findViewById(R.id.targets_list);
		_targetsListView.setAdapter(new TargetsListArrayAdapter(getActivity(),
				_targetsList));
		_targetsListView.setOnItemClickListener(new TargetClickListener());

		return rootView;
	}

	class TargetClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < _targetsList.length)
				showDialog(position);
		}

		private void showDialog(int id) {
			// custom dialog
			Context context = getActivity();

			final Dialog dialog = new Dialog(context);
			dialog.setContentView(R.layout.target_dialog_layout);

			// Caption
			dialog.setTitle(_targetsList[id].mCaption);

			// Target Icon
			ImageView image = (ImageView) dialog
					.findViewById(R.id.targetDialogImage);
			image.setImageResource(_targetsList[id].getDrawableId());

			// Target Icon Caption
			TextView targetIconCaption = (TextView) dialog
					.findViewById(R.id.targetDialogImageCaption);
			targetIconCaption.setText(_targetsList[id].getDrawableCaption());

			// Target data text
			TextView text = (TextView) dialog
					.findViewById(R.id.targetDialogText);
			text.setText( _targetsList[id].mData );

			Button dialogButton = (Button) dialog
					.findViewById(R.id.targetDialogButtonOK);
			// if button is clicked, close the custom dialog
			dialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			dialog.show();
		}
	}

}
