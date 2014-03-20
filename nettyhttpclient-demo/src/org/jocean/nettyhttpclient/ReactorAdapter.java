/**
 * 
 */
package org.jocean.nettyhttpclient;

import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;

import org.jocean.syncfsm.api.EventReceiver;
import org.jocean.transportclient.api.HttpClient;
import org.jocean.transportclient.api.HttpReactor;

/**
 * @author isdom
 *
 */
public class ReactorAdapter implements HttpReactor {
	
	@Override
	public void onHttpClientObtained(final HttpClient httpClient)
			throws Exception {
		this._receiver.acceptEvent(Events.HTTPOBTAINED, httpClient);
	}

	@Override
	public void onHttpClientLost() throws Exception {
		this._receiver.acceptEvent(Events.HTTPLOST);
	}

	@Override
	public void onHttpResponseReceived(final HttpResponse response)
			throws Exception {
		this._receiver.acceptEvent(Events.HTTPRESPONSERECEIVED, response);
	}

	@Override
	public void onHttpContentReceived(final HttpContent content) throws Exception {
		this._receiver.acceptEvent(Events.HTTPCONTENTRECEIVED, content);
	}

	@Override
	public void onLastHttpContentReceived(final LastHttpContent content)
			throws Exception {
		this._receiver.acceptEvent(Events.LASTHTTPCONTENTRECEIVED, content);
	}

	public ReactorAdapter(final EventReceiver receiver) {
		this._receiver = receiver;
	}
	
	private final EventReceiver _receiver;
}
