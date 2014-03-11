/**
 * 
 */
package org.jocean.syncfsm.demo;

import org.jocean.syncfsm.api.AbstractFlow;
import org.jocean.syncfsm.api.BizStep;
import org.jocean.syncfsm.api.EventHandler;
import org.jocean.syncfsm.api.annotion.OnEvent;
import org.jocean.syncfsm.api.annotion.SameThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.widget.TextView;

/**
 * @author isdom
 *
 */
@SameThread
public class DemoFlow extends AbstractFlow {
	  private static final Logger LOG =
              LoggerFactory.getLogger(DemoFlow.class);
	  
	  final BizStep LOCKED =
           new BizStep("LOCKED")
           .handler( selfInvoker("onCoin") )
           .freeze();
                       
       final BizStep UNLOCKED =
           new BizStep("UNLOCKED")  
           .handler( selfInvoker( "onPass") )
           .freeze();
 
        @OnEvent(event="coin")
        EventHandler onCoin(final TextView v) {
            LOG.info("{}: accept {}", 
            		new Object[]{currentEventHandler().getName(),  currentEvent()});
            v.setText(UNLOCKED.getName());
            return UNLOCKED;
        }
          
        @OnEvent(event="pass")
        EventHandler onPass(final TextView v) {
            LOG.info("{}: accept {}", new Object[]{
                            currentEventHandler().getName(),  currentEvent()
                    });
            v.setText(LOCKED.getName());
            return LOCKED;
        }
        
    	public EventHandler	currentEventHandler() {
    		return super.currentEventHandler();
    	}
}
