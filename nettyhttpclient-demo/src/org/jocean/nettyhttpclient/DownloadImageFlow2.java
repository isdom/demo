/**
 * 
 */
package org.jocean.nettyhttpclient;

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
import org.jocean.idiom.Detachable;
import org.jocean.idiom.Pair;
import org.jocean.idiom.Visitor;
import org.jocean.idiom.Visitor2;
import org.jocean.syncfsm.api.AbstractFlow;
import org.jocean.syncfsm.api.BizStep;
import org.jocean.syncfsm.api.EventHandler;
import org.jocean.syncfsm.api.annotion.OnEvent;
import org.jocean.syncfsm.api.annotion.SameThread;
import org.jocean.transportclient.api.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author isdom
 *
 */
@SameThread
public class DownloadImageFlow2 extends AbstractFlow {
	
	private static final Logger LOG = LoggerFactory
			.getLogger("DownloadImageFlow2");

	public final BizStep OBTAINING = new BizStep("dlimg2.OBTAINING")
			.handler(selfInvoker("onHttpObtained"))
			.handler(selfInvoker("onHttpLost"))
			.handler(selfInvoker("onCanceled"))
			.freeze();

	private final BizStep RECVRESP = new BizStep("dlimg2.RECVRESP")
			.handler(selfInvoker("responseReceived"))
			.handler(selfInvoker("onHttpLost"))
			.handler(selfInvoker("onCanceled"))
			.freeze();

	private final BizStep RECVCONTENT = new BizStep("dlimg2.RECVCONTENT")
			.handler(selfInvoker("contentReceived"))
			.handler(selfInvoker("lastContentReceived"))
			.handler(selfInvoker("onCanceledAndSaveUncompleteContent"))
			.handler(selfInvoker("onHttpLostAndSaveUncompleteContent"))
			.freeze();

	private void safeDetach() {
		if ( null != this._pendingCanceller ) {
			this._pendingCanceller.detach();
			this._pendingCanceller = null;
		}
		
		if ( null != this._httpClient ) {
			this._httpClient.detach();
			this._httpClient = null;
		}
	}
	
