package igrek.todotree.logic.touchcontroller;

public interface ITouchController {
    boolean onTouchDown(float x, float y);

    boolean onTouchMove(float x, float y);

    boolean onTouchUp(float x, float y);
}
