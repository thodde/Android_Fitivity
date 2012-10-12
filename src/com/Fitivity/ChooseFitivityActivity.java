package com.fitivity;

import java.util.ArrayList;
import java.util.HashMap;
import com.parse.ParseObject;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class ChooseFitivityActivity extends ExpandableListActivity {
   
	private final int TOTAL_CATEGORIES = 4;
	ArrayList<ParseObject> categories;
	ExpandableListView list;
	SimpleExpandableListAdapter expListAdapter;
	String chosenActivity;
	
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
    	try {
    		 super.onCreate(savedInstanceState);
    		 setContentView(R.layout.main);
        
             expListAdapter = new SimpleExpandableListAdapter(
					this,
					createGroupList(), 				// Creating group List.
					R.layout.group_row,				// Group item layout XML.			
					new String[] { "category" },	// the key of group item.
					new int[] { R.id.row_name },	// ID of each group item.-Data under the key goes into this TextView.					
					createChildList(),				// childData describes second-level entries.
					R.layout.child_row,				// Layout for sub-level entries(second level).
					new String[] {"subcategory"},	// Keys in childData maps to display.
					new int[] { R.id.grp_child}		// Data under the keys above go into these TextViews.
				);
			setListAdapter( expListAdapter );		// setting the adapter in the list.
    	}
    	catch(Exception e){
    		System.out.println("Errrr +++ " + e.getMessage());
    	}
    }
	
	/* Used for pulling activities out of the database (right now they are hard-coded)
	 * NOT TESTED AT ALL YET
	public void attemptQuery() {
			ParseQuery query = new ParseQuery("Activity");
			//Fetch all of the activities that are in the database
			if (query != null) {
				query.orderByAscending("category");
				query.addAscendingOrder("name");
				query.setLimit(200);
				query.whereExists("category");
				
				query.findInBackground(new FindCallback() {
				  public void done(List<ParseObject> results, ParseException e) {
					    ArrayList<ParseObject> categoryArray = new ArrayList<ParseObject>();
						    if(e == null) {
					    	String lastCategory = "";
					    	boolean firstTry = true;
						    	
					    	for(int i = 0; i <= results.size(); i++) {
					    		if(lastCategory.equalsIgnoreCase(results.get(i).toString())) {
					    			categoryArray.add(results.get(i));
					    		}
					    		else {
					    			if (firstTry) {
										firstTry = false;
									}
									else {
										categories.addAll(categoryArray);
									}
						    			
					    			lastCategory = results.get(i).toString();
					    			categoryArray = null;
					    		}
					    	}
					    	categories.addAll(categoryArray);
							categoryArray = null;
								
							ArrayList<ParseObject> resultsToShow = new ArrayList<ParseObject>();
							for (int i = 0; i < categories.size(); i++) {
								resultsToShow.add(new ParseObject("category"));
							}
						}
				  }
			});
		}
	}
	*/
    
	/* Creating the Hashmap for the row */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList createGroupList() {
	  	  ArrayList result = new ArrayList();
	  	  for( int i = 0 ; i < TOTAL_CATEGORIES ; ++i ) {
	  		  HashMap m = new HashMap();
	  		
	  		  if(i == 0) {
	  			  m.put( "category", "Conditioning");
	  		  }
	  		  else if(i == 1) {
	  			  m.put( "category", "Sports");
	  		  }
	  		  else if(i == 2) {
	  			  m.put( "category", "Running");
	  		  }
	  		  else if(i == 3) {
	  			  m.put( "category", "Recreation");
	  		  }
	  		  result.add( m );
	  	  }
	  	  return result;
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void updateChild(String title, HashMap child, ArrayList secList) {
		child.put( "subcategory", title );
		secList.add( child );
	}
    
	/* creating the HashMap for the children */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private ArrayList createChildList() {
    	ArrayList result = new ArrayList();
    	for( int i = 0 ; i < TOTAL_CATEGORIES ; ++i ) {
    	  /* each group need each HashMap-Here for each group we have 3 subgroups */
    	  ArrayList secList = new ArrayList(); 
    	  //for( int n = 0 ; n < 3 ; n++ ) {
    	  //HashMap child = new HashMap();
    	  if(i == 0) {
    		  updateChild("Abs", new HashMap(), secList);
    		  updateChild("Athleticism", new HashMap(), secList);
    		  updateChild("Boot Camp", new HashMap(), secList);
    		  updateChild("Butt and Thighs", new HashMap(), secList);
    		  updateChild("Cross Fit", new HashMap(), secList);
    		  updateChild("Home Workouts", new HashMap(), secList);	
    		  updateChild("Insanity", new HashMap(), secList);
    		  updateChild("Jump Higher", new HashMap(), secList);
    		  updateChild("Jump Rope", new HashMap(), secList);
    		  updateChild("P90X", new HashMap(), secList);
    		  updateChild("Pilates", new HashMap(), secList);
    		  updateChild("Ripped Body", new HashMap(), secList);
    		  updateChild("Spin", new HashMap(), secList);
    		  updateChild("Toned Body", new HashMap(), secList);
    		  updateChild("Walking", new HashMap(), secList);
    		  updateChild("Weight Loss", new HashMap(), secList);
    		  updateChild("Woman's Workouts", new HashMap(), secList);
    		  updateChild("Yoga", new HashMap(), secList);
    		  updateChild("Zumba", new HashMap(), secList);
    	  }
    	  else if(i == 1) {
    		  updateChild("Baseball", new HashMap(), secList);
    		  updateChild("Basketball", new HashMap(), secList);
    		  updateChild("Boxing", new HashMap(), secList);
    		  updateChild("Dancing", new HashMap(), secList);
    		  updateChild("Field Hockey", new HashMap(), secList);
    		  updateChild("Football", new HashMap(), secList);
    		  updateChild("Golf", new HashMap(), secList);
    		  updateChild("Gymnastics", new HashMap(), secList);
    		  updateChild("Hockey", new HashMap(), secList);
    		  updateChild("Lacrosse", new HashMap(), secList);
    		  updateChild("MMA", new HashMap(), secList);
    		  updateChild("Martial Arts", new HashMap(), secList);
    		  updateChild("Racquetball", new HashMap(), secList);
    		  updateChild("Roller Hockey", new HashMap(), secList);
    		  updateChild("Rowing", new HashMap(), secList);
    		  updateChild("Rugby", new HashMap(), secList);
    		  updateChild("Skiing", new HashMap(), secList);
    		  updateChild("Snowboarding", new HashMap(), secList);
    		  updateChild("Soccer", new HashMap(), secList);
    		  updateChild("Softball", new HashMap(), secList);
    		  updateChild("Squash", new HashMap(), secList);
    		  updateChild("Surfing", new HashMap(), secList);
    		  updateChild("Swimming", new HashMap(), secList);
    		  updateChild("Tennis", new HashMap(), secList);
    		  updateChild("Track and Field", new HashMap(), secList);
    		  updateChild("Triathlon", new HashMap(), secList);
    		  updateChild("Ultimate Frisbee", new HashMap(), secList);
    		  updateChild("Vollyball", new HashMap(), secList);
    		  updateChild("Wrestling", new HashMap(), secList);
      	  }
    	  else if(i == 2) {
    		  updateChild("10K Training", new HashMap(), secList);
    		  updateChild("5K Training", new HashMap(), secList);
    		  updateChild("Jogging", new HashMap(), secList);
    		  updateChild("Marathon Training", new HashMap(), secList);
    		  updateChild("Quickness and Speed", new HashMap(), secList);
    		  updateChild("Run Faster", new HashMap(), secList);
      	  }
    	  else if(i == 3) {
    		  updateChild("Badminton", new HashMap(), secList);
    		  updateChild("Billiards", new HashMap(), secList);
    		  updateChild("Boating", new HashMap(), secList);
    		  updateChild("Bowling", new HashMap(), secList);
    		  updateChild("Camping", new HashMap(), secList);
    		  updateChild("Capture the Flag", new HashMap(), secList);
    		  updateChild("Cricket", new HashMap(), secList);
    		  updateChild("Dodgeball", new HashMap(), secList);
    		  updateChild("Dog Walking", new HashMap(), secList);
    		  updateChild("Fishing", new HashMap(), secList);
    		  updateChild("Flag Football", new HashMap(), secList);
    		  updateChild("Floor Hockey", new HashMap(), secList);
    		  updateChild("Hiking", new HashMap(), secList);
    		  updateChild("Horseback Riding", new HashMap(), secList);
    		  updateChild("Hunting", new HashMap(), secList);
    		  updateChild("Kayaking", new HashMap(), secList);
    		  updateChild("Kick Ball", new HashMap(), secList);
    		  updateChild("Motocross", new HashMap(), secList);
    		  updateChild("Paintball", new HashMap(), secList);
    		  updateChild("Ping Pong", new HashMap(), secList);
    		  updateChild("Rock Climbing", new HashMap(), secList);
    		  updateChild("Roller Blading", new HashMap(), secList);
    		  updateChild("Sailing", new HashMap(), secList);
    		  updateChild("Skateboarding", new HashMap(), secList);
    		  updateChild("Sledding", new HashMap(), secList);
    		  updateChild("Snorkeling", new HashMap(), secList);
    		  updateChild("Wall Ball", new HashMap(), secList);
    		  updateChild("Water Skiing", new HashMap(), secList);
    		  updateChild("White Water Rafting", new HashMap(), secList);
         }
    	  
    	  //secList.add( child );
    	  //}
    	 result.add( secList );
    	}    	 
    	return result;
    }
    
    public void onContentChanged() {
	    super.onContentChanged();	      
    }
    
    /* This function is called on each child click */
    public boolean onChildClick( ExpandableListView parent, View v, int groupPosition,int childPosition,long id) {
    	Object o = (Object)expListAdapter.getChild(groupPosition, childPosition);
		chosenActivity = o.toString();
	    Intent returnIntent = new Intent();
	    returnIntent.putExtra("activity", chosenActivity);
	    setResult(RESULT_OK, returnIntent);        
		finish();
		return true;
    }

    /* This function is called on expansion of the group */
    public void onGroupExpand(int groupPosition) {
    	try {
    		 int len = expListAdapter.getGroupCount();
 	         for(int i=0; i<len; i++) {
 	             if(i != groupPosition) {
 	                 list.collapseGroup(i);
 	             }
 	         }
    	}
    	catch(Exception e) {
    		System.out.println(" groupPosition Error +++ " + e.getMessage());
    	}
    }  
}