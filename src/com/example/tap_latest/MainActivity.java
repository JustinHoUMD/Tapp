package com.example.tap_latest;

import android.os.Bundle;

import android.content.Intent;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private static final String LOG_TAG = "debugger";

	private Button getContactInfo;
	private Button receiveContactInfo;

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

		getContactInfo = (Button) findViewById(R.id.bDisplayContactQR);
		receiveContactInfo = (Button) findViewById(R.id.bReceiveContactQR);

		getContactInfo.setOnClickListener(this);
		receiveContactInfo.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bDisplayContactQR:
			// Intent encodeIntent = new Intent(this,DisplayContactQR.class);
			Intent encodeIntent = new Intent(
					"android.intent.action.DISPLAYCONTACTQR");
			startActivity(encodeIntent);
			break;
		case R.id.bReceiveContactQR:
<<<<<<< HEAD
			Intent receiveIntent = new Intent(
					"android.intent.action.RECEIVECONTACT");
			// new Intent(this,ReceiveContact.class);
			// startActivityForResult(receiveIntent, 0);
			startActivity(receiveIntent);
			break;
		}
=======
			Intent receiveIntent = new Intent(this,ReceiveContact.class);
			startActivityForResult(receiveIntent, 0);
			break;		
		}		
	}
	/*
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			Bundle basket = data.getExtras();
			String qrResult = basket.getString("qrDecodeString");
			//format of received String
			//Name:Person's Name,Phone:999999999,Email:abc@example.com;
			
			// parsing the received String containing the contact info
			String parsedName, parsedNumber, parsedEmail;
			int index;
			// parsing name
			index = qrResult.indexOf("Name:");
			index+=5;
			parsedName ="";
			while(qrResult.charAt(index) != ','){
				parsedName += qrResult.charAt(index);
				index++;
			}
			
			//parsing  Phone number
			index = qrResult.indexOf("Phone:");
			index+=6;
			parsedNumber = "";
			while(qrResult.charAt(index) != ','){
				parsedNumber += qrResult.charAt(index);
				index++;
			}
			
			//parsing email
			index = qrResult.indexOf("Email:");
			index+=6;
			parsedEmail="";
			while(index<qrResult.length()){
				parsedEmail+=qrResult.charAt(index);
				index++;
			}			
			
			String toastString ="";
			// adding contact info to phone and checking if contact already
			// exists
			if(!contactExists(parsedNumber)){
				ContactInfo.createContact(parsedName, parsedNumber, null, null, parsedEmail, this.getApplication());
				toastString = "Added " + parsedName + " to contact list";
			}else{
				toastString = "Contact " + parsedName +" with phone number:" 
						+ parsedNumber + " already exist!";
			}
			
			//creating Toast When Contact is created			
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(getApplicationContext(), toastString, duration);
			toast.show();			
		}
	}*/
	
	// checks if a specific contact already exists
	private boolean contactExists(String phoneNumber){
		Context context = getApplicationContext();
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, 
							Uri.encode(phoneNumber));
		String[] mPhoneNumberProjection = { PhoneLookup._ID, PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME };
		Cursor cur = context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
		
		try {
			if (cur.moveToFirst()) {
			  return true;
			}
			} finally {
			if (cur != null)
			cur.close();
			}
		return false;
	}
		
		
		
	}
>>>>>>> 299826f55958e909f73cd8404b9396bedfa81663

	}

}
