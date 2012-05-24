package nl.napauleon.sabber.search;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import nl.napauleon.sabber.R;

import java.util.List;

public class SearchListAdapter extends ArrayAdapter<NzbInfo> {

    private List<NzbInfo> content;
    private Activity activity;

    public SearchListAdapter(Activity activity, List<NzbInfo> content) {
        super(activity, R.layout.searchrowlayout, content);
        this.activity = activity;
        this.content = content;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.searchrowlayout, null, true);

        TextView nameView = (TextView) rowView.findViewById(R.id.name);
        TextView sizeView = (TextView) rowView.findViewById(R.id.size);
        NzbInfo nzbInfo = content.get(position);
        nameView.setText(nzbInfo.getTitle());
        sizeView.setText(nzbInfo.getSize());
        return rowView;
    }
    
}
