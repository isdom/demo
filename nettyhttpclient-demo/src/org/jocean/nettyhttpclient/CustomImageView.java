/**
 * 
 */
package org.jocean.nettyhttpclient;

import org.jocean.syncfsm.api.EventReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author isdom
 * 
 */
public class CustomImageView extends ImageView {

	private static final Logger LOG = LoggerFactory
			.getLogger("CustomImageView");
	
	public CustomImageView(Context context) {
		super(context);
	}

	public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("in onDraw call for uri {}", this.getTag());
		}
		super.onDraw(canvas);
		
		if ( null != this._eventReceiver ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug("try to send onDraw event for uri {}", this.getTag());
			}
			try {
				this._eventReceiver.acceptEvent("onDraw", this, canvas);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void setEventReceiver(final EventReceiver receiver) {
		this._eventReceiver = receiver;
		if ( LOG.isDebugEnabled() ) {
			LOG.debug("set uri {} CustomImageView's eventReceiver with {}", this.getTag(), receiver);
		}
	}

	private EventReceiver _eventReceiver = null;
}
