package com.example.tap_latest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MyFacebookFriend {
	String name;
	String facebookID;
	Bitmap profilePicture;

	public MyFacebookFriend(String name, String facebookID) {
		this.name = name;
		this.facebookID = facebookID;		
	}

	public Bitmap obtainProfilePicture() {		
		Thread thread = new Thread(new Runnable() {		

			@Override
			public void run() {
				String urlString = "https://graph.facebook.com/" + facebookID
						+ "/picture?width=125&height=125";
				try {
					URL url = new URL(urlString);
					InputStream is = (InputStream) url.getContent();
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					profilePicture = bitmap;
					if(profilePicture != null){
						Log.d("PROFILE PICTURE!", "Picture download successful!");
					}else{
						Log.d("PROFILE PICTURE!", "Picture download failed");
					}
				} catch (MalformedURLException e) {
					Log.d("PROFILE PICTURE!", "Picture download failed!");
					profilePicture = null;
					e.printStackTrace();
				} catch (IOException e) {
					Log.d("PROFILE PICTURE!", "Picture download failed!");
					profilePicture = null;
					e.printStackTrace();
				}
				
			}
		});
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return profilePicture;
		
	
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFacebookID() {
		return facebookID;
	}

	public void setFacebookID(String facebookID) {
		this.facebookID = facebookID;
	}

	public Bitmap getProfilePicture() {
		return profilePicture;
	}

}
