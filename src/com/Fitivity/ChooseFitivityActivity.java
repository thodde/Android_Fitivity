package com.fitivity;

import java.util.ArrayList;
import java.util.List;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.util.Log;
import android.view.*;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.Toast;

public class ChooseFitivityActivity extends Activity{
	
	private ExpandableListAdapter adapter;
	public  ExpandableListView listView;
    public  ArrayList<ArrayList<Fitivity>> categories;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_layout);
        
        
        Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl"); 
        // Retrieve the ExpandableListView from the layout
      listView = (ExpandableListView) findViewById(R.id.listView);
        
        listView.setOnChildClickListener(new OnChildClickListener()
        {
            
            public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2, int arg3, long arg4)
            {
                Toast.makeText(getBaseContext(), "Child clicked", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        
        listView.setOnGroupClickListener(new OnGroupClickListener()
        {
            
            public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2, long arg3)
            {
                Toast.makeText(getBaseContext(), "Group clicked", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        
     // Initialize the adapter with blank groups and children
	        // We will be adding children on a thread, and then update the ListView
	        //adapter = new ExpandableListAdapter(this, new ArrayList<String>(), new ArrayList<ArrayList<Fitivity>>());
	        
        categories = new ArrayList<ArrayList<Fitivity>>();

       try {
		getActivities();
		
		 //adapter = new ExpandableListAdapter(this, new ArrayList<String>(),
		         //categories);

		 // Set this blank adapter to the list view
		 listView.setAdapter(adapter);
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       
    // Initialize the adapter with blank groups and children
       // We will be adding children on a thread, and then update the ListView
       

      
    }

   public void getActivities () throws ParseException
   {
	   ParseQuery query = new ParseQuery("Activity");
	   
	   List<ParseObject> activityList = query.find();
	   
	   ArrayList<Fitivity> running = new ArrayList<Fitivity>();
       ArrayList<Fitivity> sports = new ArrayList<Fitivity>();
       ArrayList<Fitivity> conditioning = new ArrayList<Fitivity>();
          
           
       
          for (int i = 0; i < activityList.size() - 1 ; i++) {
          	ParseObject element = activityList.get(i);
          	Fitivity fitivity = new Fitivity();
          	fitivity.activityCategory = element.getString("category");
       	fitivity.activitySelectionScreenName = element.getString("name");
          	
       	
          	if (fitivity.activityCategory.equalsIgnoreCase("Sports") ) {
          		
          		
          		sports.add(fitivity);
          		
          	}
          	
          	if (fitivity.activityCategory.equalsIgnoreCase("Running") ) {
          		running.add(fitivity);
          		
          	}
          	
          	if (fitivity.activityCategory.equalsIgnoreCase("Conditioning")) {
          		conditioning.add(fitivity);
          	}
          	
          	
          }
          
   categories.add(running);
   categories.add(sports);
   categories.add(conditioning);
       
 
   /*
   	query.findInBackground(new FindCallback() {
   	    public void done(List<ParseObject> activityList, ParseException e) {
   	        if (e == null) {
   	            Log.d("score", "Retrieved " + activityList.size() + " activities");
   	            
   	            ArrayList<Fitivity> running = new ArrayList<Fitivity>();
	            ArrayList<Fitivity> sports = new ArrayList<Fitivity>();
	            ArrayList<Fitivity> conditioning = new ArrayList<Fitivity>();
   	            
   	             
   	         
   	            for (int i = 0; i < activityList.size() - 1 ; i++) {
   	            	ParseObject element = activityList.get(i);
   	            	Fitivity fitivity = new Fitivity();
   	            	fitivity.activityCategory = element.getString("activityCategory");
	            	fitivity.activitySelectionScreenName = element.getString("activitySelectionScreenName");
   	            	
	            	
   	            	if (fitivity.activityCategory.equalsIgnoreCase("Sports") ) {
   	            		
   	            		
   	            		sports.add(fitivity);
   	            		
   	            	}
   	            	
   	            	if (fitivity.activityCategory.equalsIgnoreCase("Running") ) {
   	            		running.add(fitivity);
   	            		
   	            	}
   	            	
   	            	if (fitivity.activityCategory.equalsIgnoreCase("Conditioning")) {
   	            		conditioning.add(fitivity);
   	            	}
   	            	
   	            	
   	            }
   	            
   	       categories.add(running);
     	   categories.add(sports);
     	   categories.add(conditioning);
   	         
     	 

          // Set this blank adapter to the list view
          listView.setAdapter(adapter);
   	         
   	         
   	          
   	            
   	        } else {
   	            Log.d("score", "Error: " + e.getMessage());
   	        }
   	    }
   	});
   	*/
   }

  
}