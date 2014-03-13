/**
 * 
 */
package org.jocean.nettyhttpclient;

import io.netty.channel.Channel;
import io.netty.util.ReferenceCounted;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.jocean.idiom.Visitor;
import org.jocean.syncfsm.api.ArgsHandler;
import org.jocean.syncfsm.api.EventReceiver;
import org.jocean.syncfsm.api.EventReceiverSource;
import org.jocean.syncfsm.api.SyncFSMUtils;
import org.jocean.syncfsm.container.FlowContainer;
import org.jocean.transportclient.HttpStack;
import org.jocean.transportclient.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * @author isdom
 *
 */
public class PhotoWallAdapterNio extends ArrayAdapter<String> implements OnScrollListener {  
	  
	  private static final Logger LOG =
              LoggerFactory.getLogger("PhotoWallAdapterNio");
    /** 
     * 记录所有正在下载或等待下载的任务。 
     */  
    private Set<Channel> taskCollection;  
  
    /** 
     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。 
     */  
    private LruCache<String, Bitmap> mMemoryCache;  
  
    /** 
     * GridView的实例 
     */  
    private GridView mPhotoWall;  
  
    /** 
     * 第一张可见图片的下标 
     */  
    private int mFirstVisibleItem;  
  
    /** 
     * 一屏有多少张图片可见 
     */  
    private int mVisibleItemCount;  
  
    /** 
     * 记录是否刚打开程序，用于解决进入程序不滚动屏幕，不会下载图片的问题。 
     */  
    private boolean isFirstEnter = true;  
  
    public PhotoWallAdapterNio(Context context, int textViewResourceId, String[] objects,  
            GridView photoWall) {  
        super(context, textViewResourceId, objects);  
        mPhotoWall = photoWall;  
        taskCollection = new HashSet<Channel>();
        // 获取应用程序最大可用内存  
        int maxMemory = (int) Runtime.getRuntime().maxMemory();  
        int cacheSize = maxMemory / 8;  
        // 设置图片缓存大小为程序最大可用内存的1/8  
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {  
            @Override  
            protected int sizeOf(final String key, final Bitmap bitmap) {  
                //return bitmap.getByteCount();  
                return	bitmap.getHeight() * bitmap.getWidth() * 4;
            }  
        };  
        mPhotoWall.setOnScrollListener(this);  
    }  
  
    @Override  
    public View getView(final int position, final View convertView, final ViewGroup parent) {  
        final String url = getItem(position);  
        View view;  
        if (convertView == null) {  
            view = LayoutInflater.from(getContext()).inflate(R.layout.photo_layout, null);  
        } else {  
            view = convertView;  
        }  
        final CustomImageView photo = (CustomImageView) view.findViewById(R.id.photo);  
        // 给ImageView设置一个Tag，保证异步加载图片时不会乱序  
        photo.setTag(url);  
        setImageView(url, photo);  
        return view;  
    }  
  
