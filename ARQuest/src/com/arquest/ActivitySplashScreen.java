package com.arquest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class ActivitySplashScreen extends Activity {
	private static long SPLASH_MILLIS = 1000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		LayoutInflater inflater = LayoutInflater.from(this);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(
				R.layout.splash_screen, null, false);

		addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent(ActivitySplashScreen.this,
						ActivityMagicLens.class);
				startActivity(intent);
			}

		}, SPLASH_MILLIS);

	}

}
