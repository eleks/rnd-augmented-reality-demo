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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ar.vuforiatemplate.core.ARModule;
import com.ar.vuforiatemplate.core.ARObjectsMediator;
import com.ar.vuforiatemplate.core.ActivityTargetsEvents;
import com.ar.vuforiatemplate.core.FragmentActivityImageTargets;
import com.ar.vuforiatemplate.meshobjects.TextureObject;
import com.ar.vuforiatemplate.objects.ARObjectManagement;
import com.ar.vuforiatemplate.objects.ARTexture;
import com.ar.vuforiatemplate.shaders.HueAnimationShaders;
import com.ar.vuforiatemplate.shaders.OpenGLShaders;
import com.ar.vuforiatemplate.shaders.TransparentShaders;
import com.ar.vuforiatemplate.shaders.VideoShaders;
import com.ar.vuforiatemplate.utils.SampleApplicationException;
import com.ar.vuforiatemplate.ux.GestureInfo;
import com.ar.vuforiatemplate.ux.Gestures;
import com.ar.vuforiatemplate.ux.MultiGestureListener;
import com.arcustomtarget.core.HintTargetsListArrayAdapter;
import com.arcustomtarget.core.TargetsListItem;
import com.arcustomtarget.ui.DrawerMenuArrayAdapter;
import com.arcustomtarget.ui.DrawerMenuItem;
import com.qualcomm.vuforia.Trackable;

