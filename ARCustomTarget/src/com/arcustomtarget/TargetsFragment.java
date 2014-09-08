package com.arcustomtarget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.arcustomtarget.core.TargetsListArrayAdapter;
import com.arcustomtarget.core.TargetsListItem;

public class TargetsFragment extends Fragment {
	private final String LOGTAG = "TargetsFragment";
	public static final String ARG_NUMBER = "menu_item_number";

	public final static String DATA_TEXT = "Hello world !";
	public final static String DATA_URL = "http://www.google.com/";
	public final static String DATA_VIDEO = "video ...";

	public static final int RESULT_LOAD_VIDEO = 1;

	private ListView _targetsListView;

	private View _rootView;
	private Dialog _editDialog;

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
		newTarget.setVisibility(View.GONE); // temporary

		return _rootView;
	}

	// public void createNewTarget() {
	// Handler refresh = new Handler(_activity.getMainLooper());
	// refresh.post(new Runnable() {
	// public void run() {
	//
	// boolean canCreate = _activity.mTargetsList.length <
	// FragmentActivityImageTargets.MAX_TRACKABLES;
	// if (canCreate) {
	// TargetsListItem item = new TargetsListItem("target no.: "
	// + (_activity.mTargetsList.length + 1));
	// addTarget(item);
	// } else {
	// Context context = _activity.getApplicationContext();
	// CharSequence text =
	// "You reached limit of user targets;\nPlease, remove one.";
	// int duration = Toast.LENGTH_LONG;
	// Toast toast = Toast.makeText(context, text, duration);
	// toast.show();
	// }
	//
	// }
	// });
	// }

	private synchronized void updateList() {
		_targetsListView.setAdapter(new TargetsListArrayAdapter(_activity,
				_activity.mTargetsList));
	}

	private synchronized void removeTargetWithId(int id) {
		if (id < 0 || id >= _activity.mTargetsList.size())
			return;

		String targetName = _activity.mTargetsList.get(id).mTargetName;
		_activity.mTargetsList.remove(id);
		_activity.removeTargetFromCurrentDataset(targetName);

		updateList();
	}

	private synchronized void changeTargetWithId(int id, TargetsListItem item) {
		_activity.mTargetsList.set(id, item);
		updateList();
	}

	class TargetClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position < _activity.mTargetsList.size())
				showInfoDialog(position);
		}

	}

	private void showInfoDialog(int id) {
		final int fId = id;

		// custom dialog
		Context context = getActivity();

		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.target_dialog_layout);

		TargetsListItem item = _activity.mTargetsList.get(id);
		
		// Caption
		dialog.setTitle(item.mCaption);

		// Target Icon
		ImageView image = (ImageView) dialog
				.findViewById(R.id.targetDialogImage);
		image.setImageResource(item.getDrawableId());

		// Target Icon Caption
		TextView targetIconCaption = (TextView) dialog
				.findViewById(R.id.targetDialogImageCaption);
		targetIconCaption.setText(item
				.getDrawableCaption());

		// Target data text
		TextView text = (TextView) dialog.findViewById(R.id.targetDialogText);
		text.setText(item.mData);

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

	private static boolean _sCreateItem = false;

	private void showEditDialog(int id, boolean newItem) {
		final int fId = id;
		final boolean fNewItem = newItem;

		// custom dialog
		_editDialog = new Dialog(_activity);
		_editDialog.setContentView(R.layout.target_edit_dialog_layout);
		_editDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface arg0) {
				if (fNewItem && !_sCreateItem)
					removeTargetWithId(fId);
				_sCreateItem = false;
			}
		});

		// Caption
		TargetsListItem item = _activity.mTargetsList.get(id);
		_editDialog.setTitle(item.mCaption);

		// Caption text
		final EditText editTextCaption = (EditText) _editDialog
				.findViewById(R.id.targetEditDialogTextCaption);
		editTextCaption.setText(item.mCaption);

		// Data text
		final EditText editTextData = (EditText) _editDialog
				.findViewById(R.id.targetEditDialogTextData);
		String data = item.mData;
		if (data.length() > 0)
			editTextData.setText(data);
		else
			editTextData.setText(DATA_TEXT);

		editTextData.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent galleryIntent = new Intent(
						Intent.ACTION_GET_CONTENT);
				galleryIntent.setType("video/*");
				getActivity().startActivityForResult(galleryIntent,
						RESULT_LOAD_VIDEO);
			}
		});

		// Radio buttons
		final RadioButton textRadioButton = (RadioButton) _editDialog
				.findViewById(R.id.radioButtonText);
		textRadioButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (textRadioButton.isChecked())
							changeDataTextIfItIsVirgin(editTextData, DATA_TEXT);
					}
				});

		final RadioButton urlRadioButton = (RadioButton) _editDialog
				.findViewById(R.id.radioButtonURL);
		urlRadioButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (urlRadioButton.isChecked())
							changeDataTextIfItIsVirgin(editTextData, DATA_URL);
					}
				});

		final RadioButton videoRadioButton = (RadioButton) _editDialog
				.findViewById(R.id.radioButtonVideo);
		videoRadioButton
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton arg0,
							boolean arg1) {
						if (videoRadioButton.isChecked())
							changeDataTextIfItIsVirgin(editTextData, DATA_VIDEO);
					}
				});

		if (item.mType == TargetsListItem.TARGET_TEXT)
			textRadioButton.setChecked(true);
		else if (item.mType == TargetsListItem.TARGET_URL)
			urlRadioButton.setChecked(true);
		else if (item.mType == TargetsListItem.TARGET_VIDEO)
			videoRadioButton.setChecked(true);

		// OK button
		Button dialogButtonOK = (Button) _editDialog
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

				_sCreateItem = true;

				_activity.saveTargets();
				_editDialog.dismiss();

				_activity.selectItem(_activity.FRAGMENT_CAMERA_POSITION);
				_activity.connectTargetToVuforia(item);
			}
		});

		// Cancel button
		Button dialogButtonCancel = (Button) _editDialog
				.findViewById(R.id.targetEditDialogButtonCancel);
		dialogButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_editDialog.dismiss();
				if (fNewItem)
					removeTargetWithId(fId);
			}
		});

		_editDialog.show();
	}

	void changeDataTextIfItIsVirgin(EditText aEditText, String aStr) {
		String text = aEditText.getText().toString();
		if (text.equals(DATA_TEXT) || text.equals(DATA_URL)
				|| text.equals(DATA_VIDEO)) {
			aEditText.setText(aStr);
		}
	}

	public void onActivityResultVideo(Uri aFileName) {
		Log.i(LOGTAG, "!!! onActivityResultVideo : " + aFileName);

		if (_editDialog != null) {
			EditText editTextData = (EditText) _editDialog
					.findViewById(R.id.targetEditDialogTextData);
			if (editTextData != null) {
				editTextData.setText(getRealPathFromURI(getActivity(),
						aFileName));
			}
		}
	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	public void AddNewAR(int aID) {
		final int fID = aID;
		_activity.runOnUiThread(new Runnable() {
			public void run() {
				showEditDialog(fID, true);
			}
		});
	}

}
