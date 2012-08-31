
package com.Fitivity;

import java.util.ArrayList;
import java.util.List;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;



public class SampleActivity extends Activity implements Runnable
{
    private ExpandableListAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Parse.initialize(this, "MmUj6HxQcfLSOUs31lG7uNVx9sl5dZR6gv0FqGHq", "krpZsVM2UrU71NCxDbdAmbEMq1EXdpygkl251Wjl");
        
        // Retrive the ExpandableListView from the layout
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        
        listView.setOnChildClickListener(new OnChildClickListener()
        {
            
            @Override
            public boolean onChildClick(ExpandableListView arg0, View arg1, int arg2, int arg3, long arg4)
            {
            	
            	
            	FitivityActivity selected = (FitivityActivity) adapter.getChild(arg2, arg3);
            	
            	Intent resultIntent = new Intent();
        		
        		
        		resultIntent.putExtra("activity", selected.getName());
        		resultIntent.putExtra("category", selected.getCategory());
        		
        		setResult(Activity.RESULT_OK, resultIntent);
        		finish();
         
                return false;
            }
        });
        
        listView.setOnGroupClickListener(new OnGroupClickListener()
        {
            
            @Override
            public boolean onGroupClick(ExpandableListView arg0, View arg1, int arg2, long arg3)
            {
                return false;
            }
        });

        // Initialize the adapter with blank groups and children
        // We will be adding children on a thread, and then update the ListView
        adapter = new ExpandableListAdapter(this, new ArrayList<String>(),
                new ArrayList<ArrayList<FitivityActivity>>());

        // Set this blank adapter to the list view
        listView.setAdapter(adapter);

        // This thread randomly generates some vehicle types
        // At an interval of every 2 seconds
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() 
    {
    	
    	ParseQuery query = new ParseQuery("Activity");
 	   
    	List<ParseObject> activityList = null;
    	
 	   try {
		 activityList = query.find();
 	   	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
 	   	}
 	   	for (int i = 0; i < activityList.size() - 1 ; i++) {
 	   		ParseObject element = activityList.get(i);
 	   		
 	   		FitivityActivity activity = new FitivityActivity(element.getString("name"));
 	   		activity.setCategory(element.getString("category"));
 	   		adapter.addItem(activity);
 	   		
 	   		// Notify the adapter
            handler.sendEmptyMessage(1);
 	   	}
    	
        
    }

    private Handler handler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            adapter.notifyDataSetChanged();
            super.handleMessage(msg);
        }

    };
}
