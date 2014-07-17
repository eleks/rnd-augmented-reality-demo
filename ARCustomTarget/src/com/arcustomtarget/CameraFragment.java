package com.arcustomtarget;

import android.os.Bundle;
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

	private int mTargetId = -1;

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
				Log.i(LOGTAG, "prepareToTakeAPicture id: " + mTargetId);
				// Create custom target
				if (mTargetId != -1) {
					_activity.startBuild();
					mTargetId = -1;
				}
			}
		});

		// Touch Listener
		_rootView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				_activity.onTouchEvent(event);
				return true;
			}
		});

		return _rootView;
	}

	public void prepareToTakeAPicture(int id) {
		mTargetId = id;
		// _rootView.bringToFront();
		// _takeAPictureButton.setVisibility(View.VISIBLE);
	}

}
