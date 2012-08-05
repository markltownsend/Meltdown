package net.phfactor.meltdown;

// For JSONArray to ListView I cribbed from
// http://p-xr.com/android-tutorial-how-to-parse-read-json-data-into-a-android-listview/


import java.util.HashMap;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class GroupsActivity extends ListActivity 
{
	static final String TAG = "MeltdownGA";
	private MeltdownApp app;
	
	private ProgressDialog pd;
	private RestClient rc;
	private Context ctx;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		rc = new RestClient(this);
		app = new MeltdownApp(this);
		ctx = this;
		
		// TODO run setup if login errors out?
		// Check for login, run prefs
		if (rc.haveSetup() == false)
		{
			startActivity(new Intent(this, SetupActivity.class));
			Toast.makeText(this, "Please configure a server", Toast.LENGTH_SHORT).show();
		}

		setContentView(R.layout.list);
		
		pd = new ProgressDialog(this);
		
		// TODO proportional!
		pd.setIndeterminate(true);
		pd.setMessage("Fetching data...");
		pd.show();

		class GGTask extends AsyncTask<Void, Void, Void> {
			protected Void doInBackground(Void... args) {
				app.saveGroupsData(rc.fetchGroups());
				app.saveFeedsData(rc.fetchFeeds());
				
				// TODO Display groups list while feeds run
				// FIXME limit fetch limit w/prefs-set bound, e.g. 100. 
//				while (app.saveItemsData(rc.fetchSomeFeeds(app.getMaxFetchedId())) > 0) 
//					Log.i(TAG, "Pulling another chunk...");
				
				return null;
			}
			@Override
			protected void onPostExecute(Void arg) {
				pd.dismiss();
				
				ListAdapter ladapt = new SimpleAdapter(ctx, app.getALG(), R.layout.row,
						new String[] {"title", "unread"},
						new int[] {R.id.group_title, R.id.group_subtitle});
				setListAdapter(ladapt);
		        final ListView lv = getListView();
		        lv.setTextFilterEnabled(true);
		        lv.setOnItemClickListener(new OnItemClickListener()
		        {
		        	@Override
		        	public void onItemClick(AdapterView<?> arg0, View view, int pos, long id)
		        	{
						HashMap<String, String> o = (HashMap<String, String>) lv.getItemAtPosition(pos);
						Intent intent = new Intent(GroupsActivity.this, ItemsActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("title", o.get("title"));
						intent.putExtras(bundle);
						startActivity(intent);
		        	}
		        	
		        });
			}
		}

		new GGTask().execute();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_groups, menu);
		return true;
	}
}
