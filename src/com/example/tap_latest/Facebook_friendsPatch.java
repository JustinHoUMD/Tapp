package com.example.tap_latest;

import android.content.Context;
import android.os.Bundle;

import com.facebook.android.Facebook;

public class Facebook_friendsPatch extends Facebook {

	@SuppressWarnings("deprecation")
	public Facebook_friendsPatch(String appId) {
		super(appId);
		
	}
	
	 public void dialog(Context context, String action, Bundle parameters,
             final DialogListener listener) {

      } 


}
