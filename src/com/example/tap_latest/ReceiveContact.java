package com.example.tap_latest;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

import com.facebook.FacebookException;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiveContact extends Activity implements OnClickListener{
	
	private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;   
    private Button scanButton;

    private static final String LOG_TAG = "debugger";
    

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;
    private String facebookId; 
    private String parsedFacebookId; 

    static {
        System.loadLibrary("iconv");
    } 

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.receive_contact);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        

        
        scanButton = (Button)findViewById(R.id.ScanButton);
        scanButton.setOnClickListener(this);
        
        Parse.initialize(this, "BDODZz6eR9mhRqc1R4Z6Jvf3IS17tW4nQGxSfMqm", "TTPu2E4r953VnfGqARboNOEB2czPegLsmS6YkFgU");
      
         
    }
    
    @Override
    public void onClick(View v) {
    	switch(v.getId()){
    	case R.id.ScanButton:
    		  if (barcodeScanned) {
                  barcodeScanned = false;                 
                  mCamera.setPreviewCallback(previewCb);
                  mCamera.startPreview();
                  previewing = true;
                  mCamera.autoFocus(autoFocusCB);
              }
    		break;
    	}
    	
    }

    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
            public void run() {
                if (previewing)
                    mCamera.autoFocus(autoFocusCB);
            }
        };

    PreviewCallback previewCb = new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);
                
                if (result != 0) {
                    previewing = false;
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    
                    SymbolSet syms = scanner.getResults();
                    String QRResult = "";
                    for (Symbol sym : syms) {                        
                        QRResult += sym.getData();
                        barcodeScanned = true;
                    }
                    // sending string back to main activity
                    if(barcodeScanned == true){
                    	addContact( QRResult);
                    	//Intent i = new Intent("android.intent.action.MAIN");
                    	//startActivity(i);
                    }
                }
            }
        };

    // Mimic continuous auto-focusing
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                autoFocusHandler.postDelayed(doAutoFocus, 1000);
            }
        };
    
 
	private void addContact(String qrResult){
    	//format of received String
		//Name:Person's Name Phone:999999999 Email:abc@example.com FacebookId: id;
		
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
		while(qrResult.charAt(index) != ','){
			parsedEmail+=qrResult.charAt(index);
			index++;
		}	
		
		index = qrResult.indexOf("FacebookId:");
		index+= 11;
		parsedFacebookId="";
		while(index<qrResult.length()){
			parsedFacebookId += qrResult.charAt(index);
			index++;
		}
		
		
		
		String toastString ="";
		// adding contact info to phone and checking if contact already
		// exists
		if(!contactExists(parsedNumber)){
			
			//get facebook ID
			makeMeRequest(Session.getActiveSession()); 
			
			
			
			ContactInfo.createContact(parsedName, parsedNumber, null, null, parsedEmail, this.getApplication());
			toastString = "Added " + parsedName + " to contact list, FacebookId: " + parsedFacebookId;
		}else{
			toastString = "Contact " + parsedName +" with phone number:" 
					+ parsedNumber + " already exist!,  FacebookId: " + parsedFacebookId;
		}
		
					
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(getApplicationContext(), toastString, duration);
		toast.show();		
		
		
		/*
		Bundle parameters = new Bundle();
		parameters.putString("id", parsedFacebookId);
		Facebook f = new Facebook("1440481899501016"); 
		f.dialog(this, "friends", parameters, this);		
		//facebook.dialog(this, "friends", parameters, this);
		 * 
		 */
		
		 //Intent facebookIntent = new Intent("android.intent.action.VIEW",Uri.parse("https://www.facebook.com/"+parsedFacebookId));
		 //startActivity(facebookIntent);
		
		showFBDialog(parsedFacebookId,parsedName);
		 
		 // works until the friend reqeust is sent
		 /*
		 webview.loadUrl("http://www.facebook.com/dialog/friends/?"+
				  "id=" + parsedFacebookId + "&"+
				  "app_id=1440481899501016&"+
				  "redirect_uri=https://www.facebook.com/connect/login_success.html");
		*/
		
    	
    }
    
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

 	private void makeMeRequest(final Session session) {
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {

	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                    facebookId = user.getId(); 
	                    Log.i(LOG_TAG, "Facebook ID2: "+facebookId);
	                    ParseQuery<ParseObject> query = ParseQuery.getQuery("MeetCount");
	        			query.whereEqualTo("FacebookID", facebookId);
	        			query.getFirstInBackground(new GetCallback<ParseObject>() {
	        			  public void done(ParseObject object, ParseException e) {
	        			    if (object == null) {
	        			      ParseObject newPerson = new ParseObject("MeetCount"); 
	        			      newPerson.put("FacebookID", facebookId); 
	        			      newPerson.put("numberMet", 1); 
	        			      newPerson.saveInBackground();
	        			    } else {
	        			      //already exists 
	        			    	object.increment("numberMet");
	        			    	object.saveInBackground();
	        			    }
	        			  }
	        			});
	        			
	        			ParseQuery<ParseObject> query2 = ParseQuery.getQuery("MeetCount");
	        			query2.whereEqualTo("FacebookID", parsedFacebookId);
	        			query2.getFirstInBackground(new GetCallback<ParseObject>() {
	        			  public void done(ParseObject object, ParseException e) {
	        			    if (object == null) {
	        			      ParseObject newPerson = new ParseObject("MeetCount"); 
	        			      newPerson.put("FacebookID", parsedFacebookId); 
	        			      newPerson.put("numberMet", 1); 
	        			      newPerson.saveInBackground();
	        			    } else {
	        			      //already exists 
	        			    	object.increment("numberMet");
	        			    	object.saveInBackground();
	        			    }
	        			  }
	        			});
	                }
	            }
	            if (response.getError() != null) {
	            	Log.i(LOG_TAG, "ERROR while getting Facebook ID");
	            }
	        }
	    });
	    request.executeAsync();
	} 
 	
 	private void showFBDialog(final String facebookId,String name){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	   builder.setMessage("Do you want to visit " + name + "'s facebook page.")
       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
        	WebView webview = new WebView(getApplicationContext());
      		 setContentView(webview);
      		 webview.loadUrl("http://www.facebook.com/"+facebookId);
           }
       })
       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               
           }
       });
 	  AlertDialog dialog = builder.create();
 	  dialog.show();
 	}

}
