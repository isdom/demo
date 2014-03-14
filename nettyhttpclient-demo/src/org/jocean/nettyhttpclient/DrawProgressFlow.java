/**
 * 
 */
package org.jocean.nettyhttpclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import java.net.URI;

import org.jocean.syncfsm.api.AbstractFlow;
import org.jocean.syncfsm.api.BizStep;
import org.jocean.syncfsm.api.EventHandler;
import org.jocean.syncfsm.api.EventReceiver;
import org.jocean.syncfsm.api.annotion.OnEvent;
import org.jocean.syncfsm.api.annotion.SameThread;
import org.jocean.transportclient.TransportEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;

/**
 * @author isdom
 *
 */
@SameThread
public class DrawProgressFlow extends AbstractFlow {
	private static final Logger LOG = LoggerFactory
			.getLogger("DrawProgressFlow");

	public final BizStep UNCONNECTED = new BizStep("UNCONNECTED")
			.handler(selfInvoker("onConnected"))
			.handler(selfInvoker("onDrawOnConnecting"))
			.handler(selfInvoker("onUnregistered")).freeze();

	private final BizStep RECVRESP = new BizStep("RECVRESP")
			.handler(selfInvoker("responseReceived"))
			.handler(selfInvoker("onDrawOnRecvResp"))
			.handler(selfInvoker("onUnregistered")).freeze();

	private final BizStep RECVCONTENT = new BizStep("RECVCONTENT")
			.handler(selfInvoker("contentReceived"))
			.handler(selfInvoker("lastContentReceived"))
			.handler(selfInvoker("onDrawOnRecvContent"))
			.handler(selfInvoker("onUnregistered"))
			.freeze();

	@Override
	public void setEventReceiver(final EventReceiver receiver) {
		super.setEventReceiver(receiver);
		this._collection.addEventReceiver(receiver);
		this._view.invalidate();
	}

