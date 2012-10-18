package com.fitivity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class TabBarActivity extends TabActivity implements OnTabChangeListener {
	/** Called when the activity is first created. */

	TabHost tabHost;
	int previousTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(this);

		TabHost.TabSpec spec;
		Intent intent;
		Resources res = getResources(); // Resource object to get Drawables

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, FeedActivity.class);
		spec = tabHost.newTabSpec("First")
				.setIndicator("", res.getDrawable(R.drawable.first_tab))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, CreateActivityActivity.class);
		spec = tabHost.newTabSpec("Second")
				.setIndicator("", res.getDrawable(R.drawable.second_tab))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ProfileActivity.class);
		spec = tabHost.newTabSpec("Third")
				.setIndicator("", res.getDrawable(R.drawable.third_tab))
				.setContent(intent);
		tabHost.addTab(spec);

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildAt(i)
					.setBackgroundColor(Color.TRANSPARENT);
		}

		tabHost.getTabWidget().setCurrentTab(0);
		tabHost.getTabWidget().getChildAt(0)
				.setBackgroundColor(Color.TRANSPARENT);
		previousTab = 0;

		// Make middle tab wider
		tabHost.getTabWidget().getChildAt(0).getLayoutParams().width = 159;
		tabHost.getTabWidget().getChildAt(1).getLayoutParams().width = 278;
		tabHost.getTabWidget().getChildAt(2).getLayoutParams().width = 159;

		tabHost.getTabWidget().getChildAt(0).getLayoutParams().height = 60;
		tabHost.getTabWidget().getChildAt(1).getLayoutParams().height = 60;
		tabHost.getTabWidget().getChildAt(2).getLayoutParams().height = 60;
	}

	public void onTabChanged(String tabId) {
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildAt(i)
					.setBackgroundColor(Color.TRANSPARENT);
		}

		tabHost.getTabWidget().getChildAt(tabHost.getCurrentTab())
				.setBackgroundColor(Color.TRANSPARENT);
		previousTab = tabHost.getCurrentTab();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}