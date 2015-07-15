package com.ar.vuforia4template.ux;

import android.app.Activity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class Gestures {
	private static final String LOGTAG = "Gestures";

	private ScaleGestureDetector _scaleGestureDetector;
	private SwipeTouchListener _swipeTouchListener;
	private GestureDetector _singleGestureDetector;

	private MultiGestureListener _gestureListener;

	public Gestures(Activity aActivity, MultiGestureListener aGestureListener) {
		_gestureListener = aGestureListener;

		// SCALE
		_scaleGestureDetector = new ScaleGestureDetector(aActivity,
				new OnScaleGestureListener() {
					@Override
					public boolean onScaleBegin(ScaleGestureDetector detector) {
						_gestureListener.onGesture(new GestureInfo(
								GestureInfo.GESTURE_PINCH,
								GestureInfo.STATE_START, detector
										.getScaleFactor()));
						return true;
					}

					@Override
					public boolean onScale(ScaleGestureDetector detector) {
						_gestureListener.onGesture(new GestureInfo(
								GestureInfo.GESTURE_PINCH,
								GestureInfo.STATE_MOVE, detector
										.getScaleFactor()));
						return false;
					}

					@Override
					public void onScaleEnd(ScaleGestureDetector detector) {
						_gestureListener.onGesture(new GestureInfo(
								GestureInfo.GESTURE_PINCH,
								GestureInfo.STATE_FINISH, detector
										.getScaleFactor()));
					}
				});

		// SWIPE
		_swipeTouchListener = new SwipeTouchListener(aActivity) {
			@Override
			public boolean onSwipeRight() {
				Log.i(LOGTAG, "swipe right");
				return false;
			}

			@Override
			public boolean onSwipeLeft() {
				Log.i(LOGTAG, "swipe left");
				return false;
			}

			@Override
			public boolean onSwipeTop() {
				Log.i(LOGTAG, "swipe top");
				return false;
			}

			@Override
			public boolean onSwipeBottom() {
				Log.i(LOGTAG, "swipe bottom");
				return false;
			}

		};

		// DOUBLE TAP
		GestureDetector.SimpleOnGestureListener singleGestureListener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				return _gestureListener.onGesture(new GestureInfo(
						GestureInfo.GESTURE_DOUBLE_TAP, GestureInfo.STATE_MOVE,
						0));
			}

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return _gestureListener.onGesture(new GestureInfo(
						GestureInfo.GESTURE_SINGLE_TAP_UP,
						GestureInfo.STATE_MOVE, 0));
			}
		};

		_singleGestureDetector = new GestureDetector(aActivity,
				singleGestureListener);
	}

	public boolean onTouchEvent(MotionEvent event) {
		_scaleGestureDetector.onTouchEvent(event);
		_swipeTouchListener.onTouch(event);
		_singleGestureDetector.onTouchEvent(event);

		if (event.getPointerCount() == 1) {
			int action = event.getAction();
			switch (action) {
			case (MotionEvent.ACTION_MOVE):
				if (event.getHistorySize() > 0) {
					int pos = event.getHistorySize() - 1;
					float dx = event.getX() - event.getHistoricalX(pos);
					float dy = event.getY() - event.getHistoricalY(pos);

					_gestureListener.onGesture(new GestureInfo(
							GestureInfo.GESTURE_MOVE_X, GestureInfo.STATE_MOVE,
							dx));
					_gestureListener.onGesture(new GestureInfo(
							GestureInfo.GESTURE_MOVE_Y, GestureInfo.STATE_MOVE,
							dy));
				}
				break;

			case (MotionEvent.ACTION_UP):
				break;

			case (MotionEvent.ACTION_DOWN):
				_gestureListener.onGesture(new GestureInfo(
						GestureInfo.GESTURE_POINTER_TAP,
						GestureInfo.STATE_FINISH, 0));
				break;
			}
		}

		return false;
	}

}
