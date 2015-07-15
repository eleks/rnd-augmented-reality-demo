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

import com.ar.vuforia4template.core.FragmentActivityImageTargets;
import com.qualcomm.vuforia.CameraDevice;

public class CameraFragment extends Fragment {
	private String LOGTAG = "CameraFragment";
	public static final String ARG_NUMBER = "menu_item_number";

	private ActivityMagicLens _activity;

	private View _rootView;
	private Button _takeAPictureButton;

	private String _takeAPictureStr = "";
	private String _addNewTargetStr = "";

	private boolean _firstTimeShow = true;
	private boolean _buttonEnabled = true;

	private int _targetId = -1;

	public void setActivity(ActivityMagicLens aActivity) {
		_activity = aActivity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		_rootView = inflater.inflate(R.layout.main_fragment, container, false);

		// Take A Picture button
		_takeAPictureButton = (Button) _rootView
				.findViewById(R.id.mainFragmentCaptureButton);
		_takeAPictureButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disableButton();
				focusCameraAndRequestClick();
//				_activity.onNewARButtonClicked();

				// Log.i(LOGTAG, "prepareToTakeAPicture id: " + _targetId);
				//
				// String buttonName = _takeAPictureButton.getText().toString();
				// if (buttonName.equals(_takeAPictureStr)) {
				// // Create custom target
				// if ((_targetId != -1) && (_activity.startBuild(_targetId))) {
				// _targetId = -1;
				// disableButton();
				// }
				// }
				//
				// if (buttonName.equals(_addNewTargetStr)) {
				// _activity.requestAddNewTarget();
				// }

			}

		});
		if (_firstTimeShow) {
			hideButton();
			_firstTimeShow = false;
		}

		// Touch Listener
		_rootView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return _activity.onTouchEvent(event);
			}
		});

		_takeAPictureStr = getResources().getString(R.string.take_a_picture);
		_addNewTargetStr = getResources().getString(R.string.add_new_target);

		Log.i(LOGTAG, "!!! @@@ " + _takeAPictureStr);
		Log.i(LOGTAG, "!!! @@@ " + _addNewTargetStr);

		return _rootView;
	}

	public void focusCameraAndRequestClick() {
		final Handler autofocusHandler = new Handler();
		autofocusHandler.postDelayed(new Runnable() {
			public void run() {
				CameraDevice.getInstance().setFocusMode(
						CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
				requestClick();
			}
		}, 500L);
	}

	public void requestClick() {
		final Handler autofocusHandler = new Handler();
		autofocusHandler.postDelayed(new Runnable() {
			public void run() {
				_activity.onNewARButtonClicked();
			}
		}, 500L);
	}

	public void onTargetCreated() {
		enableButton();
	}

	@Override
	public void onPause() {
		Log.i(LOGTAG, "!!! on Pause");

		if (_targetId != -1) {
			Log.i(LOGTAG, "!!! on Pause +");

			// _activity.responceTargetFromCamera(_targetId, false);
			_targetId = -1;
			hideButton();
			enableButton();
		}

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		updataButtonState();
	}

	private boolean trackablesLimit() {
		return (_activity.mTargetsList.size() >= ActivityMagicLens.MAX_TRACKABLES);
	}

	public void updataButtonState() {
		_activity.hintTargetsLimitSetVisible(trackablesLimit());
		_takeAPictureButton.setEnabled(_buttonEnabled && !trackablesLimit());
	}

	public void onBadFrameQuality() {
		enableButton();
	}

	private void disableButton() {
		_buttonEnabled = false;
		updataButtonState();
	}

	private void enableButton() {
		_buttonEnabled = true;
		updataButtonState();
	}

	private void hideButton() {
		Handler refresh = new Handler(_activity.getMainLooper());
		refresh.post(new Runnable() {
			public void run() {
				if (_addNewTargetStr.length() > 0) // FIXME: bad code
					_takeAPictureButton.setText(_addNewTargetStr);
			}
		});
	}

	public boolean testIfCanAddNewAR() {
		return _activity.mTargetsList.size() >= FragmentActivityImageTargets.MAX_TRACKABLES;
	}

	public boolean testIfCanAddNewARAndUpdateButton() {
		boolean res = testIfCanAddNewAR();

		Handler refresh = new Handler(_activity.getMainLooper());
		refresh.post(new Runnable() {
			public void run() {
				disableButton();
				// TODO: change caption
			}
		});

		return res;
	}

}
