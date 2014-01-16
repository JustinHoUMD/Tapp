package com.example.tap_latest;

import java.util.Scanner;
import java.util.regex.MatchResult;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;


import android.app.Activity;
import android.content.Context;
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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReceiveContact extends Activity {
	
	private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;      

    TextView scanText;
    

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

        scanText = (TextView)findViewById(R.id.scanText);

        /*
        scanButton = (Button)findViewById(R.id.ScanButton);

        scanButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (barcodeScanned) {
                        barcodeScanned = false;
                        scanText.setText("Scanning...");
                        mCamera.setPreviewCallback(previewCb);
                        mCamera.startPreview();
                        previewing = true;
                        mCamera.autoFocus(autoFocusCB);
                    }
                }
            });
         */
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
		String parsedName, parsedNumber, parsedEmail,parsedFacebookId;
		Scanner s = new Scanner(qrResult).useDelimiter("Name:(\\w+) Phone:(\\w+) Email:(\\w+)*@(\\w+)*.(\\w+) FacebookId:(\\w+)");
		MatchResult result = s.match();
		parsedName = result.group(1);
		parsedNumber = result.group(2);
		parsedEmail = result.group(3)+"@"+result.group(4)+"."+result.group(5);
		parsedFacebookId = result.group(6);
		
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
		
		
		
		Bundle parameters = new Bundle();
		parameters.putString("id", parsedFacebookId);
		WebDialog requestsDialog = (
		        new WebDialog.RequestsDialogBuilder(this,
		                Session.getActiveSession(),
		                parameters))
		                .build();
		requestsDialog.show();
		
		//facebook.dialog(this, "friends", parameters, this);
    	
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


 


}
