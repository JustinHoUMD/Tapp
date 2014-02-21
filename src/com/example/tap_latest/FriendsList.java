package com.example.tap_latest;

import java.util.ArrayList;
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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class FriendsList extends Activity {
	private static final String LOG_TAG = "debugger";
	private List<MyFacebookFriend> myFriends = new ArrayList<MyFacebookFriend>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_friends);
		makeMeRequest(Session.getActiveSession());			
		
		
	}
	
	
	private void populateListView() {
		ArrayAdapter<MyFacebookFriend> adapter = new MyListAdapter();
		ListView list = (ListView)findViewById(R.id.lvFriendsListView);
		list.setAdapter(adapter);		
	}


	


	private void makeMeRequest(final Session session) {
	    Request request = Request.newMeRequest(session, 
	            new Request.GraphUserCallback() {

	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                	requestMyAppFacebookFriends(session);
	                }
	            }
	            if (response.getError() != null) {
	            	Log.i(LOG_TAG, "ERROR while getting Facebook ID");	              	     	
	            }
	        }
	    });
	   request.executeAsync();
	   
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
	            for(GraphUser user : friends){
	            	if(user.getProperty("installed") != null){
	            	    boolean installed = (Boolean) user.getProperty("installed");
	            	    if(installed){
	            	    	Log.d("APP USER FOUND!", user.getName());
	            	    	myFriends.add(new MyFacebookFriend(user.getName(), user.getId()));
	            	    }
	            	}	            	
	            }
	            Log.d("POPULATE LIST!","populating list!");
	            populateListView();
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
	
	
	//inner class for list adapter
	private class MyListAdapter extends ArrayAdapter<MyFacebookFriend>{
		public MyListAdapter(){
			super(FriendsList.this,R.layout.item_view,myFriends);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			
			// Make sure we have a view to work with
			View itemView = convertView;
			if(itemView == null){
				itemView = getLayoutInflater().inflate(R.layout.item_view, parent,false);
			}
		
			
			//Setting the image
			MyFacebookFriend currentFriend = myFriends.get(position);
			ImageView imageView = (ImageView)itemView.findViewById(R.id.ivProfilePicture);
			Bitmap profilePicture = currentFriend.obtainProfilePicture();
			if(imageView!= null && profilePicture != null){
				imageView.setImageBitmap(profilePicture);
			}
			
			
			//Setting the name
			TextView nameText = (TextView)itemView.findViewById(R.id.tvFacebookName);
			nameText.setText(currentFriend.getName());
			
			return itemView;			
		}
		
		
		
		
	}

}
