package com.example.tap_latest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphMultiResult;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphObjectList;
import com.facebook.model.GraphUser;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class FriendsList extends Activity {
	private static final String LOG_TAG = "debugger";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		
	}
	
	
	private void makeMeRequest(final Session session) {
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {

	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                   
	                }
	            }
	            if (response.getError() != null) {
	            	Log.i(LOG_TAG, "ERROR while getting Facebook ID");	              	     	
	            }
	        }
	    });
	   request.executeAsync();
	   
	} 
	
	private void StartFacebookRequest(){
		
		
	}
	
	private Request createRequest(Session session) {
	    Request request = Request.newGraphPathRequest(session, "me/friends", null);

	    Set<String> fields = new HashSet<String>();
	    String[] requiredFields = new String[] { "id", "name", "picture",
	            "installed" };
	    fields.addAll(Arrays.asList(requiredFields));

	    Bundle parameters = request.getParameters();
	    parameters.putString("fields", TextUtils.join(",", fields));
	    request.setParameters(parameters);

	    return request;
	}
	
	private void requestMyAppFacebookFriends(Session session) {
	    Request friendsRequest = createRequest(session);
	    friendsRequest.setCallback(new Request.Callback() {

	        @Override
	        public void onCompleted(Response response) {
	            List<GraphUser> friends = getResults(response);
	            // TODO: your code here
	        }
	    });
	    friendsRequest.executeAsync();
	}
	private List<GraphUser> getResults(Response response) {
	    GraphMultiResult multiResult = response
	            .getGraphObjectAs(GraphMultiResult.class);
	    GraphObjectList<GraphObject> data = multiResult.getData();
	    return data.castToListOf(GraphUser.class);
	}

}
