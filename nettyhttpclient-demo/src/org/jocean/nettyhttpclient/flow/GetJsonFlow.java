/**
 * 
 */
package org.jocean.nettyhttpclient.flow;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.jocean.idiom.ByteArrayListInputStream;
import org.jocean.syncfsm.api.AbstractFlow;
import org.jocean.syncfsm.api.BizStep;
import org.jocean.syncfsm.api.EventHandler;
import org.jocean.syncfsm.api.annotion.OnEvent;
import org.jocean.transportclient.api.HttpClient;
import org.jocean.transportclient.api.HttpClientHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author isdom
 *
 */
public class GetJsonFlow extends AbstractFlow<GetJsonFlow> {
	
	private static final Logger LOG = LoggerFactory
			.getLogger("GetJsonFlow");

	public final BizStep OBTAINING = new BizStep("getjson.OBTAINING")
			.handler(selfInvoker("onHttpObtained"))
			.handler(selfInvoker("onHttpLost"))
			.handler(selfInvoker("onCanceled"))
			.freeze();

	private final BizStep RECVRESP = new BizStep("getjson.RECVRESP")
			.handler(selfInvoker("responseReceived"))
			.handler(selfInvoker("onHttpLost"))
			.handler(selfInvoker("onCanceled"))
			.freeze();

	private final BizStep RECVCONTENT = new BizStep("getjson.RECVCONTENT")
			.handler(selfInvoker("contentReceived"))
			.handler(selfInvoker("lastContentReceived"))
			.handler(selfInvoker("onCanceled"))
			.handler(selfInvoker("onHttpLost"))
			.freeze();

	@OnEvent(event="cancel")
	private EventHandler onCanceled() throws Exception {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("download {} progress canceled", _uri);
		}
		this._handle.detach();
		return null;
		
	}
	
	@OnEvent(event = "onHttpClientLost")
	private EventHandler onHttpLost()
			throws Exception {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("http for {} lost.", _uri);
		}
		return null;
	}

	@OnEvent(event = "onHttpClientObtained")
	private EventHandler onHttpObtained(final HttpClient httpclient) {
		// save http request
		final HttpRequest request = genHttpRequest(this._uri);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("send http request {}", request);
		}
		httpclient.sendHttpRequest( request );
		return RECVRESP;
	}

	@OnEvent(event = "onHttpResponseReceived")
	private EventHandler responseReceived(final HttpResponse response) {
		LOG.debug("channel for {} recv response {}", _uri, response);
		final String contentType = response.headers().get(HttpHeaders.Names.CONTENT_TYPE);
		if ( contentType != null && contentType.startsWith("application/json")) {
			LOG.info("get json succeed");
			return RECVCONTENT;
		}
		else {
			LOG.info("get json failed, wrong contentType {}", contentType);
			return	null;
		}
	}

	@OnEvent(event = "onHttpContentReceived")
	private EventHandler contentReceived(final HttpContent content) {
		final byte[] bytes = content.content().array();
		_bytesList.add(bytes);
		LOG.debug("channel for {} recv content, size {}", _uri, bytes.length);
		return RECVCONTENT;
	}

	@OnEvent(event = "onLastHttpContentReceived")
	private EventHandler lastContentReceived(final LastHttpContent content) throws Exception {
		final byte[] bytes = content.content().array();
		_bytesList.add(bytes);
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("channel for {} recv last content, size {}", this._uri,
					bytes.length);
		}
		
		this._handle.detach();
		final InputStream is = new ByteArrayListInputStream(_bytesList);
		final byte[] totalbytes = new byte[sizeOf(_bytesList)];
		is.read(totalbytes);
		is.close();
		LOG.info("get json for uri:{} succeed. detail: {}", this._uri, new String(totalbytes) );
		return null;
	}

	public GetJsonFlow(
			final HttpClientHandle handle,
			final URI uri) {
		this._handle = handle;
		this._uri = uri;
	}
	
	public final HttpClientHandle	_handle;

	private final URI _uri;
	private final List<byte[]> _bytesList = new ArrayList<byte[]>();

	private static HttpRequest genHttpRequest(final URI uri) {
		// Prepare the HTTP request.
		final String host = uri.getHost() == null ? "localhost" : uri.getHost();

		final HttpRequest request = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
		request.headers().set(HttpHeaders.Names.HOST, host);
		request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING,
				HttpHeaders.Values.GZIP);
		
		return request;
	}
	
	private static int sizeOf(final List<byte[]> bytesList) {
		int totalSize = 0;
		for ( byte[] bytes : bytesList) {
			totalSize += bytes.length;
		}
		
		return totalSize;
	}

	@Override
	public String toString() {
		return "GetJsonFlow, uri:" + this._uri;
	}
	
}
