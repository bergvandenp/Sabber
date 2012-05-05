package nl.napauleon.downloadmanager.Queue;

import java.util.List;

import nl.napauleon.downloadmanager.R;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class QueueListAdapter extends ArrayAdapter<QueueInfo>{
	
	private List<QueueInfo> content;
	private Activity activity;

	public QueueListAdapter(Activity activity, List<QueueInfo> content) {
		super(activity, R.layout.queuerowlayout, content);
		this.activity = activity;
		this.content = content;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.queuerowlayout, null, true);
		TextView textView = (TextView) rowView.findViewById(R.id.itemname);
		TextView timeleftView = (TextView) rowView.findViewById(R.id.timeleft);
		ProgressBar progressView = (ProgressBar) rowView.findViewById(R.id.progress);
		QueueInfo queueInfo = content.get(position);
		textView.setText(queueInfo.getItem());
		timeleftView.setText(queueInfo.getTimeleft());
		progressView.setProgress(queueInfo.getPercentage());
		return rowView;
	}


}
