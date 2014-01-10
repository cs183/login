package com.example.login;


import org.achartengine.GraphicalView;

import com.example.login.R;
import com.example.login.chart.SensorValueChart;
//import ru.efko.dims.domain.DataChangeHandler;
//import ru.efko.dims.domain.SensorDevice;
//import ru.efko.dims.util.Constants;
//import ru.efko.dims.util.ThreadLocalVariablesKeeper;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class GraphicsActivity extends Activity implements SensorDevice.ChangeHandler {

	private GraphicalView mChartView;
	private Button startButton;
	private Button stopButton;
	private Button settingsButton;
	private SensorValueChart chart;
	private LinearLayout layout;

	private SensorDevice sensorDevice;
	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
		startButton = (Button) findViewById(R.id.startButton);
		startButton.setBackgroundResource(R.drawable.play_grey);
		startButton.setEnabled(false);
		stopButton = (Button) findViewById(R.id.stopButton);
		settingsButton = (Button) findViewById(R.id.settingsButton);
		sensorDevice = ThreadLocalVariablesKeeper.getSensorDevice();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		addListeners();
	}

	private void addListeners() {
		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sensorDevice.startPolling(40);
				startButton.setEnabled(false);
				startButton.setBackgroundResource(R.drawable.play_grey);
				stopButton.setEnabled(true);
				stopButton.setBackgroundResource(R.drawable.stop);

			}
		});

		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sensorDevice.stopPolling();
				stopButton.setEnabled(false);
				stopButton.setBackgroundResource(R.drawable.stop_grey);
				startButton.setEnabled(true);
				startButton.setBackgroundResource(R.drawable.play);
			}
		});
		
		settingsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
/*                sensorDevice.onDataChangedHandler = null;
				sensorDevice.stopPolling();
				Intent settingsIntent = new Intent(GraphicsActivity.this, SettingsActivity.class);
				settingsIntent.putExtra(Constants.CAME_FROM, Constants.MENU_GRAPHICS);
				GraphicsActivity.this.startActivity(settingsIntent);
*/			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		startButton.setBackgroundResource(R.drawable.play_grey);
		startButton.setEnabled(false);
		stopButton.setBackgroundResource(R.drawable.stop);
		stopButton.setEnabled(true);
		chart = new SensorValueChart();
		if (sensorDevice == null) {
			CharSequence text = "Нет соединения с сервером";
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
			return;
		}
		sensorDevice.startPolling(40);
        /*
		while (sensorDevice.getData() == null) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				;
			}
		}
		*/
		if (mChartView == null) {
			layout = (LinearLayout) findViewById(R.id.mainLayout);
			mChartView = chart.execute(this, sensorDevice.getData(),prefs);
			layout.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			layout.setBottom(startButton.getHeight());
			sensorDevice.onDataChangedHandler = this;
			addListeners();
		} else {
			mChartView.repaint();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
        sensorDevice.onDataChangedHandler = null;
		sensorDevice.stopPolling();
		layout.removeView(mChartView);
		mChartView = null;
		chart = null;
		
	}

 /*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, Constants.MENU_SETTINGS, 0, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(Menu.NONE, Constants.MENU_SYSTEM_PREFERENCES, 1, R.string.menu_system_preferences);
        menu.add(Menu.NONE, Constants.MENU_DISCONNECT, 2, R.string.menu_disconnect);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case Constants.MENU_SETTINGS:
			sensorDevice.setDataHandler(null);
			sensorDevice.stopPolling();
			Intent settingsIntent = new Intent(GraphicsActivity.this, SettingsActivity.class);
			settingsIntent.putExtra(Constants.CAME_FROM, Constants.MENU_GRAPHICS);
			GraphicsActivity.this.startActivity(settingsIntent);
			return true;
		case Constants.MENU_DISCONNECT:
			sensorDevice.disconnect();
			ThreadLocalVariablesKeeper.clean();
            finish();
			return true;
		case Constants.MENU_SYSTEM_PREFERENCES:
			Intent systemIntent = new Intent(GraphicsActivity.this, SystemPreferencesActivity.class);
			GraphicsActivity.this.startActivity(systemIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
 */

	public void onDataChanged(int[] data) {
		// TODO Auto-generated method stub
		chart.updateDataSet();

	}

}