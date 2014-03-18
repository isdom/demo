/**
 * 
 */
package org.jocean.nettyhttpclient;

import org.jocean.syncfsm.api.EventReceiver;

/**
 * @author isdom
 *
 */
public interface ReceiverRemover {
	public void removeReceiver(final EventReceiver receiver);
}
