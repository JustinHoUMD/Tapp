package com.example.tap_latest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayContactQR extends Activity {
	
	private ImageView qrImage;
	private TextView facebookStatusTv;
	private String ContactData;
    private String facebookId; 
    private static final String LOG_TAG = "debugger";
    private static final String SUCCESS_MESSAGE = "SUCCESS";
    private static final String FAIL_MESSAGE = "FAIL";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_qr);
		qrImage = (ImageView) findViewById(R.id.ivQRcode);
		facebookStatusTv = (TextView)findViewById(R.id.tvFacebookStatus);		
		makeMeRequest(Session.getActiveSession()); 
			
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void drawQRCode(String data) {
		Bitmap bm;
		try {
			bm = CreateQRCode(data);
			qrImage.setBackgroundResource(R.drawable.qr_background);
			qrImage.setImageBitmap(bm);
			
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String generateContactInfo(String facebookStatus) {
		String name, email, phoneNumber;
		String finalData;

		name = ContactInfo.getName(this.getApplication());
		email = ContactInfo.getEmail(this);
		phoneNumber = ContactInfo.requestCurrPhoneNum(this.getApplication());
		
		
		Log.d("Debug", "Facebook ID: "+ facebookId);

		finalData = "Name:" + name + ",Phone:" + phoneNumber
				+ ",Email:" + email + ",FacebookId:" + facebookId;
		
		finalData = "{\"name\" : " + "\"" + name + "\"" + ",\"phone\" : " + "\"" + phoneNumber + "\"" + 
				",\"email\" : " + "\"" + email + "\"" + ",\"facebookId\" : " + "\"" + facebookId + "\"" + 
				",\"loginStatus\" : " + "\"" + facebookStatus + "\"" + "}";
		Log.d("Debug", finalData);

		return finalData;
	}

	private Bitmap CreateQRCode(String data) throws WriterException,
			IOException {
		String qrCodeData = data;
		String charset = "ISO-8859-1"; // or "UTF-8"
		int qrCodewidth = 150;
		int qrCodeheight = 150;
		Config conf = Bitmap.Config.RGB_565;
		Bitmap bmp = Bitmap.createBitmap(qrCodewidth, qrCodeheight, conf);

		Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		BitMatrix matrix = new MultiFormatWriter().encode(
				new String(qrCodeData.getBytes(charset), charset),
				BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);

		try {
			for (int x = 0; x < qrCodewidth; x++) {
				for (int y = 0; y < qrCodeheight; y++) {
					bmp.setPixel(x, y, matrix.get(x, y) ? 0xff000000
							: 0xffffffff);
				}
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return bmp;
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
	                    Log.i(LOG_TAG, "Facebook ID QR Generate code: "+facebookId);	                    
	                    facebookStatusTv.setText("Logged into facebook");                 
	                    facebookStatusTv.setBackgroundResource(R.drawable.logged_in);
	                    
	                    ContactData = generateContactInfo(SUCCESS_MESSAGE);
	            		drawQRCode(ContactData);
	                }
	            }
	            if (response.getError() != null) {
	            	facebookStatusTv.setText("Not Logged Into Facebook!!!");
	            	facebookStatusTv.setBackgroundResource(R.drawable.logged_out);
	            	ContactData = generateContactInfo(FAIL_MESSAGE);
	           		drawQRCode(ContactData);
	            	Log.i(LOG_TAG, "ERROR while getting Facebook ID");	            	     	
	            }
	        }
	    });
	   request.executeAsync();
	   // request.executeAndWait();
	} 
	
	private void displayErrorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	 	   builder.setMessage("You are not logged into facebook.Click Cancel to go back or OK to proceed without sharing your facebook info")
	       .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	  ContactData = generateContactInfo(FAIL_MESSAGE);
           		  drawQRCode(ContactData);
	           }
	       })
	       .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               
	           }
	       });
	}

}
	