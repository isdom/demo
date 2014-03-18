/**
 * 
 */
package org.jocean.nettyhttpclient;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jocean.idiom.ByteArrayListInputStream;
import org.jocean.idiom.Visitor;
import org.jocean.syncfsm.api.AbstractFlow;
import org.jocean.syncfsm.api.BizStep;
import org.jocean.syncfsm.api.EventHandler;
import org.jocean.syncfsm.api.annotion.OnEvent;
import org.jocean.syncfsm.api.annotion.SameThread;
import org.jocean.transportclient.TransportEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author isdom
 *
 */
@SameThread
public class DownloadImageFlow1 extends AbstractFlow {
	private static final Logger LOG = LoggerFactory
			.getLogger("DownloadImageFlow1");

	/*
	public final BizStep OBTAINING = new BizStep("dlimg1.UNCONNECTED")
			.handler(selfInvoker("onActive"))
			.handler(selfInvoker("onInactive")).freeze();

	public final BizStep RECVRESP = new BizStep("dlimg1.RECVRESP")
			.handler(selfInvoker("responseReceived"))
			.handler(selfInvoker("onInactive")).freeze();

	public final BizStep RECVCONTENT = new BizStep("dlimg1.RECVCONTENT")
			.handler(selfInvoker("contentReceived"))
			.handler(selfInvoker("lastContentReceived"))
			.handler(selfInvoker("onInactiveAndSaveUncompleteContent"))
			.freeze();

	public final BizStep RECVCOMPLETE = new BizStep("dlimg1.RECVCOMPLETE")
		.handler(selfInvoker("onInactive")).freeze();

	@OnEvent(event = TransportEvents.EVENT_CHANNELINACTIVE)
	private EventHandler onInactive(final ChannelHandlerContext ctx)
			throws Exception {
		this._channelRemover.removeChannel(ctx.channel());
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("channel for {} closed.", _uri);
		}
		return null;
	}

	@OnEvent(event = TransportEvents.EVENT_CHANNELACTIVE)
	private EventHandler onActive(final ChannelHandlerContext ctx) {
		// save http request
		_request = genHttpRequest(_uri);
		LOG.debug("send http request {}", _request);
		ctx.channel().writeAndFlush(_request);
		return RECVRESP;
	}

	@OnEvent(event = TransportEvents.EVENT_HTTPRESPONSERECEIVED)
	private EventHandler responseReceived(final ChannelHandlerContext ctx,
			final HttpResponse response) {
		LOG.debug("channel for {} recv response {}", _uri, response);
		_response = response;
		return RECVCONTENT;
	}

	@OnEvent(event = TransportEvents.EVENT_HTTPCONTENTRECEIVED)
	private EventHandler contentReceived(final ChannelHandlerContext ctx,
			final HttpContent content) {
		final byte[] bytes = content.content().array();
		_bytesList.add(bytes);
		LOG.debug("channel for {} recv content, size {}", _uri, bytes.length);
		// _buf.addComponent( content.content().retain() );
		return RECVCONTENT;
	}

	@OnEvent(event = TransportEvents.EVENT_CHANNELINACTIVE)
	private EventHandler onInactiveAndSaveUncompleteContent(
			final ChannelHandlerContext ctx) throws Exception {
		// Add some code to save reuse data from server
		// ...
		// TODO
		// _onSaveUncompletedHttpContent.visit(_response, _bytesList);

		_channelRemover.removeChannel(ctx.channel());
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("channel for {} closed.", _uri);
		}
		return null;
	}

	@OnEvent(event = TransportEvents.EVENT_LASTHTTPCONTENTRECEIVED)
	private EventHandler lastContentReceived(final ChannelHandlerContext ctx,
			final LastHttpContent content) throws Exception {
		final byte[] bytes = content.content().array();
		_bytesList.add(bytes);
		LOG.debug("channel for {} recv last content, size {}", _uri,
				bytes.length);
		// _buf.addComponent( content.content().retain() );
		_bitmapVisitor.visit(BitmapFactory
				.decodeStream(new ByteArrayListInputStream(_bytesList)));
		// _buf.removeComponents(0, _buf.numComponents());
		// _buf.release();
		return RECVCOMPLETE;
	}

	public DownloadImageFlow1(final URI uri, final ChannelRemover channelRemover,
			final Visitor<Bitmap> visitor) {
		this._uri = uri;
		this._bitmapVisitor = visitor;
		this._channelRemover = channelRemover;
	}

	private final URI _uri;
	private final List<byte[]> _bytesList = new ArrayList<byte[]>();
	private final Visitor<Bitmap> _bitmapVisitor;
	private final ChannelRemover _channelRemover;
	private HttpRequest _request;
	private HttpResponse _response;

	private static HttpRequest genHttpRequest(final URI uri) {
		// Prepare the HTTP request.
		final String host = uri.getHost() == null ? "localhost" : uri.getHost();

		final HttpRequest request = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
		request.headers().set(HttpHeaders.Names.HOST, host);
		request.headers().set(HttpHeaders.Names.CONNECTION,
				HttpHeaders.Values.CLOSE);
		request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING,
				HttpHeaders.Values.GZIP);

		return request;
	}
	*/
}
