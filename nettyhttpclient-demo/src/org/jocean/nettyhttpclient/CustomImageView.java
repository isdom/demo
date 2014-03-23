/**
 * 
 */
package org.jocean.nettyhttpclient;

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
		
		if ( null != this._drawable ) {
			this._drawable.drawOnView(this, canvas);
		}

	}

	public void setDrawable(final DrawableOnView drawable) {
		this._drawable = drawable;
	}

	private DrawableOnView _drawable = null;
}
