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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

public class DisplayContactQR extends Activity {

	private ImageView qrImage;
	private String ContactData;
    private String facebookId; 
    private static final String LOG_TAG = "debugger";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_qr);

		qrImage = (ImageView) findViewById(R.id.ivQRcode);
		ContactData = generateContactInfo();
		drawQRCode(ContactData);
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
			qrImage.setImageBitmap(bm);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String generateContactInfo() {
		String name, email, phoneNumber;
		String finalData;

		name = ContactInfo.getName(this.getApplication());
		email = ContactInfo.getEmail(this);
		phoneNumber = ContactInfo.requestCurrPhoneNum(this.getApplication());
		
		//get facebook ID
		makeMeRequest(Session.getActiveSession()); 

		finalData = "Name:" + name + " Phone:" + phoneNumber
				+ " Email:" + email + " FacebookId:" + facebookId;
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
	                    Log.i(LOG_TAG, "Facebook ID: "+facebookId);
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
