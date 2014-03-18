/**
 * 
 */
package org.jocean.nettyhttpclient;

import java.net.URI;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.ClientCookieEncoder;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

import org.jocean.syncfsm.api.AbstractFlow;
import org.jocean.syncfsm.api.BizStep;
import org.jocean.syncfsm.api.EventHandler;
import org.jocean.syncfsm.api.annotion.OnEvent;
import org.jocean.syncfsm.api.annotion.SameThread;
import org.jocean.transportclient.TransportEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author isdom
 *
 */
@SameThread
public class HttpDemoFlow extends AbstractFlow {
	  private static final Logger LOG =
              LoggerFactory.getLogger(HttpDemoFlow.class);
	  
	  /*
	  public final BizStep OBTAINING =
           new BizStep("demo.UNCONNECTED")
           .handler( selfInvoker("onActive") )
           .handler( selfInvoker("onInactive") )
           .freeze();
                       
       public final BizStep RECVRESP =
           new BizStep("demo.RECVRESP")  
           .handler( selfInvoker( "responseReceived") )
           .handler( selfInvoker( "contentReceived") )
           .handler( selfInvoker( "lastContentReceived") )
           .handler( selfInvoker("onInactive") )
           .freeze();
 
        @OnEvent(event=TransportEvents.EVENT_CHANNELACTIVE)
        private EventHandler onActive(final ChannelHandlerContext ctx) {
            ctx.channel().writeAndFlush(genHttpRequest(_uri));
            return RECVRESP;
        }
          
        @OnEvent(event=TransportEvents.EVENT_CHANNELINACTIVE)
        private EventHandler onInactive(final ChannelHandlerContext ctx) {
            return null;
        }
        
        @OnEvent(event=TransportEvents.EVENT_HTTPRESPONSERECEIVED)
        private EventHandler responseReceived(final ChannelHandlerContext ctx, final HttpResponse response) {
            LOG.info("dump for uri {}", _uri);
            LOG.info("STATUS: {}", response.getStatus());
            LOG.info("VERSION: {}", response.getProtocolVersion());

            if (!response.headers().isEmpty()) {
                for (String name: response.headers().names()) {
                    for (String value: response.headers().getAll(name)) {
                        LOG.info("HEADER: {}={}", name, value);
                    }
                }
            }

            if (HttpHeaders.isTransferEncodingChunked(response)) {
                LOG.info("CHUNKED CONTENT {");
            } else {
                LOG.info("CONTENT {");
            }
            
            return RECVRESP;
        }
        
        @OnEvent(event=TransportEvents.EVENT_HTTPCONTENTRECEIVED)
        private EventHandler contentReceived(final ChannelHandlerContext ctx, final HttpContent content) {
            LOG.info("{}: body size {}", _uri, 
            		content.content().toString(CharsetUtil.UTF_8).length());
            return RECVRESP;
        }
        
        @OnEvent(event=TransportEvents.EVENT_LASTHTTPCONTENTRECEIVED)
        private EventHandler lastContentReceived(final ChannelHandlerContext ctx, final LastHttpContent content) {
            LOG.info("{}: body size {}", _uri, 
            		content.content().toString(CharsetUtil.UTF_8).length());
            LOG.info("} END OF CONTENT for uri {}", _uri);
            return RECVRESP;
        }
        
    	public HttpDemoFlow(final URI uri) {
    		this._uri = uri;
    	}
    	
    	private final URI _uri;

    	private static HttpRequest genHttpRequest(final URI uri) {
    	    // Prepare the HTTP request.
            final String host = uri.getHost() == null? "localhost" : uri.getHost();

    	    final HttpRequest request = new DefaultFullHttpRequest(
    	            HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
    	    request.headers().set(HttpHeaders.Names.HOST, host);
    	    request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
    	    request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
    	
    	    // Set some example cookies.
    	    request.headers().set(
    	            HttpHeaders.Names.COOKIE,
    	            ClientCookieEncoder.encode(
    	                    new DefaultCookie("my-cookie", "foo"),
    	                    new DefaultCookie("another-cookie", "bar")));
    	    
    	    return	request;
    	}
    	*/
}
