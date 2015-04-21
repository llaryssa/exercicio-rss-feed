package br.ufpe.cin.if1001.rss;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ItemRSSAdapter extends BaseAdapter {

	private List<ItemRSS> items;
	private Activity activity;
	
	public ItemRSSAdapter(Activity activity, List<ItemRSS> items) {
		this.activity = activity;
		this.items = items;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public ItemRSS getItem(int index) {
		return items.get(index);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {		
		// TODO: trocar o estilo do xml
		View v = this.activity.getLayoutInflater().inflate(R.layout.item_list, null);		
		ItemRSS item = items.get(position);
		if (item != null) {
			TextView t1 = (TextView) v.findViewById(R.id.title);
			t1.setText(item.title);
			
			TextView t2 = (TextView) v.findViewById(R.id.description);
			t2.setText(item.description);
			
			TextView t3 = (TextView) v.findViewById(R.id.pubDate);
			t3.setText(item.pubDate);
		}		
		return v;	
	}

}