	@OnEvent(event="cancel")
	private EventHandler onCanceled() throws Exception {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("download {} progress canceled", _uri);
		}
		safeDetach();
		return null;
		
	}
	
	@OnEvent(event="cancel")
	private EventHandler onCanceledAndSaveUncompleteContent() throws Exception {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("download {} progress canceled", _uri);
		}
		safeDetach();
		if ( null != this._uncompletedVisitor) {
			this._uncompletedVisitor.visit(this._response, this._bytesList);
		}
		return null;
	}
	
	@OnEvent(event = Events.HTTPLOST)
	private EventHandler onHttpLost()
			throws Exception {
		_receiverRemover.removeReceiver(selfEventReceiver());
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("http for {} lost.", _uri);
		}
		return null;
	}

	@OnEvent(event = Events.HTTPOBTAINED)
	private EventHandler onHttpObtained(final HttpClient httpclient) {
		// save http request
		this._pendingCanceller = null;
		this._httpClient = httpclient;
		this._request = genHttpRequest(this._uri, this._part);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("send http request {}", _request);
		}
		httpclient.sendHttpRequest( this._request);
		return RECVRESP;
	}

	@OnEvent(event = Events.HTTPRESPONSERECEIVED)
	private EventHandler responseReceived(final HttpResponse response) {
		LOG.debug("channel for {} recv response {}", _uri, response);
		this._response = response;
		if ( null != this._part ) {
			// check if content range
			final String contentRange = response.headers().get(HttpHeaders.Names.CONTENT_RANGE);
			if ( null != contentRange ) {
				// assume Partial
				this._bytesList.addAll(this._part.second);
				LOG.info("uri {}, recv partial get response, detail: {}", _uri, contentRange);
			}
		}
		return RECVCONTENT;
	}

	@OnEvent(event = Events.HTTPCONTENTRECEIVED)
	private EventHandler contentReceived(final HttpContent content) {
		final byte[] bytes = content.content().array();
		_bytesList.add(bytes);
		LOG.debug("channel for {} recv content, size {}", _uri, bytes.length);
		// _buf.addComponent( content.content().retain() );
		return RECVCONTENT;
	}

	@OnEvent(event = Events.HTTPLOST)
	private EventHandler onHttpLostAndSaveUncompleteContent() throws Exception {
		// Add some code to save reuse data from server
		// ...
		if ( null != this._uncompletedVisitor) {
			this._uncompletedVisitor.visit(this._response, this._bytesList);
		}

		_receiverRemover.removeReceiver(selfEventReceiver());
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("channel for {} closed.", _uri);
		}
		return null;
	}

	@OnEvent(event = Events.LASTHTTPCONTENTRECEIVED)
	private EventHandler lastContentReceived(final LastHttpContent content) throws Exception {
		final byte[] bytes = content.content().array();
		_bytesList.add(bytes);
		LOG.debug("channel for {} recv last content, size {}", _uri,
				bytes.length);
		// _buf.addComponent( content.content().retain() );
		_bitmapVisitor.visit(BitmapFactory
				.decodeStream(new ByteArrayListInputStream(_bytesList)));
		// _buf.removeComponents(0, _buf.numComponents());
		// _buf.release();
		_receiverRemover.removeReceiver(selfEventReceiver());
		this._httpClient.detach();
		return null;
	}

	public DownloadImageFlow2(
			final Pair<HttpResponse, List<byte[]>> part,
			final URI uri, 
			final ReceiverRemover channelRemover,
			final Visitor<Bitmap> bitmapVisitor,
			final Visitor2<HttpResponse, List<byte[]>> visitor2) {
		this._part = part;
		this._uri = uri;
		this._bitmapVisitor = bitmapVisitor;
		this._receiverRemover = channelRemover;
		this._uncompletedVisitor = visitor2;
	}
	
	public void setCanceller(final Detachable canceller) {
		this._pendingCanceller = canceller;
	}

	private Detachable	_pendingCanceller;
	private HttpClient	_httpClient;

	private final Pair<HttpResponse, List<byte[]>> _part;
	private final URI _uri;
	private final List<byte[]> _bytesList = new ArrayList<byte[]>();
	private final Visitor<Bitmap> _bitmapVisitor;
	private final ReceiverRemover _receiverRemover;
	private final Visitor2<HttpResponse, List<byte[]>> _uncompletedVisitor;
	private HttpRequest _request;
	private HttpResponse _response;

	private static HttpRequest genHttpRequest(final URI uri, Pair<HttpResponse, List<byte[]>> part) {
		// Prepare the HTTP request.
		final String host = uri.getHost() == null ? "localhost" : uri.getHost();

		final HttpRequest request = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
		request.headers().set(HttpHeaders.Names.HOST, host);
//		request.headers().set(HttpHeaders.Names.CONNECTION,
//				HttpHeaders.Values.KEEP_ALIVE);
		request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING,
				HttpHeaders.Values.GZIP);
		
		if ( null != part ) {
			//	add Range info
			request.headers().set(HttpHeaders.Names.RANGE, "bytes=" + sizeOf(part.second) + "-");
			final String etag = HttpHeaders.getHeader(part.first, HttpHeaders.Names.ETAG);
			if ( null != etag ) {
				request.headers().set(HttpHeaders.Names.IF_RANGE, etag);
			}
			LOG.info("uri {}, send partial get request, detail: Range:{}/If-Range:{}", uri, 
					request.headers().get(HttpHeaders.Names.RANGE), 
					request.headers().get(HttpHeaders.Names.IF_RANGE));
		}

		return request;
	}
	
	private static int sizeOf(final List<byte[]> bytesList) {
		int totalSize = 0;
		for ( byte[] bytes : bytesList) {
			totalSize += bytes.length;
		}
		
		return totalSize;
	}
}
