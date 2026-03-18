package com.margelo.nitro.nitroblur

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import com.facebook.proguard.annotations.DoNotStrip
import com.facebook.react.uimanager.ThemedReactContext
import eightbitlab.com.blurview.BlurView

private enum class BlurViewConfiguration {
  /** BlurView is yet to be configured. */
  UNCONFIGURED,
  /** BlurView has been configured to use the NONE blur method. */
  NONE,
  /** BlurView has been configured to use the DIMEZIS blur method. */
  DIMEZIS
}

@DoNotStrip
class HybridNitroBlur(val context: ThemedReactContext) : HybridNitroBlurSpec() {

  private val containerView = BlurContainerView(context)

  private val blurView = BlurView(context).also {
    it.layoutParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    )
    containerView.addView(it)
  }

  override val view: View get() = containerView

  // Internal state
  private var _blurMethod: BlurMethod = BlurMethod.NONE
  private var _blurReduction = 4.0
  private var _blurRadius = 50.0
  private var _tint: BlurTint = BlurTint.DEFAULT
  private var _blurTargetId = 0.0
  private var blurConfiguration = BlurViewConfiguration.NONE
  private var blurTargetView: eightbitlab.com.blurview.BlurTarget? = null

  // region Props

  override var tint: BlurTint
    get() = _tint
    set(value) {
      _tint = value
      applyTint()
    }

  override var intensity: Double
    get() = _blurRadius
    set(value) {
      _blurRadius = value
      setBlurRadius(value)
    }

  override var blurReductionFactor: Double
    get() = _blurReduction
    set(value) {
      _blurReduction = value
      setBlurRadius(_blurRadius)
    }

  override var blurTargetId: Double
    get() = _blurTargetId
    set(value) {
      val intId = value.toInt()
      val currentIntId = _blurTargetId.toInt()
      if (intId == currentIntId) return

      _blurTargetId = value

      if (intId <= 0) {
        blurTargetView = null
      } else {
        // Find the blur target view by its React Native tag (view ID)
        val rootView = context.currentActivity?.window?.decorView
        val targetView = rootView?.findViewById<View>(intId)
        val targetContainer = targetView as? BlurTargetContainer
        blurTargetView = targetContainer?.blurTargetView
      }

      configureBlurView()
    }

  override var blurMethod: BlurMethod
    get() = _blurMethod
    set(value) {
      _blurMethod = value

      // Re-configure if the method was changed from none -> dimezis at runtime
      if (value != BlurMethod.NONE && blurConfiguration != BlurViewConfiguration.DIMEZIS) {
        configureBlurView()
        applyTint()
        setBlurRadius(_blurRadius)
      }

      val safeMethod = if (blurTargetView != null) value else BlurMethod.NONE

      if (blurConfiguration == BlurViewConfiguration.UNCONFIGURED) return

      when (safeMethod) {
        BlurMethod.NONE -> {
          blurView.setBlurEnabled(false)
        }
        BlurMethod.DIMEZISBLURVIEW -> {
          blurView.setBlurEnabled(true)
          containerView.setBackgroundColor(Color.TRANSPARENT)
        }
        BlurMethod.DIMEZISBLURVIEWSDK31PLUS -> {
          val isNewSdk = Build.VERSION.SDK_INT >= 31
          blurView.setBlurEnabled(isNewSdk)
          if (isNewSdk) {
            containerView.setBackgroundColor(Color.TRANSPARENT)
          }
        }
      }
      // Update blur to the current blurRadius value
      setBlurRadius(_blurRadius)
    }

  // endregion

  // region Configuration

  private fun configureBlurView() {
    val target = blurTargetView
    if (target == null || _blurMethod == BlurMethod.NONE) {
      blurView.setBlurEnabled(false)
      blurConfiguration = BlurViewConfiguration.NONE
      return
    }

    val decorView = context.currentActivity?.window?.decorView ?: run {
      blurConfiguration = BlurViewConfiguration.NONE
      return
    }

    blurView.setupWith(target)
      .setFrameClearDrawable(decorView.background)
      .setBlurRadius(_blurRadius.toFloat())

    blurConfiguration = BlurViewConfiguration.DIMEZIS
  }

  /**
   * Apply blur settings that may have been set before the BlurView was configured.
   */
  private fun applyCurrentBlurSettings() {
    setBlurRadius(_blurRadius)
    this.blurMethod = _blurMethod
    applyTint()
  }

  // endregion

  // region Blur application

  private fun setBlurRadius(radius: Double) {
    if (blurConfiguration == BlurViewConfiguration.UNCONFIGURED) return

    when (_blurMethod) {
      BlurMethod.NONE -> {
        applyBlurViewRadiusCompat(useBlur = false, radius = radius)
      }
      BlurMethod.DIMEZISBLURVIEW -> {
        applyBlurViewRadiusCompat(useBlur = true, radius = radius)
      }
      BlurMethod.DIMEZISBLURVIEWSDK31PLUS -> {
        applyBlurViewRadiusCompat(useBlur = Build.VERSION.SDK_INT >= 31, radius = radius)
      }
    }
  }

  fun applyTint() {
    if (blurConfiguration == BlurViewConfiguration.UNCONFIGURED) return

    when (_blurMethod) {
      BlurMethod.NONE -> {
        applyBlurViewOverlayColorCompat(useBlurView = false)
      }
      BlurMethod.DIMEZISBLURVIEW -> {
        applyBlurViewOverlayColorCompat(useBlurView = true)
      }
      BlurMethod.DIMEZISBLURVIEWSDK31PLUS -> {
        applyBlurViewOverlayColorCompat(useBlurView = Build.VERSION.SDK_INT >= 31)
      }
    }
    blurView.invalidate()
  }

  private fun applyBlurViewRadiusCompat(useBlur: Boolean, radius: Double) {
    if (useBlur && blurTargetView != null) {
      // When setting a blur directly to 0 a "nativePtr is null" exception is thrown
      // https://issuetracker.google.com/issues/241546169
      blurView.setBlurEnabled(radius != 0.0)
      if (radius > 0) {
        blurView.setBlurRadius((radius / _blurReduction).toFloat())
        blurView.invalidate()
      }
    } else {
      containerView.setBackgroundColor(_tint.toOverlayColor(_blurRadius))
    }
  }

  private fun applyBlurViewOverlayColorCompat(useBlurView: Boolean) {
    if (useBlurView && blurTargetView != null) {
      blurView.setOverlayColor(_tint.toOverlayColor(_blurRadius))
    } else {
      containerView.setBackgroundColor(_tint.toOverlayColor(_blurRadius))
    }
  }

  // endregion
}

/**
 * Simple container ViewGroup that lays out children to fill bounds.
 */
private class BlurContainerView(context: android.content.Context) : ViewGroup(context) {
  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    for (i in 0 until childCount) {
      getChildAt(i).layout(0, 0, r - l, b - t)
    }
  }
}
