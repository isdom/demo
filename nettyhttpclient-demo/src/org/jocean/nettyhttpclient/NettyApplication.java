package org.jocean.nettyhttpclient;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dalvik.system.DexClassLoader;

import android.app.Application;

public class NettyApplication extends Application {

	private static final Logger LOG = LoggerFactory
			.getLogger("NettyApplication");

	public NettyApplication() {
	}

	@Override
	public void onCreate() {
		LOG.info("on NettyApplication's onCreate");
		
//		final String basedir = this.getFilesDir().getAbsoluteFile().toString();
//		final String apkPath = basedir + File.separator + "netty.apk";
//		
//		LOG.info("basedir {}, apkPath {}", basedir, apkPath);
//		
//		ClassLoader cl = new DexClassLoader(apkPath, basedir, null, Thread.currentThread().getContextClassLoader());
//		try {
//			Class<?> c = cl.loadClass("io.netty.channel.EventLoop");
//			LOG.info( "load {} succeed", c.getName() );
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//		
//		Thread.currentThread().setContextClassLoader(cl);
		
		super.onCreate();
	}
}
