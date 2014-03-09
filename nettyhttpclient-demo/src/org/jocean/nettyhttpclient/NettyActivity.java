package org.jocean.nettyhttpclient;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

/*
public class NettyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setupAction();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void setupAction() {
		final String[] uris = new String[]{"http://www.google.com.hk", "http://www.baidu.com", "http://cn.bing.com"};
		//final EditText httpUri = (EditText)this.findViewById(R.id.httpURI);
		((Button)this.findViewById(R.id.btnGo)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				//final String uri = httpUri.getText().toString();
				//Toast.makeText(getApplicationContext(), uri, Toast.LENGTH_SHORT).show();
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							
							final List<Channel> chs = new ArrayList<Channel>() {{
								for ( String s : uris ) {
									final URI uri = new URI(s);
									final HttpDemoFlow flow = new HttpDemoFlow(uri);
									this.add( _http.connect(
											_client.newChannel(), 
											uri, 
											_source.create(flow, flow.UNCONNECTED ) ) );
								}
							}};
							
							TransportUtils.awaitForAllEnded( chs.toArray(new Channel[0]) );
						} catch (Exception e) {
							e.printStackTrace();
						}
					}} ).start();
			}});
	}
	
	private final TransportClient _client = new TransportClient();
	private final HttpStack _http = new HttpStack();
	private final EventReceiverSource _source = new FlowContainer("demo").genEventReceiverSource();
}
*/

public class NettyActivity extends Activity {  
	  
	static {
//		System.setProperty("io.netty.tryUnsafe", "false");
//		System.setProperty("io.netty.noPreferDirect", "true");
//		System.setProperty("io.netty.allocator.numDirectArenas", "0")
//		io.netty.util.internal.PlatformDependent.freeDirectBuffer(  java.nio.ByteBuffer.allocateDirect(1) );
	}
	
    /** 
     * 用于展示照片墙的GridView 
     */  
    private GridView mPhotoWall;  
  
    /** 
     * GridView的适配器 
     */  
    private PhotoWallAdapterNio adapter;  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
        mPhotoWall = (GridView) findViewById(R.id.photo_wall);  
        adapter = new PhotoWallAdapterNio(this, 0, Images.imageThumbUrls, mPhotoWall);  
        mPhotoWall.setAdapter(adapter);  
    }  
  
    @Override  
    protected void onDestroy() {  
        super.onDestroy();  
        // 退出程序时结束所有的下载任务  
        adapter.cancelAllTasks();  
    }  
  
} 
