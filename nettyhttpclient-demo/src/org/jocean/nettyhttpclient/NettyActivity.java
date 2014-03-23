package org.jocean.nettyhttpclient;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

public class NettyActivity extends Activity {  
	  
//	static {
//		System.setProperty("io.netty.tryUnsafe", "false");
//		System.setProperty("io.netty.noPreferDirect", "true");
//		System.setProperty("io.netty.allocator.numDirectArenas", "0")
//		io.netty.util.internal.PlatformDependent.freeDirectBuffer(  java.nio.ByteBuffer.allocateDirect(1) );
//	}
	
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
        try {
			adapter.cancelAllTasks();
		} catch (Exception e) {
			e.printStackTrace();
		}  
    }  
  
} 
