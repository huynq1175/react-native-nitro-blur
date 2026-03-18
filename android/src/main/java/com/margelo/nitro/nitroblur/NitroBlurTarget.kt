package com.margelo.nitro.nitroblur

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.uimanager.ThemedReactContext
import eightbitlab.com.blurview.BlurTarget

@DoNotStrip
class HybridNitroBlurTarget(val context: ThemedReactContext) : HybridNitroBlurTargetSpec() {

  private val containerView = BlurTargetContainer(context)

  override val view: View get() = containerView

  val blurTargetView: BlurTarget get() = containerView.blurTargetView
}

/**
 * A container view that delegates child management to an inner BlurTarget.
 * This is needed because the Dimezis BlurView library requires a BlurTarget
 * as the root view to capture and blur its content.
 *
 * When adding a child to this view, we want to actually add it to the blur target view.
 * Because of this we need to override add, remove and measurement methods.
 */
@SuppressLint("ViewConstructor")
class BlurTargetContainer(context: ThemedReactContext) : ViewGroup(context) {

  internal val blurTargetView = ReactCompatibleBlurTarget(context)

  init {
    super.addView(
      blurTargetView,
      LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    )
  }

  // region Child delegation to blurTargetView

  override fun addView(child: View?) {
    if (child === blurTargetView) {
      super.addView(child)
      return
    }
    blurTargetView.addView(child)
  }

  override fun addView(child: View?, index: Int) {
    if (child === blurTargetView) {
      super.addView(child, index)
      return
    }
    blurTargetView.addView(child, index)
  }

  override fun addView(child: View?, params: LayoutParams?) {
    if (child === blurTargetView) {
      super.addView(child, toHostLayoutParams(params))
      return
    }
    blurTargetView.addView(child, params)
  }

  override fun addView(child: View?, index: Int, params: LayoutParams?) {
    if (child === blurTargetView) {
      super.addView(child, index, toHostLayoutParams(params))
      return
    }
    blurTargetView.addView(child, index, params)
  }

  override fun addView(child: View?, width: Int, height: Int) {
    if (child === blurTargetView) {
      super.addView(child, width, height)
      return
    }
    blurTargetView.addView(child, width, height)
  }

  override fun updateViewLayout(view: View?, params: LayoutParams?) {
    if (view === blurTargetView) {
      super.updateViewLayout(view, toHostLayoutParams(params))
      return
    }
    blurTargetView.updateViewLayout(view, params)
  }

  override fun removeView(view: View?) {
    if (view === blurTargetView) {
      super.removeView(view)
      return
    }
    blurTargetView.removeView(view)
  }

  override fun removeViewAt(index: Int) = blurTargetView.removeViewAt(index)
  override fun removeViews(start: Int, count: Int) = blurTargetView.removeViews(start, count)
  override fun removeViewsInLayout(start: Int, count: Int) = blurTargetView.removeViewsInLayout(start, count)
  override fun removeAllViews() = blurTargetView.removeAllViews()
  override fun removeAllViewsInLayout() = blurTargetView.removeAllViewsInLayout()
  override fun getChildCount(): Int = blurTargetView.childCount
  override fun getChildAt(index: Int): View? = blurTargetView.getChildAt(index)
  override fun indexOfChild(child: View?): Int = blurTargetView.indexOfChild(child)

  // endregion

  // region Layout

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = MeasureSpec.getSize(heightMeasureSpec)
    setMeasuredDimension(width, height)
    blurTargetView.measure(
      MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
      MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
    )
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    blurTargetView.layout(0, 0, right - left, bottom - top)
  }

  // endregion

  private fun toHostLayoutParams(params: LayoutParams?): LayoutParams = when (params) {
    null -> LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    is LayoutParams -> params
    else -> LayoutParams(params)
  }
}

/**
 * A BlurTarget compatible with React Native's layout system.
 * Overrides requestLayout and onLayout to avoid conflicts with React Native's UIManager,
 * which handles all layout operations for RN-managed views.
 */
class ReactCompatibleBlurTarget(context: ThemedReactContext) : BlurTarget(context) {
  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    // No-op since UIManager handles actually laying out children.
  }

  @SuppressLint("MissingSuperCall")
  override fun requestLayout() {
    // No-op, terminate requestLayout here, UIManager handles laying out children and
    // layout is called on all RN-managed views by NativeViewHierarchyManager
  }
}
