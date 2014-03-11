package org.jocean.syncfsm.demo;

import org.jocean.idiom.ExceptionUtils;
import org.jocean.syncfsm.api.EventHandler;
import org.jocean.syncfsm.api.EventReceiver;
import org.jocean.syncfsm.api.FlowSource;
import org.jocean.syncfsm.container.FlowContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jocean.syncfsm.demo.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	  private static final Logger LOG =
              LoggerFactory.getLogger(MainActivity.class);
	  
	public MainActivity() {
		this._demoEventReceiver = new FlowContainer("demo").genEventReceiverSource().create(
                 new FlowSource<DemoFlow>() {

					@Override
					public DemoFlow getFlow() {
						return _flow;
					}

					@Override
					public EventHandler getInitHandler(final DemoFlow flow) {
						return flow.LOCKED;
					}});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initControllers();
	}

	private void initControllers() {
		final TextView stateView = (TextView)findViewById(R.id.textState);
		
		stateView.setText(this._flow.currentEventHandler().getName());
		
		initButtonWithEvent(stateView, R.id.btnCoin, "coin");	
		initButtonWithEvent(stateView, R.id.btnPass, "pass");	
	}

	private void initButtonWithEvent(final TextView stateView, final int btnid, final String event) {
		((Button) findViewById(btnid)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(final View v) {
				try {
					if ( !_demoEventReceiver.acceptEvent(event, stateView) ) {
						displayNotAcceptEvent(event);
					}
				} catch (Exception e) {
					LOG.error("exception when accept {} event, detail: {}", 
							event, ExceptionUtils.exception2detail(e));
				}
			}
		});
	}

	protected void displayNotAcceptEvent(final String msg) {
		Toast.makeText(getApplicationContext(), "Not accept event:" + msg, Toast.LENGTH_SHORT).show();
 	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private final EventReceiver	_demoEventReceiver;
	private final DemoFlow	_flow = new DemoFlow();
}
