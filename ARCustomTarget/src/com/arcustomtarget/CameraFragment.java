package com.arcustomtarget;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class CameraFragment extends Fragment {
	private String LOGTAG = "CameraFragment";
	public static final String ARG_NUMBER = "menu_item_number";

	private ActivityMagicLens _activity;

	private View _rootView;
	private Button _takeAPictureButton;

	private boolean _firstTimeShow = true;

	private int _targetId = -1;

	public void setActivity(ActivityMagicLens aActivity) {
		_activity = aActivity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		_rootView = inflater.inflate(R.layout.main_fragment, container, false);
		int i = getArguments().getInt(ARG_NUMBER);
		String menuItemName = getResources().getStringArray(
				R.array.menu_drawer_array)[i];

		getActivity().setTitle(menuItemName);

		// Take A Picture button
		_takeAPictureButton = (Button) _rootView
				.findViewById(R.id.mainFragmentCaptureButton);
		_takeAPictureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOGTAG, "prepareToTakeAPicture id: " + _targetId);
				// Create custom target
				if (_targetId != -1) {
					if (_activity.startBuild(_targetId)) {
						_targetId = -1;
						disableButton();
					}
				}
			}
		});
		if (_firstTimeShow) {
			hideButton();
			_firstTimeShow = false;
		}

		// Touch Listener
		_rootView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				_activity.onTouchEvent(event);
				return true;
			}
		});

		return _rootView;
	}

	public void requestTargetFromCamera(int aTargetId) {
		_targetId = aTargetId;
		enableButton();
		showButton();
	}

	public void onTargetCreated() {
		hideButton();
		enableButton();
	}

	public void onTargetFailedToCreate(int aTargetId) {
		_targetId = aTargetId;
		enableButton();
		showButton();
	}

	@Override
	public void onPause() {
		Log.i(LOGTAG, "!!! on Pause");

		if (_targetId != -1) {
			Log.i(LOGTAG, "!!! on Pause +");

			_activity.responceTargetFromCamera(_targetId, false);
			_targetId = -1;
			hideButton();
			enableButton();
		}

		super.onPause();
	}

	private void disableButton() {
		// _takeAPictureButton.setEnabled(false);
	}

	private void enableButton() {
		// _takeAPictureButton.setEnabled(true);
	}

	private void showButton() {
		Log.i(LOGTAG, "!!! show button");
		Handler refresh = new Handler(_activity.getMainLooper());
		refresh.post(new Runnable() {
			public void run() {
				// _takeAPictureButton.setVisibility(View.VISIBLE);
				// _takeAPictureButton.setEnabled(true);
			}
		});
		// _takeAPictureButton.setEnabled(true);
	}

	private void hideButton() {
		Log.i(LOGTAG, "!!! hide button");
		Handler refresh = new Handler(_activity.getMainLooper());
		refresh.post(new Runnable() {
			public void run() {
				// _takeAPictureButton.setVisibility(View.INVISIBLE);
				// _takeAPictureButton.setEnabled(false);
			}
		});
		// _takeAPictureButton.setEnabled(false);
	}

}
