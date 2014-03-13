/**
 * 
 */
package org.jocean.nettyhttpclient;

import io.netty.channel.Channel;

/**
 * @author isdom
 *
 */
public interface ChannelRemover {
	public void removeChannel(final Channel channel);
}