	@OnEvent(event = TransportEvents.EVENT_CHANNELUNREGISTERED)
	private EventHandler onUnregistered(final ChannelHandlerContext ctx)
			throws Exception {
		this._collection.removeEventReceiver( selfEventReceiver() );
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("channel for {} closed.", _uri);
		}
		return null;
	}

	@OnEvent(event = TransportEvents.EVENT_CHANNELCONNECTED)
	private EventHandler onConnected(final ChannelHandlerContext ctx) {
		this._view.invalidate();
		return RECVRESP;
	}

	@OnEvent(event = "onDraw")
	private EventHandler onDrawOnConnecting(final ImageView view, final Canvas canvas) {
		
		int center = view.getWidth() / 2;
		int radios = center / 4;

		// 绘制圆环
		this.paint.setStyle(Paint.Style.STROKE); // 绘制空心圆
		this.paint.setColor(Color.RED);
		this.paint.setStrokeWidth(ringWidth);
		canvas.drawCircle(center, center, radios, this.paint);

		// display _progress %
		this.paint.setStyle(Paint.Style.FILL);
		this.paint.setColor(textColor);
		this.paint.setStrokeWidth(0);
		this.paint.setTextSize(textSize);
		this.paint.setTypeface(Typeface.DEFAULT_BOLD);
		textProgress = "-->";
		final float textWidth = paint.measureText(textProgress);
		canvas.drawText(textProgress, center - textWidth / 2, center + textSize
				/ 2, paint);
		
		return this.currentEventHandler();
	}
	
	@OnEvent(event = "onDraw")
	private EventHandler onDrawOnRecvResp(final ImageView view, final Canvas canvas) {
		
		int center = view.getWidth() / 2;
		int radios = center / 4;

		// 绘制圆环
		this.paint.setStyle(Paint.Style.STROKE); // 绘制空心圆
		this.paint.setColor(Color.GREEN);
		this.paint.setStrokeWidth(ringWidth);
		canvas.drawCircle(center, center, radios, this.paint);

		// display _progress %
		this.paint.setStyle(Paint.Style.FILL);
		this.paint.setColor(textColor);
		this.paint.setStrokeWidth(0);
		this.paint.setTextSize(textSize);
		this.paint.setTypeface(Typeface.DEFAULT_BOLD);
		textProgress = "<--";
		final float textWidth = paint.measureText(textProgress);
		canvas.drawText(textProgress, center - textWidth / 2, center + textSize
				/ 2, paint);
		
		return this.currentEventHandler();
	}
	
	@OnEvent(event = TransportEvents.EVENT_HTTPRESPONSERECEIVED)
	private EventHandler responseReceived(final ChannelHandlerContext ctx,
			final HttpResponse response) {
		if ( LOG.isDebugEnabled()) {
			LOG.debug("channel for {} recv response {}", _uri, response);
		}
		_response = response;
		_contentLength = HttpHeaders.getContentLength(response, -1);
		// 考虑 Content-Range 的情况
		final String contentRange = response.headers().get(HttpHeaders.Names.CONTENT_RANGE);
		if ( null != contentRange ) {
			LOG.info("found Content-Range header, parse {}", contentRange);
			//	Content-Range: bytes (unit first byte pos) - [last byte pos]/[entity legth] 
			//	eg: Content-Range: bytes 0-800/801 //801:文件总大小
			final int bytesStart = contentRange.indexOf("bytes ");
			final String bytesRange = ( -1 != bytesStart ? contentRange.substring(bytesStart + 6) : contentRange);
			LOG.info("Content-Range parsing bytesStart:{}/ bytesRange:{}", bytesStart, bytesRange);
			
			final int dashStart = bytesRange.indexOf('-');
			final String partBegin = ( -1 != dashStart ? bytesRange.substring(0, dashStart) : null);
			final int totalStart = bytesRange.indexOf('/');
			final String totalSize = ( -1 != totalStart ? bytesRange.substring(totalStart + 1) : null);
			if ( null != partBegin ) {
				this._progress = Integer.parseInt(partBegin);
				LOG.info("Content-Range begins at {}", this._progress);
			}
			if ( null != totalSize ) {
				this._contentLength = Long.parseLong(totalSize);
				LOG.info("Content-Range total size {}", this._contentLength);
			}
		}
		return RECVCONTENT;
	}

	@OnEvent(event = TransportEvents.EVENT_HTTPCONTENTRECEIVED)
	private EventHandler contentReceived(final ChannelHandlerContext ctx,
			final HttpContent content) {
		final byte[] bytes = content.content().array();
		_progress += bytes.length;
		LOG.info("progress: uri {}, {}/{}", new Object[]{ _uri, this._progress, this._contentLength});
		
		//	refresh UI
		if ( null != this._view) {
			this._view.invalidate();
		}
		
		return RECVCONTENT;
	}

	@OnEvent(event = TransportEvents.EVENT_LASTHTTPCONTENTRECEIVED)
	private EventHandler lastContentReceived(final ChannelHandlerContext ctx,
			final LastHttpContent content) throws Exception {
		final byte[] bytes = content.content().array();
		_progress += bytes.length;
		LOG.info("end of progress: uri {}, {}/{}", new Object[]{ _uri, this._progress, this._contentLength});
		this._collection.removeEventReceiver(selfEventReceiver());
		return null;
	}

	@OnEvent(event = "onDraw")
	private EventHandler onDrawOnRecvContent(final ImageView view, final Canvas canvas) {
		
		final long max = Math.max(this._contentLength, this._progress);
		LOG.info("draw uri {} progress with {}/{}", new Object[]{ this._uri, this._progress, this._contentLength}); 
		

		int center = view.getWidth() / 2;
		int radios = center / 4;

		// 绘制圆环
		this.paint.setStyle(Paint.Style.STROKE); // 绘制空心圆
		this.paint.setColor(ringColor);
		this.paint.setStrokeWidth(ringWidth);
		canvas.drawCircle(center, center, radios, this.paint);

		// draw arc
		final RectF oval = new RectF(center - radios, center - radios, center
				+ radios, center + radios);

		this.paint.setColor(progressColor);
		canvas.drawArc(oval, 90, 360 * this._progress / max, false, paint);

		// display _progress %
		this.paint.setStyle(Paint.Style.FILL);
		this.paint.setColor(textColor);
		this.paint.setStrokeWidth(0);
		this.paint.setTextSize(textSize);
		this.paint.setTypeface(Typeface.DEFAULT_BOLD);
		textProgress = (int) (1000 * (this._progress / (10.0 * max))) + "%";
		float textWidth = paint.measureText(textProgress);
		canvas.drawText(textProgress, center - textWidth / 2, center + textSize
				/ 2, paint);
		
		return this.currentEventHandler();
	}
	
	public DrawProgressFlow(final Context context, final View view, final URI uri, final EventReceiverCollection collection) {
		this._uri = uri;
		this._collection = collection;
		this._view = view;
		
		this.paint.setAntiAlias(true); // 消除锯齿

		this.ringWidth = dip2px(context, 3); // 设置圆环宽度;
		this.ringColor = Color.BLACK;// 黑色进度条背景
		this.progressColor = Color.WHITE;// 白色进度条
		this.textColor = Color.BLACK;// 黑色数字显示进度;
		this.textSize = 15;// 默认字体大小
	}

	private final URI _uri;
	private final EventReceiverCollection _collection;
	private long _contentLength = -1;
	private int _progress = 0;
	private final View _view;
	
	private HttpRequest _request;
	private HttpResponse _response;
	
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(final Context context, final float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	private final Paint paint = new Paint();

	private int ringWidth;

	// 圆环的颜色
	private int ringColor;

	// 进度条颜色
	private int progressColor;
	// 字体颜色
	private int textColor;
	// 字的大小
	private int textSize;
	private String textProgress;
	
}
