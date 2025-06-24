package com.sebastian.licentafrontendtransport.Lines;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sebastian.licentafrontendtransport.R;

import java.util.List;
import java.util.Map;

public class ExpandableMetroListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<MetroLine> metroLines; // grupuri
    private final Map<String, List<String>> stationMap; // copii per linie (nume linie -> statii)

    public ExpandableMetroListAdapter(Context context, List<MetroLine> metroLines, Map<String, List<String>> stationMap) {
        this.context = context;
        this.metroLines = metroLines;
        this.stationMap = stationMap;
    }

    @Override
    public int getGroupCount() {
        return metroLines.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String key = metroLines.get(groupPosition).getName();
        return stationMap.containsKey(key) ? stationMap.get(key).size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return metroLines.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String key = metroLines.get(groupPosition).getName();
        return stationMap.get(key).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_group_linie_metrou, parent, false);
        }

        MetroLine line = metroLines.get(groupPosition);

        TextView tvLineName = convertView.findViewById(R.id.tvLineName);
        TextView tvLineDescription = convertView.findViewById(R.id.tvLineDescription);
        TextView tvLineFunFact = convertView.findViewById(R.id.tvFunFact);
        View colorIndicator = convertView.findViewById(R.id.colorIndicator);
        ImageView expandIcon = convertView.findViewById(R.id.expandIcon);

        if (tvLineName != null) {
            tvLineName.setText(line.getName());
        }
        if (tvLineDescription != null) {
            tvLineDescription.setText(line.getDescription());
        }
        if (tvLineFunFact != null) {
            tvLineFunFact.setText("Știai că? " + line.getFunFact());
        }
        try {
            colorIndicator.setBackgroundColor(Color.parseColor(line.getColor()));
        } catch (IllegalArgumentException e) {
            colorIndicator.setBackgroundColor(Color.GRAY);
        }

        if (isExpanded) {
            expandIcon.setImageResource(R.drawable.up);
        } else {
            expandIcon.setImageResource(R.drawable.down);
        }

        // Add spacing between group items via padding
        int topPadding = groupPosition == 0 ? 24 : 12;
        int bottomPadding = 12;
        int topPx = (int) (topPadding * context.getResources().getDisplayMetrics().density);
        int bottomPx = (int) (bottomPadding * context.getResources().getDisplayMetrics().density);
        convertView.setPadding(convertView.getPaddingLeft(), topPx, convertView.getPaddingRight(), bottomPx);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_child_statie, parent, false);
        }

        String stationName = stationMap.get(metroLines.get(groupPosition).getName()).get(childPosition);
        TextView tvStation = convertView.findViewById(R.id.tvStationName);
        tvStation.setText(stationName);

        // Set the color of the vertical line instead of the text itself
        View stationLine = convertView.findViewById(R.id.linie_statie);
        try {
            stationLine.setBackgroundColor(Color.parseColor(metroLines.get(groupPosition).getColor()));
        } catch (IllegalArgumentException e) {
            stationLine.setBackgroundColor(Color.DKGRAY);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}