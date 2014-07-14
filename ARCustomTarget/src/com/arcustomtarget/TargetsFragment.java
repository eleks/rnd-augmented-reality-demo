package com.arcustomtarget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.arcustomtarget.core.TargetsListArrayAdapter;
import com.arcustomtarget.core.TargetsListItem;

public class TargetsFragment extends Fragment {
	private String LOGTAG = "TargetsFragment";
	public static final String ARG_NUMBER = "menu_item_number";

	private ListView _targetsListView;
	private TargetsListItem[] _targetsList;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.second_fragment, container,
				false);
		// int i = getArguments().getInt(ARG_NUMBER);
		String menuItemName = "Some name";
		// getResources().getStringArray(
		// R.array.menu_drawer_array)[i];

		getActivity().setTitle(menuItemName);

		// targets
		TargetsListItem item1 = new TargetsListItem("abcd");
		TargetsListItem item2 = new TargetsListItem("abcd 2");
		TargetsListItem item3 = new TargetsListItem("abcd 3");

		_targetsList = new TargetsListItem[] { item1, item2, item3 };

		_targetsListView = (ListView) rootView.findViewById(R.id.targets_list);
		_targetsListView.setAdapter(new TargetsListArrayAdapter(getActivity(),
				_targetsList));
		_targetsListView.setOnItemClickListener(new TargetClickListener());

		return rootView;
	}

	class TargetClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.i(LOGTAG, "onItemClick: " + position);
		}
	}

}
