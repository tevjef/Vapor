package deadpixel.app.vapor.libs;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tevin on 7/12/2014.
 */
public class TransitionButton extends Button {

    final private int DEFAULT_DURATION = 500;



    public enum BtnType {
        ERROR, SUCCESS, PROGRESS
    }
    
    private BtnType type = null;
    private CharSequence[] buttonText= null;
    private CharSequence fromText = null;
    private CharSequence toText = null;
    private boolean indefinite = false;
    private int duration = 4000;

    private TransitionDrawable mTransitionDrawable;
    private List<OnTransitionListener> listeners = new ArrayList<OnTransitionListener>();
    private Drawable errorDrawable;
    private Drawable successDrawable;
    private Drawable normalDrawable;
    private Drawable progressDrawable;
    private Context mContext;
    private boolean transitioning = false;

    private void reset() {
        type = null;
        buttonText= null;
        fromText = null;
        toText = null;
        duration = 4000;
        indefinite = false;
    }

    public TransitionButton(Context context) {
        super(context);
    }

    public TransitionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransitionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TransitionDrawable getTransition(){
        return mTransitionDrawable;
    }

    public TransitionButton setBackgroundAsTransition(Drawable drawable){
        setBackgroundDrawable(drawable);
        return this;
    }

    public TransitionButton setSelectors(int fromSelector, int toSelector) {
        Drawable[] drawableArray = {mContext.getResources().getDrawable(fromSelector), mContext.getResources().getDrawable(toSelector)};
        mTransitionDrawable = new TransitionDrawable(drawableArray);
        return this;
    }
    
    public TransitionButton from(Context context) {
        mContext = context;
        return this;
    }
    
    public TransitionButton setType(BtnType type) {
        Drawable[] drawableArray;
        switch (type) {
            case ERROR:
                drawableArray = new Drawable[]{normalDrawable, errorDrawable};
                mTransitionDrawable = new TransitionDrawable(drawableArray);
                break;
            case SUCCESS:
                drawableArray = new Drawable[]{normalDrawable, successDrawable};
                mTransitionDrawable = new TransitionDrawable(drawableArray);
                break;
            case PROGRESS:
                drawableArray = new Drawable[]{normalDrawable, progressDrawable};
                mTransitionDrawable = new TransitionDrawable(drawableArray);
                break;
            default:
                break;
        }

        setBackgroundAsTransition(mTransitionDrawable);
        return this;
    }

    public TransitionButton setErrorSelector(int error) {
        errorDrawable = mContext.getResources().getDrawable(error);
        return this;
    }
    public TransitionButton setNormalSelector(int normal) {
        normalDrawable = mContext.getResources().getDrawable(normal);
        return this;
    }

    public TransitionButton setSuccessSelector(int success) {
        successDrawable = mContext.getResources().getDrawable(success);
        return this;
    }

    public TransitionButton setProgressSelector(int progress) {
        progressDrawable = mContext.getResources().getDrawable(progress);
        return this;
    }
    public TransitionButton setTransitionText(CharSequence fromText, CharSequence toText) {
        if(this.fromText == null && this.toText == null)
            buttonText =  new CharSequence[]{fromText, toText};
        else {
            buttonText = new CharSequence[]{this.fromText, this.toText};
            Log.i("TransitionButton", "setTransitionText() should not have been called." + "from: " + this.fromText + " to: " + this.toText);
        }
        return this;
    }

    public TransitionButton setFromText(CharSequence fromText) {
        this.fromText = fromText;
        setTransitionText(null, null);
        return this;
    }
    public TransitionButton setToText(CharSequence toText) {
        this.toText = toText;
        setTransitionText(null, null);
        return this;
    }
    public TransitionButton setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public void start() {

        final int TIME_TO_TRANSITION = (int) (duration * .15);
        this.setEnabled(false);
        this.setClickable(false);
        transitioning = true;

        notifyTransitionStart();
        mTransitionDrawable.startTransition(TIME_TO_TRANSITION);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setButtonText(buttonText[1]);
            }
        }, TIME_TO_TRANSITION/2);

        if(!indefinite) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTransitionDrawable.reverseTransition(TIME_TO_TRANSITION);
                }
            }, duration - TIME_TO_TRANSITION);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    setButtonText(buttonText[0]);

                }
            }, (duration - (TIME_TO_TRANSITION) / 2));


        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setEnabled(true);
                setClickable(true);
                reset();
                transitioning = false;
                notifyTransitionEnd();
            }
        }, duration);
    }
    private synchronized void notifyTransitionEnd() {
        for(OnTransitionListener l : listeners){
            l.OnTransitionEnd(getThis());
        }
    }
    private synchronized void notifyTransitionStart() {
        for(OnTransitionListener l : listeners){
            l.OnTransitionStart(getThis());
        }
    }
    private TransitionButton getThis() {
        return this;
    }

    public synchronized void setTransitionListener(OnTransitionListener l) {
        listeners.add(l);
    }

    private void setButtonText(CharSequence text) {
        this.setText(text);
    }

    public TransitionButton setIndefinite(boolean b) {
        indefinite = b;
        return this;
    }


    public interface OnTransitionListener {

        public void OnTransitionStart(TransitionButton button);

        public void OnTransitionEnd(TransitionButton button);
    }

}