public class ActivityMagicLens extends FragmentActivityImageTargets implements
		ActivityTargetsEvents, MultiGestureListener {
	private static final String LOGTAG = "ActivityMagicLens";
	private static final String DAT_FILE_NAME = "Targets.dat";
	private Gestures _gestures;

	public static int FRAGMENT_CAMERA_POSITION = 0;
	public static int FRAGMENT_TARGETS_POSITION = 1;

	// UI
	// Drawer menu
	private DrawerLayout _drawerLayout;
	private ListView _drawerList;
	private ActionBarDrawerToggle _drawerToggle;

	private int _currentFragmentPosition = -1;
	private CharSequence _drawerTitle;
	private CharSequence _title;
	private DrawerMenuItem[] _menuDrawerTitles;

	private ListView _hintTargetsListView;

	// fragments
	CameraFragment _cameraFragment;
	TargetsFragment _targetsFragment;

	// Hint on screen center
	boolean _hintPutCameraBehind = false;
	TextView _hintText;
	TextView _hintTargetLimitText;

	// Targets
	public Vector<TargetsListItem> mTargetsList = new Vector<TargetsListItem>();
	private int _targetsNumberPrev = 0;

	// Exit test
	private long _backPressedTimestamp = 0;
	public static int BACK_BUTTON_DELTA_TEST = 2000;

	public ActivityMagicLens() {
		super(R.id.loading_indicator2, R.layout.activity_with_drawer_layout);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "onCreate 1");

		// Fragments
		_cameraFragment = new CameraFragment();
		_cameraFragment.setActivity(this);

		_targetsFragment = new TargetsFragment();
		_targetsFragment.setActivity(this);

		// Vuforia
		// AR Module
		ARModule arModule = new ARModule();

		// MeshObjects
		arModule.addMeshObject("texture", new TextureObject());

		// Targets
		// "wiki"
		ARTexture wiki = new ARTexture(arModule.getMeshObject("texture"),
				arModule.getShader("hue_animation", true),
				"images/www_icon.png");
		arModule.addARObjectManagement("wiki", wiki);

		// create Objects Mediator
		_arObjectsMediator = new ARObjectsMediator(arModule);

		// gestures
		_gestures = new Gestures(this, this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_with_drawer_layout);

		_hintText = (TextView) findViewById(R.id.hintText);
		_hintTargetLimitText = (TextView) findViewById(R.id.hintTargetsLimitText);
		_hintTargetsListView = (ListView) findViewById(R.id.targetsList);
		_hintTargetsListView.setEmptyView(findViewById(R.id.emptyElement));

		UpdateActionBar(this);
		PrepareDrawerMenu(savedInstanceState);

		// Custom targets
		// loadTargets();

		// button Open Menu
		ImageView openMenu = (ImageView) findViewById(R.id.openDrawerMenu);
		openMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_drawerLayout.openDrawer(Gravity.LEFT);
			}
		});
	}

	@Override
	public void onInitARDone(SampleApplicationException exception) {
		super.onInitARDone(exception);
		updateViewZPosition();
	}

	@Override
	public void onStart() {
		super.onStart();
		updateViewZPosition();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateViewZPosition();
	}

	private void updateViewZPosition() {
		// FIXME: bad code ! (put GLSurfaceView to back)
		View v = (View) findViewById(R.id.drawer_layout);
		if (null != v)
			v.bringToFront();
		else
			Log.e(LOGTAG, "findViewById(R.id.drawer_layout) Error !!!");

		View p = (View) findViewById(R.id.loading_indicator2);
		if (null != p)
			p.setVisibility(View.INVISIBLE);
		else
			Log.e(LOGTAG, "findViewById(R.id.loading_indicator2) Error !!!");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (_drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle action buttons
		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("NewApi")
	public static void UpdateActionBar(Activity activity) {
		ActionBar ab = activity.getActionBar();
		if (null == ab) {
			Log.e(LOGTAG, "UpdateActionBar, ActionBar == null");
			return;
		}
		ab.setHomeButtonEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);
	}

	@SuppressLint("NewApi")
	public static void onHomeSelected(Activity activity, MenuItem menuItem) {
		Intent homeIntent = new Intent(activity, ActivityMagicLens.class);
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(homeIntent);
	}

	@SuppressLint("NewApi")
	public void PrepareDrawerMenu(Bundle savedInstanceState) {
		_title = _drawerTitle = getTitle();

		// drawer menu
		DrawerMenuItem item1 = new DrawerMenuItem("Camera", getResources()
				.getDrawable(R.drawable.icon), _cameraFragment);
		DrawerMenuItem item2 = new DrawerMenuItem("Targets", getResources()
				.getDrawable(R.drawable.icon), _targetsFragment);
		DrawerMenuItem item3 = new DrawerMenuItem("Travel Book");
		DrawerMenuItem item4 = new DrawerMenuItem("About");

		_menuDrawerTitles = new DrawerMenuItem[] { item1, item2, item3, item4 };

		_drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		_drawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		_drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		_drawerList.setAdapter(new DrawerMenuArrayAdapter(this,
				_menuDrawerTitles));
		_drawerList.setOnItemClickListener(new DrawerItemClickListener());

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		_drawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		_drawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				ActionBar ab = getActionBar();
				if (null != ab) {
					ab.setTitle(_title);
				}
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				ActionBar ab = getActionBar();
				if (null != ab) {
					ab.setTitle(_drawerTitle);
				}
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		_drawerLayout.setDrawerListener(_drawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	public void selectItem(int position) {
		Log.i(LOGTAG, "selectItem " + position);

		if (position >= _menuDrawerTitles.length)
			return;

		if (_menuDrawerTitles[position].mCaption.equals("Travel Book")) {
			onTravelBookClicked();
			_drawerLayout.closeDrawer(_drawerList);
			return;
		}

		if (_menuDrawerTitles[position].mCaption.equals("About")) {
			onAboutClicked();
			_drawerLayout.closeDrawer(_drawerList);
			return;
		}

		if ((null != _menuDrawerTitles[position].mFragment)
				&& (_currentFragmentPosition != position)) {

			// update the main content by replacing fragments
			Fragment fragment = _menuDrawerTitles[position].mFragment;
			Bundle args = new Bundle();
			args.putInt(CameraFragment.ARG_NUMBER, position);
			fragment.setArguments(args);

			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();

			_currentFragmentPosition = position;
		}

		// update selected item and title, then close the drawer
		_drawerList.setItemChecked(position, true);
		setTitle(_menuDrawerTitles[position].mCaption);
		_drawerLayout.closeDrawer(_drawerList);
	}

	@SuppressLint("NewApi")
	@Override
	public void setTitle(CharSequence title) {
		_title = title;
		ActionBar ab = getActionBar();
		if (null != ab) {
			ab.setTitle(_title);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		_drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		_drawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i(LOGTAG, "onTouchEvent - " + event.toString());

		if (null != _gestures)
			_gestures.onTouchEvent(event);

		return super.onTouchEvent(event);
	}

	// @Override
	public void loadTextures() {
		List<String> files = new ArrayList<String>();
		files.add("images/www_icon.png");

		_arObjectsMediator.loadTextures(files, getAssets());
	}

	@Override
	public boolean onGesture(GestureInfo aGestureInfo) {
		boolean res_super = super.onGesture(aGestureInfo);
		boolean res_aro = _arObjectsMediator.onGesture(aGestureInfo);
		return res_super || res_aro;
	}

	private String _trackTargetPrev = "";

	@Override
	public void onTargetTrack(Trackable arg0) {
		super.onTargetTrack(arg0);

		Log.i(LOGTAG, "Track: " + arg0.getName());

		if (!_hintPutCameraBehind) {
			_hintPutCameraBehind = true;
			runOnUiThread(new Runnable() {
				public void run() {
					_hintText.setVisibility(View.INVISIBLE);
				}
			});
		}

		if (!_trackTargetPrev.equals(arg0.getName())) {
			_trackTargetPrev = arg0.getName();
			for (TargetsListItem item : mTargetsList)
				item.mTracking = item.mTargetName.equals(_trackTargetPrev);

			runOnUiThread(new Runnable() {
				public void run() {
					updateTargetListView();
					animateTargetListView();
				}
			});
		}

	}

	@Override
	public void compileShaders() {
		Map<String, OpenGLShaders> shaders = new TreeMap<String, OpenGLShaders>();
		// shaders.put("simple", new SimpleShaders());
		// shaders.put("simple_normal", new NormalsShaders());
		shaders.put("transparent", new TransparentShaders());
		shaders.put("hue_animation", new HueAnimationShaders());
		shaders.put("video", new VideoShaders());

		_arObjectsMediator.compileShaders(shaders);
	}

	private boolean isPackageExists(String targetPackage) {
		List<ApplicationInfo> packages;
		PackageManager pm;
		pm = getPackageManager();
		packages = pm.getInstalledApplications(0);
		for (ApplicationInfo packageInfo : packages) {
			if (packageInfo.packageName.equals(targetPackage))
				return true;
		}
		return false;
	}

	private void onTravelBookClicked() {
		if (isPackageExists("com.lwiwbuch")) {
			final Intent intent = new Intent(
					"com.lwiwbuch/.ActivitySplashScreen");
			intent.setPackage("com.lwiwbuch");
			intent.setAction("android.intent.action.MAIN");
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException anfe) {
				AlertDialog alertDialog = new AlertDialog.Builder(this)
						.create();
				alertDialog.setTitle("Client apk not found");
				alertDialog.setMessage("Exception: "
						+ anfe.getLocalizedMessage());
				alertDialog.show();
			}

		} else {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("App not found");
			alertDialog.setMessage("Please install Travel Book app");
			alertDialog.show();
		}
	}

	private void onAboutClicked() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.about_layout);
		dialog.setTitle("Augmented Reality demo app");

		Button dialogButton = (Button) dialog.findViewById(R.id.aboutOkButton);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		OnClickListener ocl = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "http://eleks.com";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		};

		OnClickListener ocl_contacts = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = "http://eleks.com/company/contact-us";
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		};

		ImageView eleksLogo = (ImageView) dialog.findViewById(R.id.logoImage);
		eleksLogo.setOnClickListener(ocl);

		TextView linkText = (TextView) dialog.findViewById(R.id.linkText);
		linkText.setOnClickListener(ocl);

		ImageView qrLinkImage = (ImageView) dialog
				.findViewById(R.id.qrSiteImage);
		qrLinkImage.setOnClickListener(ocl);

		ImageView qrLinkContactsImage = (ImageView) dialog
				.findViewById(R.id.qrContactsSiteImage);
		qrLinkContactsImage.setOnClickListener(ocl_contacts);

		dialog.show();
	}

	public void loadTargets() {
		File file = new File(this.getFilesDir(), DAT_FILE_NAME);
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

	public void saveTargets() {
		File file = new File(this.getFilesDir(), DAT_FILE_NAME);
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

	@Override
	public void targetCreated() {
		super.targetCreated();
		Log.i(LOGTAG, "!!! Target created " + _lastTargetName);

		TargetsListItem item = new TargetsListItem("new AR #"
				+ (mTargetsList.size() + 1));
		mTargetsList.add(item);

		_targetsFragment.AddNewAR(mTargetsList.size() - 1);

		runOnUiThread(new Runnable() {
			public void run() {
				_hintText.setText(R.string.put_object_behind);
				selectItem(FRAGMENT_TARGETS_POSITION);
				_cameraFragment.updataButtonState();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TargetsFragment.RESULT_LOAD_VIDEO) {
			if (resultCode == RESULT_OK) {
				if (null != _targetsFragment) {
					Uri selectedUri = data.getData();
					_targetsFragment.onActivityResultVideo(selectedUri);
				}
			}
		}
	}

	public void onAllTargetLose() {
		runOnUiThread(new Runnable() {
			public void run() {
				_hintText.setVisibility(View.VISIBLE);
			}
		});
	}

	public void onFirstTargetTrack() {
		runOnUiThread(new Runnable() {
			public void run() {
				_hintText.setVisibility(View.INVISIBLE);
			}
		});
	}

	public void onNewARButtonClicked() {
		if (!startBuild()) {
			showToast("Bad quality, please try again");
			_cameraFragment.onBadFrameQuality();
		}
	}

	public void showToast(String msg) {
		Toast toast = Toast.makeText(getApplicationContext(), msg,
				Toast.LENGTH_SHORT);
		toast.show();
	}

	public void connectTargetToVuforia(TargetsListItem item) {
		ARModule arModule = _arObjectsMediator.getModule();
		ARObjectManagement mngmnt = item.getARObjectManagement(this,
				_arObjectsMediator);

		arModule.addARObjectManagement(_lastTargetName, mngmnt);
		item.mTargetName = _lastTargetName;
		for (TargetsListItem i : mTargetsList)
			i.mTracking = false;
		item.mTracking = true;

		_cameraFragment.onTargetCreated();

		updateTargetListView();
	}

	@Override
	public void onBackPressed() {
		if (_currentFragmentPosition != FRAGMENT_CAMERA_POSITION)
			selectItem(FRAGMENT_CAMERA_POSITION);
		else {
			long time_ms = System.currentTimeMillis();
			if (_backPressedTimestamp + BACK_BUTTON_DELTA_TEST >= time_ms)
				super.onBackPressed();
			else {
				_backPressedTimestamp = time_ms;
				showToast(getResources()
						.getString(R.string.press_again_to_exit));
			}
		}
	}

	@Override
	public void removeTargetFromCurrentDataset(String targetName) {
		super.removeTargetFromCurrentDataset(targetName);
		_cameraFragment.updataButtonState();
	}

	public void hintTargetsLimitSetVisible(boolean aVisible) {
		if (aVisible && (_hintTargetLimitText.getVisibility() != View.VISIBLE))
			_hintTargetLimitText.setVisibility(View.VISIBLE);
		else if (!aVisible
				&& (_hintTargetLimitText.getVisibility() == View.VISIBLE))
			_hintTargetLimitText.setVisibility(View.INVISIBLE);

	}

	@Override
	public void customTargetRenderer(int aTargetsNumber) {
		super.customTargetRenderer(aTargetsNumber);

		if (_targetsNumberPrev != aTargetsNumber) {
			if (aTargetsNumber == 0)
				onAllTargetLose();
			else if (aTargetsNumber == 1 && _targetsNumberPrev == 0)
				onFirstTargetTrack();

			_targetsNumberPrev = aTargetsNumber;
		}
	}

	public void updateTargetListView() {
		_hintTargetsListView.setAdapter(new HintTargetsListArrayAdapter(this,
				mTargetsList));
	}

	protected void animateTargetListView() {
		// TODO Auto-generated method stub
	}

}
