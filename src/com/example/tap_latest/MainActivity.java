package com.example.tap_latest;

import android.os.Bundle;

import android.content.Intent;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity {

	private static final String LOG_TAG = "debugger";
	private MainFragment mainFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "Begin");
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			// Add the fragment on initial activity setup
			Log.i(LOG_TAG, "Inside If Statement");
			mainFragment = new MainFragment();
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, mainFragment).commit();
		} else {
			Log.i(LOG_TAG, "INSIDE ELSE");
			// Or set the fragment from restored state info
			mainFragment = (MainFragment) getSupportFragmentManager()
					.findFragmentById(android.R.id.content);
			
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	public void qrButtonsClick(View v){
		switch (v.getId()) {
		case R.id.bDisplayContactQR:			
			Intent encodeIntent = new Intent(
					"android.intent.action.DISPLAYCONTACTQR");
			startActivity(encodeIntent);
			break;
		case R.id.bReceiveContactQR:
			Intent receiveIntent = new Intent("android.intent.action.RECEIVECONTACT");			
			startActivity(receiveIntent);
			break;
		}
	}

	

}
