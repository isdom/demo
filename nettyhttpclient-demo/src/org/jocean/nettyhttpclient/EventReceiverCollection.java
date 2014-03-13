/**
 * 
 */
package org.jocean.nettyhttpclient;

import org.jocean.syncfsm.api.EventReceiver;

/**
 * @author isdom
 *
 */
public interface EventReceiverCollection {
	
	public void addEventReceiver(final EventReceiver eventReceiver);
	
	public void removeEventReceiver(final EventReceiver eventReceiver);
}
