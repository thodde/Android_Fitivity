package com.fitivity;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    private Context context;

    private ArrayList<String> categories;

    private ArrayList<ArrayList<FitivityActivity>> children;

    public ExpandableListAdapter(Context context, ArrayList<String> categories,
            ArrayList<ArrayList<FitivityActivity>> children) {
        this.context = context;
        this.categories = categories;
        this.children = children;
    }

    /**
     * A general add method, that allows you to add a Vehicle to this list
     * 
     * Depending on if the category of the vehicle is present or not,
     * the corresponding item will either be added to an existing group if it 
     * exists, else the group will be created and then the item will be added
     * @param vehicle
     */
    public void addItem(FitivityActivity activity) {
        if (!categories.contains(activity.getCategory())) {
        	categories.add(activity.getCategory());
        }
        int index = categories.indexOf(activity.getCategory());
        if (children.size() < index + 1) {
            children.add(new ArrayList<FitivityActivity>());
        }
        children.get(index).add(activity);
    }

    public Object getChild(int categoryPosition, int childPosition) {
        return children.get(categoryPosition).get(childPosition);
    }

    public long getChildId(int categoryPosition, int childPosition) {
        return childPosition;
    }
    
    // Return a child view. You can load your custom layout here.
    public View getChildView(int categoryPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        FitivityActivity activity = (FitivityActivity) getChild(categoryPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.child_layout, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
        tv.setText("   " + activity.getName());

        // Depending upon the child type, set the imageTextView01
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (activity instanceof Car) {
            tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.car, 0, 0, 0);
        } else if (activity instanceof Bus) {
            tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bus, 0, 0, 0);
        } else if (activity instanceof Bike) {
            tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bike, 0, 0, 0);
        }
        return convertView;
    }

    public int getChildrenCount(int categoryPosition) {
        return children.get(categoryPosition).size();
    }

    public Object getGroup(int groupPosition) {
        return categories.get(groupPosition);
    }

    public int getGroupCount() {
        return categories.size();
    }

    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    // Return a group view. You can load your custom layout here.
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        String group = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.group_layout, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
        tv.setText(group);
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public boolean isChildSelectable(int arg0, int arg1) {
        return true;
    }

}
