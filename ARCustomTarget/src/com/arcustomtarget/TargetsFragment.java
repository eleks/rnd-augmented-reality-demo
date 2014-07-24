package com.arcustomtarget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
	// private final String LOGTAG = "TargetsFragment";
	public static final String ARG_NUMBER = "menu_item_number";

	private ListView _targetsListView;

	private View _rootView;

	private ActivityMagicLens _activity;

	public void setActivity(ActivityMagicLens aActivity) {
		_activity = aActivity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		_rootView = inflater
				.inflate(R.layout.second_fragment, container, false);

		_targetsListView = (ListView) _rootView.findViewById(R.id.targets_list);
		_targetsListView.setOnItemClickListener(new TargetClickListener());

		updateList();

		// Add new target
		Button newTarget = (Button) _rootView.findViewById(R.id.target_add_new);
		newTarget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TargetsListItem item = new TargetsListItem("target ¹"
						+ (ActivityMagicLens.mTargetsList.length+1) );
				addTarget(item);
			}
		});

		return _rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private synchronized void updateList() {
		_targetsListView.setAdapter(new TargetsListArrayAdapter(getActivity(),
				ActivityMagicLens.mTargetsList));
	}

	private synchronized void removeTargetWithId(int id) {
		TargetsListItem[] tmp = new TargetsListItem[ActivityMagicLens.mTargetsList.length - 1];
		for (int i = 0; i < id; i++)
			tmp[i] = ActivityMagicLens.mTargetsList[i];
		for (int i = id + 1; i < ActivityMagicLens.mTargetsList.length; i++)
			tmp[i - 1] = ActivityMagicLens.mTargetsList[i];

		ActivityMagicLens.mTargetsList = tmp;

		updateList();
	}

	private synchronized void changeTargetWithId(int id, TargetsListItem item) {
		ActivityMagicLens.mTargetsList[id] = item;
		updateList();
	}

	private synchronized void addTarget(TargetsListItem item) {
		TargetsListItem[] tmp = new TargetsListItem[ActivityMagicLens.mTargetsList.length + 1];

		for (int i = 0; i < ActivityMagicLens.mTargetsList.length; i++)
			tmp[i] = ActivityMagicLens.mTargetsList[i];
		tmp[ActivityMagicLens.mTargetsList.length] = item;

		ActivityMagicLens.mTargetsList = tmp;

		showEditDialog(ActivityMagicLens.mTargetsList.length - 1, true);

		updateList();
	}

	class TargetClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < ActivityMagicLens.mTargetsList.length)
				showInfoDialog(position);
		}

	}

	private void showInfoDialog(int id) {
		final int fId = id;

		// custom dialog
		Context context = getActivity();

		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.target_dialog_layout);

		// Caption
		dialog.setTitle(ActivityMagicLens.mTargetsList[id].mCaption);

		// Target Icon
		ImageView image = (ImageView) dialog
				.findViewById(R.id.targetDialogImage);
		image.setImageResource(ActivityMagicLens.mTargetsList[id]
				.getDrawableId());

		// Target Icon Caption
		TextView targetIconCaption = (TextView) dialog
				.findViewById(R.id.targetDialogImageCaption);
		targetIconCaption.setText(ActivityMagicLens.mTargetsList[id]
				.getDrawableCaption());

		// Target data text
		TextView text = (TextView) dialog.findViewById(R.id.targetDialogText);
		text.setText(ActivityMagicLens.mTargetsList[id].mData);

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
				showEditDialog(fId, false);
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

	private void showEditDialog(int id, boolean newItem) {
		final int fId = id;
		final boolean fNewItem = newItem;

		// custom dialog
		Context context = getActivity();

		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.target_edit_dialog_layout);

		// Caption
		dialog.setTitle(ActivityMagicLens.mTargetsList[id].mCaption);

		// Caption text
		final EditText editTextCaption = (EditText) dialog
				.findViewById(R.id.targetEditDialogTextCaption);
		editTextCaption.setText(ActivityMagicLens.mTargetsList[id].mCaption);

		// Radio buttons
		final RadioButton textRadioButton = (RadioButton) dialog
				.findViewById(R.id.radioButtonText);
		final RadioButton urlRadioButton = (RadioButton) dialog
				.findViewById(R.id.radioButtonURL);
		final RadioButton videoRadioButton = (RadioButton) dialog
				.findViewById(R.id.radioButtonVideo);

		if (ActivityMagicLens.mTargetsList[id].mType == TargetsListItem.TARGET_TEXT)
			textRadioButton.setChecked(true);
		else if (ActivityMagicLens.mTargetsList[id].mType == TargetsListItem.TARGET_URL)
			urlRadioButton.setChecked(true);
		else if (ActivityMagicLens.mTargetsList[id].mType == TargetsListItem.TARGET_VIDEO)
			videoRadioButton.setChecked(true);

		// Data text
		final EditText editTextData = (EditText) dialog
				.findViewById(R.id.targetEditDialogTextData);
		editTextData.setText(ActivityMagicLens.mTargetsList[id].mData);

		// OK button
		Button dialogButtonOK = (Button) dialog
				.findViewById(R.id.targetEditDialogButtonOK);
		if (newItem) {
			dialogButtonOK.setText("OK, take a picture");
		}

		dialogButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TargetsListItem item = new TargetsListItem(editTextCaption
						.getText().toString());
				item.mData = editTextData.getText().toString();
				if (textRadioButton.isChecked())
					item.mType = TargetsListItem.TARGET_TEXT;
				else if (urlRadioButton.isChecked())
					item.mType = TargetsListItem.TARGET_URL;
				else if (videoRadioButton.isChecked())
					item.mType = TargetsListItem.TARGET_VIDEO;

				changeTargetWithId(fId, item);

				_activity.saveTargets();
				dialog.dismiss();

				if (fNewItem) {
					_activity.prepateToTakeAPicture(fId);
					_activity
							.selectItem(ActivityMagicLens.FRAGMENT_CAMERA_POSITION);
				}
			}
		});

		// Cancel button
		Button dialogButtonCancel = (Button) dialog
				.findViewById(R.id.targetEditDialogButtonCancel);
		dialogButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				if (fNewItem)
					removeTargetWithId(fId);
			}
		});

		dialog.show();
	}

}
