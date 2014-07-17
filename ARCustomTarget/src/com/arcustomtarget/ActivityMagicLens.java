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
import java.util.Set;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

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
	Button _extendedTrackingButton;

	// Drawer menu
	private DrawerLayout _drawerLayout;
	private ListView _drawerList;
	private ActionBarDrawerToggle _drawerToggle;

	private int _currentFragmentPosition = -1;
	private CharSequence _drawerTitle;
	private CharSequence _title;
	private DrawerMenuItem[] _menuDrawerTitles;

	// fragments
	CameraFragment _cameraFragment;
	TargetsFragment _targetsFragment;

	// Targets
	static public TargetsListItem[] mTargetsList = new TargetsListItem[] {};
	private int _lastTakePictureId = -1;

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
		// addDataset("vuforia/test.xml");

		// AR Module
		ARModule arModule = new ARModule();

		// MeshObjects
		arModule.addMeshObject("texture", new TextureObject());

		// Targets
		// "wiki"
		ARTexture wiki = new ARTexture(arModule.getMeshObject("texture"),
				arModule.getShader("hue_animation", true),
				"images/wikipedia_mask.png");
		arModule.addARObjectManagement("wiki", wiki);

		// create Objects Mediator
		_arObjectsMediator = new ARObjectsMediator(arModule);

		// UI
		// Extended tracking button
		// _extendedTrackingButton = (Button) findViewById(R.id.buttonExtended);
		// _extendedTrackingButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// if (stopExtendedTracking())
		// _extendedTrackingButton.setVisibility(View.INVISIBLE);
		// }
		// });

		// gestures
		_gestures = new Gestures(this, this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_with_drawer_layout);

		UpdateActionBar(this);
		PrepareDrawerMenu(savedInstanceState);

		// Custom targets
		loadTargets();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (_drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_statadd:
			Log.i(LOGTAG, "action_statadd");
			return true;

		case R.id.action_http_get:
			Log.i(LOGTAG, "action_http_get");
			return true;

		case R.id.action_sqlite_query:
			Log.i(LOGTAG, "action_sqlite_query");
			return true;

		case R.id.action_about:
			Log.i(LOGTAG, "action_about");
			return true;

		case R.id.action_settings:
			Log.i(LOGTAG, "action_settings");
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
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
		if (null != _gestures)
			_gestures.onTouchEvent(event);

		return super.onTouchEvent(event);
	}

	// @Override
	public void loadTextures() {
		List<String> files = new ArrayList<String>();
		files.add("images/wikipedia_mask.png");
		files.add("images/www_icon.png");

		_arObjectsMediator.loadTextures(files, getAssets());

		// FIXME: hardcode !!!
		_arObjectsMediator.addTextTexture("abc");
	}

	@Override
	public void updateActiveARObjects(Set<String> trackablesName) {
		super.updateActiveARObjects(trackablesName);

		// Extended tracking
		if (startExtendedTrackingIfNeeded(trackablesName)
				&& (null != _extendedTrackingButton)) {
			runOnUiThread(new Runnable() {
				public void run() {
					_extendedTrackingButton.setVisibility(View.VISIBLE);
				}
			});
		}

	}

	@Override
	public boolean onGesture(GestureInfo aGestureInfo) {
		boolean result = super.onGesture(aGestureInfo);

		return _arObjectsMediator.onGesture(aGestureInfo) || result;
	}

	@Override
	public void onTargetTrack(Trackable arg0) {
		super.onTargetTrack(arg0);
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
					"com.lwiwbuch/.ActivitySplashScreen"); // .ActivitySplashScreen");
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
		new AlertDialog.Builder(this)
				.setTitle("About")
				.setMessage("Augmented Reality test app. Made by (r)Eleks")
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								// continue with delete
							}
						}).setIcon(android.R.drawable.ic_dialog_info).show();
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

	public void prepateToTakeAPicture(int id) {
		_lastTakePictureId = id;
		_cameraFragment.prepareToTakeAPicture(id);
	}

	@Override
	public void targetCreated() {
		super.targetCreated();
		Log.i(LOGTAG, "Target created " + _lastTargetName);

		if ((_lastTakePictureId >= 0)
				&& (_lastTakePictureId < mTargetsList.length)) {
			ARModule arModule = _arObjectsMediator.getModule();
			ARObjectManagement mngmnt = mTargetsList[_lastTakePictureId]
					.getARObjectManagement(this, _arObjectsMediator);
			arModule.addARObjectManagement(_lastTargetName, mngmnt);

			Log.i(LOGTAG, "ARObjectManagement created " + _lastTargetName);
			_lastTakePictureId = -1;
		}
	}

}
