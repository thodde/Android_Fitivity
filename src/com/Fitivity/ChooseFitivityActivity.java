package com.fitivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class ChooseFitivityActivity extends ExpandableListActivity {
   
	private final int TOTAL_CATEGORIES = 4;
	
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
    	try{
    		 super.onCreate(savedInstanceState);
    		 setContentView(R.layout.main);
        
             SimpleExpandableListAdapter expListAdapter = new SimpleExpandableListAdapter(
					this,
					createGroupList(), 				// Creating group List.
					R.layout.group_row,				// Group item layout XML.			
					new String[] { "Group Item" },	// the key of group item.
					new int[] { R.id.row_name },	// ID of each group item.-Data under the key goes into this TextView.					
					createChildList(),				// childData describes second-level entries.
					R.layout.child_row,				// Layout for sub-level entries(second level).
					new String[] {"Sub Item"},		// Keys in childData maps to display.
					new int[] { R.id.grp_child}		// Data under the keys above go into these TextViews.
				);
			setListAdapter( expListAdapter );		// setting the adapter in the list.
    	}
    	catch(Exception e){
    		System.out.println("Errrr +++ " + e.getMessage());
    	}
    }
    
	/* Creating the Hashmap for the row */
	@SuppressWarnings("unchecked")
	private List createGroupList() {
	  	  ArrayList result = new ArrayList();
	  	  for( int i = 0 ; i < TOTAL_CATEGORIES ; ++i ) { // TOTAL_CATEGORIES groups........
	  		  HashMap m = new HashMap();
	  		
	  		  if(i == 0) {
	  			  m.put( "Conditioning", "Conditioning"); // the key and it's value.
	  		  }
	  		  else if(i == 1) {
	  			  m.put( "Sports", "Sports"); // the key and it's value.
	  		  }
	  		  else if(i == 2) {
	  			  m.put( "Running", "Running"); // the key and it's value.
	  		  }
	  		  else if(i == 3) {
	  			  m.put( "Recreation", "Recreation"); // the key and it's value.
	  		  }
	  		  result.add( m );
	  	  }
	  	  return (List)result;
    }
    
	/* creating the HashMap for the children */
    @SuppressWarnings("unchecked")
	private List createChildList() {
    	ArrayList result = new ArrayList();
    	for( int i = 0 ; i < TOTAL_CATEGORIES ; ++i ) { // this TOTAL_CATEGORIES is the number of groups(Here it's TOTAL_CATEGORIES)
    	  /* each group need each HashMap-Here for each group we have 3 subgroups */
    	  ArrayList secList = new ArrayList(); 
    	  for( int n = 0 ; n < 3 ; n++ ) {
    	    HashMap child = new HashMap();
    		child.put( "Sub Item", "Sub Item " + n );    	    
    		secList.add( child );
    	  }
    	 result.add( secList );
    	}    	 
    	return result;
    }
    
    public void onContentChanged() {
    	System.out.println("onContentChanged");
	    super.onContentChanged();	      
    }
    
    /* This function is called on each child click */
    public boolean onChildClick( ExpandableListView parent, View v, int groupPosition,int childPosition,long id) {
    	System.out.println("Inside onChildClick at groupPosition = " + groupPosition +" Child clicked at position " + childPosition);
    	return true;
    }

    /* This function is called on expansion of the group */
    public void onGroupExpand(int groupPosition) {
    	try {
    		 System.out.println("Group exapanding Listener => groupPosition = " + groupPosition);
    	}
    	catch(Exception e) {
    		System.out.println(" groupPosition Errrr +++ " + e.getMessage());
    	}
    }  
}