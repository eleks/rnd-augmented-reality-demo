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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.arcustomtarget.core.TargetsListArrayAdapter;
import com.arcustomtarget.core.TargetsListItem;

public class TargetsFragment extends Fragment {
	private String LOGTAG = "TargetsFragment";
	public static final String ARG_NUMBER = "menu_item_number";

	private ListView _targetsListView;
	private TargetsListItem[] _targetsList;

	private View _rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		_rootView = inflater
				.inflate(R.layout.second_fragment, container, false);
		// int i = getArguments().getInt(ARG_NUMBER);
		// String menuItemName = "Some name";
		// getResources().getStringArray(
		// R.array.menu_drawer_array)[i];
		// getActivity().setTitle(menuItemName);

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

		updateList();

		_targetsListView.setOnItemClickListener(new TargetClickListener());

		return _rootView;
	}

	private void updateList() {
		_targetsListView = (ListView) _rootView.findViewById(R.id.targets_list);
		_targetsListView.setAdapter(new TargetsListArrayAdapter(getActivity(),
				_targetsList));
	}

	private synchronized void removeTargetWithId(int id) {
		TargetsListItem[] tmp = new TargetsListItem[_targetsList.length - 1];
		for (int i = 0; i < id; i++)
			tmp[i] = _targetsList[i];
		for (int i = id + 1; i < _targetsList.length; i++)
			tmp[i - 1] = _targetsList[i];

		_targetsList = tmp;

		updateList();
	}

	private synchronized void changeTargetWithId(int id, TargetsListItem item) {
		_targetsList[id] = item;
		updateList();
	}

	class TargetClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < _targetsList.length)
				showInfoDialog(position);
		}

		private void showInfoDialog(int id) {
			final int fId = id;

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
			text.setText(_targetsList[id].mData);

			// OK button
			Button dialogButtonOK = (Button) dialog
					.findViewById(R.id.targetDialogButtonOK);
			dialogButtonOK.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			// Edit button
			Button dialogButtonEdit = (Button) dialog
					.findViewById(R.id.targetDialogButtonEdit);
			dialogButtonEdit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showEditDialog(fId);
					dialog.dismiss();
				}
			});

			// Delete button
			Button dialogButtonDelete = (Button) dialog
					.findViewById(R.id.targetDialogButtonDelete);
			dialogButtonDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					removeTargetWithId(fId);
					dialog.dismiss();
				}
			});

			dialog.show();
		}

		private void showEditDialog(int id) {
			final int fId = id;

			// custom dialog
			Context context = getActivity();

			final Dialog dialog = new Dialog(context);
			dialog.setContentView(R.layout.target_edit_dialog_layout);

			// Caption
			dialog.setTitle(_targetsList[id].mCaption);

			// Caption text
			final EditText editTextCaption = (EditText) dialog
					.findViewById(R.id.targetEditDialogTextCaption);
			editTextCaption.setText(_targetsList[id].mCaption);

			// Radio buttons
			final RadioButton textRadioButton = (RadioButton) dialog
					.findViewById(R.id.radioButtonText);
			final RadioButton urlRadioButton = (RadioButton) dialog
					.findViewById(R.id.radioButtonURL);
			final RadioButton videoRadioButton = (RadioButton) dialog
					.findViewById(R.id.radioButtonVideo);

			if (_targetsList[id].mType == TargetsListItem.TARGET_TEXT)
				textRadioButton.setChecked(true);
			else if (_targetsList[id].mType == TargetsListItem.TARGET_URL)
				urlRadioButton.setChecked(true);
			else if (_targetsList[id].mType == TargetsListItem.TARGET_VIDEO)
				videoRadioButton.setChecked(true);

			// Data text
			final EditText editTextData = (EditText) dialog
					.findViewById(R.id.targetEditDialogTextData);
			editTextData.setText(_targetsList[id].mData);

			// OK button
			Button dialogButtonOK = (Button) dialog
					.findViewById(R.id.targetEditDialogButtonOK);
			dialogButtonOK.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					TargetsListItem item = new TargetsListItem(
							editTextCaption.getText().toString());
					item.mData = editTextData.getText().toString();
					if (textRadioButton.isChecked())
						item.mType = TargetsListItem.TARGET_TEXT;
					else if (urlRadioButton.isChecked())
						item.mType = TargetsListItem.TARGET_URL;
					else if (videoRadioButton.isChecked())
						item.mType = TargetsListItem.TARGET_VIDEO;

					changeTargetWithId(fId, item);

					dialog.dismiss();
				}
			});

			// Cancel button
			Button dialogButtonCancel = (Button) dialog
					.findViewById(R.id.targetEditDialogButtonCancel);
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			dialog.show();
		}
	}

}
