package com.arcustomtarget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TargetsFragment extends Fragment {
	// private String LOGTAG = "MainFragment";
	public static final String ARG_NUMBER = "menu_item_number";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.second_fragment, container,
				false);
		int i = getArguments().getInt(ARG_NUMBER);
		String menuItemName = getResources().getStringArray(
				R.array.menu_drawer_array)[i];

		getActivity().setTitle(menuItemName);
		return rootView;
	}
}
