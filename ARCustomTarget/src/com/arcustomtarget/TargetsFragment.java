package com.arcustomtarget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

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
	private final String LOGTAG = "TargetsFragment";
	private final String DAT_FILE_NAME = "Targets.dat";
	public static final String ARG_NUMBER = "menu_item_number";

	private ListView _targetsListView;
	private TargetsListItem[] _targetsList = new TargetsListItem[] {};

	private View _rootView;

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
				TargetsListItem item = new TargetsListItem("new");
				addTarget(item);
			}
		});

		return _rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		loadTargets();
	}

	private void loadTargets() {
		File file = new File(this.getActivity().getFilesDir(), DAT_FILE_NAME);
		if (file.exists()) {
			try {
				InputStream inFile = new FileInputStream(file);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inFile));
				// StringBuilder out = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					// out.append(line);
					Log.i(LOGTAG, "loadTargets !!! readed from file : " + line);
				}

				reader.close();
				inFile.close();
			} catch (IOException e) {
				Log.e(LOGTAG, "loadTargets IOException : " + e.getMessage());
				e.printStackTrace();
			}
		} else
			Log.e(LOGTAG, "loadTargets: file does not exists: " + DAT_FILE_NAME);
	}

	private void saveTargets() {
		File file = new File(this.getActivity().getFilesDir(), DAT_FILE_NAME);
		try {
			file.createNewFile();
		} catch (Exception e) {
			Log.e(LOGTAG, "saveTargets exception : " + e.getMessage());
			return;
		}

		if (file.exists()) {
			try {
				OutputStream outFile = new FileOutputStream(file);

				String str = "Hello smb.!";
				outFile.write(str.getBytes());
				outFile.close();

				Log.i(LOGTAG, "saveTargets !!! wrote to file : " + str);
			} catch (FileNotFoundException e) {
				Log.e(LOGTAG,
						"saveTargets FileNotFoundException : " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(LOGTAG, "saveTargets IOException : " + e.getMessage());
				e.printStackTrace();
			}
		} else
			Log.e(LOGTAG, "saveTargets: file does not exists: " + DAT_FILE_NAME);
	}

	private synchronized void updateList() {
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

	private synchronized void addTarget(TargetsListItem item) {
		TargetsListItem[] tmp = new TargetsListItem[_targetsList.length + 1];

		for (int i = 0; i < _targetsList.length; i++)
			tmp[i] = _targetsList[i];
		tmp[_targetsList.length] = item;

		_targetsList = tmp;
		
		showEditDialog(_targetsList.length-1, true);

		updateList();
	}

	class TargetClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < _targetsList.length)
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
		TextView text = (TextView) dialog.findViewById(R.id.targetDialogText);
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

				saveTargets();
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
				if (fNewItem)
					removeTargetWithId(fId);
			}
		});

		dialog.show();
	}

}
