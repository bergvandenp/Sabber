package nl.napauleon.sabber.history;

import android.app.Activity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import nl.napauleon.sabber.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HistoryListAdapter extends ArrayAdapter<HistoryInfo>{

	private List<HistoryInfo> content;
	private Activity activity;

	public HistoryListAdapter(Activity activity, List<HistoryInfo> content) {
		super(activity, R.layout.historyrowlayout, content);
		this.activity = activity;
		this.content = content;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.historyrowlayout, null, true);
		TextView textView = (TextView) rowView.findViewById(R.id.itemname);
		TextView dateView = (TextView) rowView.findViewById(R.id.itemdate);
        TextView messageView = (TextView) rowView.findViewById(R.id.historymessage);

        HistoryInfo historyInfo = content.get(position);

        textView.setText(historyInfo.getItem());
        try {
            Date dateDownloaded = new SimpleDateFormat("yyyy-MM-dd hh:mm").parse(historyInfo.getDateDownloaded());
            dateView.setText(DateUtils.getRelativeTimeSpanString(dateDownloaded.getTime()));
        } catch (ParseException e) {
            Log.w("Sabber", "couldnt parse date" + historyInfo.getDateDownloaded());
            dateView.setText(historyInfo.getDateDownloaded());
        }
        messageView.setText(historyInfo.getMessage());
		return rowView;
	}

}
