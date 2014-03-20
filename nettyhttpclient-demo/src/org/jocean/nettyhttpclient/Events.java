/**
 * 
 */
package org.jocean.nettyhttpclient;

/**
 * @author isdom
 *
 */
public class Events {
	static final String HTTPOBTAINED	= "_httpObtained";
	
	static final String HTTPLOST		= "_httpLost";
	
	//	params: HttpResponse response
	static final String HTTPRESPONSERECEIVED 	= "_httpResponseReceived";

	//	params: HttpContent content
	static final String HTTPCONTENTRECEIVED 	= "_httpContentReceived";

	//	params: LastHttpContent lastContent
	static final String LASTHTTPCONTENTRECEIVED= "_lastHttpContentReceived";
}
