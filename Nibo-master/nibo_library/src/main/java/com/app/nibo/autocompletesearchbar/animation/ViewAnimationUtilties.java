package com.app.nibo.autocompletesearchbar.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.lang.ref.WeakReference;


import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.app.nibo.autocompletesearchbar.animation.CircularRevealAnimator.CLIP_RADIUS;

public class ViewAnimationUtilties {

    private final static boolean LOLLIPOP_PLUS = SDK_INT >= LOLLIPOP;

    public static final int SCALE_UP_DURATION = 500;

    /**
     * Returns an Animator which can animate a clipping circle.
     * <p>
     * Any shadow cast by the View will respect the circular clip from this animator.
     * <p>
     * Only a single non-rectangular clip can be applied on a View at any time.
     * Views clipped by a circular reveal animation take priority over
     * {@link View#setClipToOutline(boolean) View Outline clipping}.
     * <p>
     * Note that the animation returned here is a one-shot animation. It cannot
     * be re-used, and once started it cannot be paused or resumed.
     *
     * @param view The View will be clipped to the animating circle.
     * @param centerX The x coordinate of the center of the animating circle.
     * @param centerY The y coordinate of the center of the animating circle.
     * @param startRadius The starting radius of the animating circle.
     * @param endRadius The ending radius of the animating circle.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static BaseSupportAnimator createCircularReveal(View view,
                                                           int centerX, int centerY,
                                                           float startRadius, float endRadius) {

        if(!(view.getParent() instanceof CircularRevealAnimator)){
            throw new IllegalArgumentException("View must be inside RevealFrameLayout or RevealLinearLayout.");
        }

        CircularRevealAnimator revealLayout = (CircularRevealAnimator) view.getParent();
        revealLayout.attachRevealInfo(new CircularRevealAnimator.RevealInfo(centerX, centerY, startRadius, endRadius,
                new WeakReference<>(view)));

        if(LOLLIPOP_PLUS){
            return new SupportAnimatorForLollipop(android.view.ViewAnimationUtils
                    .createCircularReveal(view, centerX, centerY, startRadius, endRadius), revealLayout);
        }

        ObjectAnimator reveal = ObjectAnimator.ofFloat(revealLayout, CLIP_RADIUS,
                startRadius, endRadius);
        reveal.addListener(getRevealFinishListener(revealLayout));

        return new SupportAnimatorPreLollipop(reveal, revealLayout);
    }

    private static Animator.AnimatorListener getRevealFinishListener(CircularRevealAnimator target){
        if(SDK_INT >= 18){
            return new CircularRevealAnimator.RevealFinishedJellyBeanMr2(target);
        }else if(SDK_INT >= 14){
            return new CircularRevealAnimator.RevealFinishedIceCreamSandwich(target);
        }else {
            return new CircularRevealAnimator.RevealFinishedGingerbread(target);
        }
    }

    /**
     * Lifting view
     *
     * @param view The animation target
     * @param baseRotation initial Rotation X in 3D space
     * @param fromY initial Y position of view
     * @param duration aniamtion duration
     * @param startDelay start delay before animation begin
     */
    @Deprecated
    public static void liftingFromBottom(View view, float baseRotation, float fromY, int duration, int startDelay){
        view.setRotationX(baseRotation);
        view.setTranslationY(fromY);

        view
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(duration)
                .setStartDelay(startDelay)
                .rotationX(0)
                .translationY(0)
                .start();

    }

    /**
     * Lifting view
     *
     * @param view The animation target
     * @param baseRotation initial Rotation X in 3D space
     * @param duration aniamtion duration
     * @param startDelay start delay before animation begin
     */
    @Deprecated
    public static void liftingFromBottom(View view, float baseRotation, int duration, int startDelay){
        view.setRotationX(baseRotation);
        view.setTranslationY(view.getHeight() / 3);

        view
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(duration)
                .setStartDelay(startDelay)
                .rotationX(0)
                .translationY(0)
                .start();

    }

    /**
     * Lifting view
     *
     * @param view The animation target
     * @param baseRotation initial Rotation X in 3D space
     * @param duration aniamtion duration
     */
    @Deprecated
    public static void liftingFromBottom(View view, float baseRotation, int duration){
        view.setRotationX(baseRotation);
        view.setTranslationY(view.getHeight() / 3);

        view
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(duration)
                .rotationX(0)
                .translationY(0)
                .start();

    }

    static class SimpleAnimationListener implements Animator.AnimatorListener{

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

}