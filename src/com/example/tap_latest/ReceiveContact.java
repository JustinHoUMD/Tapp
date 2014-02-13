package com.example.tap_latest;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiveContact extends Activity implements OnClickListener {
	
	private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;   
    private Button scanButton;
    private String myFacebookId; 
    private String parsedFacebookId; 
    private static final String SUCCESS_MESSAGE = "SUCCESS";
    private static final String FAIL_MESSAGE = "FAIL";  
    private static final String LOG_TAG = "debugger";
    

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

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
    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	restoreCamera();
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
    
    private void restoreCamera(){
    	if (mCamera == null) {
   		 mCamera = getCameraInstance(); // Local method to handle camera init
   		 previewing = true;
   		 mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
   	     FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
   	     preview.addView(mPreview);
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
                   
                    if(barcodeScanned == true){
                    	addContact( QRResult);
                    	
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
		String parsedName = "", parsedNumber = "", parsedEmail = "";

		
		try {
			JSONObject jsonString = new JSONObject(qrResult);
			parsedName = jsonString.getString("name");
			Log.d("Name",parsedName);
			parsedNumber = jsonString.getString("phone");
			parsedEmail = jsonString.getString("email");
			String status = jsonString.getString("loginStatus");
			Log.d("FACEBOOK",status);
			if(status.equals(SUCCESS_MESSAGE)){
				parsedFacebookId = jsonString.getString("facebookId");
				Log.d("FACEBOOK ID",parsedFacebookId);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		String toastString ="";
		// adding contact info to phone and checking if contact already
		// exists
		if(!contactExists(parsedNumber)){			
			ContactInfo.createContact(parsedName, parsedNumber, null, null, parsedEmail, this.getApplication());
			toastString = "Added " + parsedName + " to contact list";
		}else{
			toastString = "Contact " + parsedName +" with phone number:" 
					+ parsedNumber + " already exists!";
		}
		
					
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(getApplicationContext(), toastString, duration);
		toast.show();		
		
		if(parsedFacebookId != null){
			makeMeRequest(Session.getActiveSession()); 
			showFBDialog(parsedFacebookId, parsedName);
			
			
		}    	
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
 	
 	private void showFBDialog(final String facebookId,String name){
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	   builder.setMessage("Do you want to visit " + name + "'s facebook page.")
       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {     		
        	// launch facebook app with user's profile
        	String uri = "fb://profile/" + facebookId ;
        	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        	startActivity(intent);
           }
       })
       .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               
           }
       });
 	   builder.create();
 	   builder.show();
 	}
 	
 	
 	
 	
 	private void makeMeRequest(final Session session) {
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {

	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                    myFacebookId = user.getId(); 
	                    Log.i(LOG_TAG, "Facebook ID2: "+myFacebookId );
	                    ParseQuery<ParseObject> query = ParseQuery.getQuery("MeetCount");
	        			query.whereEqualTo("FacebookID", myFacebookId );
	        			query.getFirstInBackground(new GetCallback<ParseObject>() {
	        			  public void done(ParseObject object, ParseException e) {
	        			    if (object == null) {
	        			      ParseObject newPerson = new ParseObject("MeetCount"); 
	        			      newPerson.put("FacebookID", myFacebookId ); 
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
}