    /** 
     * 给ImageView设置图片。首先从LruCache中取出图片的缓存，设置到ImageView上。如果LruCache中没有该图片的缓存， 
     * 就给ImageView设置一张默认图片。 
     *  
     * @param imageUrl 
     *            图片的URL地址，用于作为LruCache的键。 
     * @param imageView 
     *            用于显示图片的控件。 
     */  
    private void setImageView(final String imageUrl, final ImageView imageView) {  
        final Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);  
        if (bitmap != null) {  
            imageView.setImageBitmap(bitmap);  
        } else {  
            imageView.setImageResource(R.drawable.empty_photo);  
        }  
    }  
  
    /** 
     * 将一张图片存储到LruCache中。 
     *  
     * @param key 
     *            LruCache的键，这里传入图片的URL地址。 
     * @param bitmap 
     *            LruCache的键，这里传入从网络上下载的Bitmap对象。 
     */  
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {  
        if (getBitmapFromMemoryCache(key) == null) {  
            mMemoryCache.put(key, bitmap);  
        }  
    }  
  
    /** 
     * 从LruCache中获取一张图片，如果不存在就返回null。 
     *  
     * @param key 
     *            LruCache的键，这里传入图片的URL地址。 
     * @return 对应传入键的Bitmap对象，或者null。 
     */  
    public Bitmap getBitmapFromMemoryCache(String key) {  
        return mMemoryCache.get(key);  
    }  
  
    @Override  
    public void onScrollStateChanged(AbsListView view, int scrollState) {  
        // 仅当GridView静止时才去下载图片，GridView滑动时取消所有正在下载的任务  
        if (scrollState == SCROLL_STATE_IDLE) {  
            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);  
        } else {  
            cancelAllTasks();  
        }  
    }  
  
    @Override  
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,  
            int totalItemCount) {  
        mFirstVisibleItem = firstVisibleItem;  
        mVisibleItemCount = visibleItemCount;  
        // 下载的任务应该由onScrollStateChanged里调用，但首次进入程序时onScrollStateChanged并不会调用，  
        // 因此在这里为首次进入程序开启下载任务。  
        if (isFirstEnter && visibleItemCount > 0) {  
            loadBitmaps(firstVisibleItem, visibleItemCount);  
            isFirstEnter = false;  
        }  
    }  
  
    /** 
     * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象， 
     * 如果发现任何一个ImageView的Bitmap对象不在缓存中，就会开启异步线程去下载图片。 
     *  
     * @param firstVisibleItem 
     *            第一个可见的ImageView的下标 
     * @param visibleItemCount 
     *            屏幕中总共可见的元素数 
     */  
    private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {  
        try {  
            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {  
                final String imageUrl = Images.imageThumbUrls[i];  
                final Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);  
                if (bitmap == null) {  
                	// start to download image
                	if ( LOG.isDebugEnabled() ) {
                		LOG.debug("start to loading image {}", imageUrl);
                	}
                	
					final URI uri = new URI(imageUrl);
					final DownloadImageFlow0 downloadImageFlow = new DownloadImageFlow0(uri, 
						new ChannelRemover() {

							@Override
							public void removeChannel(final Channel channel) {
								_handler.post(new Runnable() {

									@Override
									public void run() {
										taskCollection.remove(channel);
									}});
							}},
						new Visitor<Bitmap>() {

							@Override
							public void visit(final Bitmap b) throws Exception {
								_handler.post(new Runnable() {
	
									@Override
									public void run() {
							            if (b != null) {  
							                // 图片下载完成后缓存到LrcCache中  
							                addBitmapToMemoryCache(imageUrl, b);  
								            // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。  
						                    setImageToView(imageUrl, b);  
							            }
									}});
							}});
					final DrawProgressFlow progressFlow = new DrawProgressFlow( 
							this.getContext(), 
							getImageViewOf(imageUrl),
							uri, new EventReceiverCollection() {

						@Override
						public void addEventReceiver(
								final EventReceiver eventReceiver) {
							LOG.info("add {}", eventReceiver);
							attachEventReceiverToImageView(imageUrl, eventReceiver);
						}

						@Override
						public void removeEventReceiver(
								final EventReceiver eventReceiver) {
							LOG.info("remove {}", eventReceiver);
							detachEventReceiverFromImageView(imageUrl, eventReceiver);
						}} );
					
					final Channel channel = _client.newChannel();
					taskCollection.add(channel);
					_http.launchConnect(
							channel,
							uri, 
							SyncFSMUtils.combineEventReceivers( 
									SyncFSMUtils.wrapAsyncEventReceiver(_source.create(progressFlow, progressFlow.UNCONNECTED), 
											new Visitor<Runnable>() {

												@Override
												public void visit(final Runnable runnable)
														throws Exception {
													_handler.post(runnable);
													
												}}, 
												genSafeRetainArgsHandler()),
									_source.create(downloadImageFlow, downloadImageFlow.UNCONNECTED )
								),
							false);
                	if ( LOG.isDebugEnabled() ) {
                		LOG.debug("try to connect {}", imageUrl);
                	}
                } else {  
                    setImageToView(imageUrl, bitmap);  
                }  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }

	protected void attachEventReceiverToImageView(final String imageUrl,
			final EventReceiver eventReceiver) {
		final CustomImageView imageView = (CustomImageView) mPhotoWall.findViewWithTag(imageUrl);  
		if (imageView != null ) {  
		    imageView.setEventReceiver(eventReceiver);
		}
	}

	protected void detachEventReceiverFromImageView(final String imageUrl,
			final EventReceiver eventReceiver) {
		final CustomImageView imageView = (CustomImageView) mPhotoWall.findViewWithTag(imageUrl);  
		if (imageView != null ) {  
		    imageView.setEventReceiver(null);
		}
	}

	private ArgsHandler genSafeRetainArgsHandler() {
		return new ArgsHandler() {

			@Override
			public Object[] beforeAcceptEvent(final Object[] args) {
				final Object[] safeArgs = new Object[args.length];
				int idx = 0;
				for ( Object arg : args) {
					if ( arg instanceof ReferenceCounted ) {
						((ReferenceCounted)arg).retain();
					}
					safeArgs[idx++] = arg;
				}
				return safeArgs;
			}

			@Override
			public void afterAcceptEvent(final Object[] args) {
				for ( Object arg : args) {
					if ( arg instanceof ReferenceCounted ) {
						((ReferenceCounted)arg).release();
					}
				}
			}};
	}

	private void setImageToView(final String imageUrl, final Bitmap bitmap) {
		final ImageView imageView = getImageViewOf(imageUrl);  
		if (imageView != null && bitmap != null) {  
		    imageView.setImageBitmap(bitmap);  
		}
	}

	private ImageView getImageViewOf(final String imageUrl) {
		return (ImageView) mPhotoWall.findViewWithTag(imageUrl);
	}  
  
    /** 
     * 取消所有正在下载或等待下载的任务。 
     */  
    public void cancelAllTasks() {  
        if (taskCollection != null) {  
            for (Channel channel : taskCollection) {  
            	channel.close();
            }  
        }  
    }  
  
    private final Handler _handler = new Handler();
	private final TransportClient _client = new TransportClient();
	private final HttpStack _http = new HttpStack();
	private final EventReceiverSource _source = new FlowContainer("global").genEventReceiverSource();
    
}  