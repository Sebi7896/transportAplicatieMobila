package com.sebastian.licentafrontendtransport.Lines;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sebastian.licentafrontendtransport.R;

public class LineFragment extends Fragment {


    public LineFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_line, container, false);

        ExpandableListView expandableListView = view.findViewById(R.id.expandableListView);
        List<MetroLine> metroLines = loadMetroLinesFromAssets();

        Map<String, List<String>> stationsMap = new HashMap<>();
        for (MetroLine line : metroLines) {
            stationsMap.put(line.getName(), line.getStations());
        }

        ExpandableMetroListAdapter adapter = new ExpandableMetroListAdapter(requireContext(), metroLines, stationsMap);
        expandableListView.setAdapter(adapter);

        return view;
    }

    private List<MetroLine> loadMetroLinesFromAssets() {
        try {
            InputStream is = requireContext().getAssets().open("metro_lines.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray linesArray = jsonObject.getJSONArray("lines");

            Gson gson = new Gson();
            Type listType = new TypeToken<List<MetroLine>>() {}.getType();
            return gson.fromJson(linesArray.toString(), listType);

        } catch (IOException | JSONException e) {
            return new ArrayList<>();
        }
    }
}