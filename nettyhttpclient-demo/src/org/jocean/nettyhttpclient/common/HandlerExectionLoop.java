/**
 * 
 */
package org.jocean.nettyhttpclient.common;

import org.jocean.syncfsm.api.ExectionLoop;

import android.os.Handler;

/**
 * @author isdom
 *
 */
public class HandlerExectionLoop implements ExectionLoop {

    @Override
    public boolean inExectionLoop() {
        return (Thread.currentThread().getId() == this._handler.getLooper().getThread().getId());
    }

    @Override
    public void submit(final Runnable runnable) {
        this._handler.post(runnable);
    }

    @Override
    public void schedule(Runnable runnable, long delayMillis) {
        this._handler.postDelayed(runnable, delayMillis);
    }

    public HandlerExectionLoop(final Handler handler) {
        this._handler = handler;
    }
    
    private final Handler _handler;
}
