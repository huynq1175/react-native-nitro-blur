package com.margelo.nitro.nitroblur

import android.view.ViewGroup
import com.facebook.react.uimanager.ReactStylesDiffMap
import com.facebook.react.uimanager.StateWrapper
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.margelo.nitro.R.id.associated_hybrid_view_tag

/**
 * Custom ViewGroupManager for NitroBlurTarget.
 *
 * The Nitrogen-generated HybridNitroBlurTargetManager extends SimpleViewManager,
 * which does not support children. BlurTargetView must accept children so we need
 * ViewGroupManager instead.
 */
class NitroBlurTargetViewGroupManager : ViewGroupManager<BlurTargetContainer>() {
  override fun getName(): String = "NitroBlurTarget"

  override fun createViewInstance(reactContext: ThemedReactContext): BlurTargetContainer {
    val hybridView = HybridNitroBlurTarget(reactContext)
    val view = hybridView.view as BlurTargetContainer
    view.setTag(associated_hybrid_view_tag, hybridView)
    return view
  }

  override fun updateState(view: BlurTargetContainer, props: ReactStylesDiffMap, stateWrapper: StateWrapper): Any? {
    val hybridView = getHybridView(view)
      ?: throw Error("Couldn't find view $view in local views table!")

    hybridView.beforeUpdate()
    com.margelo.nitro.nitroblur.views.HybridNitroBlurTargetStateUpdater.updateViewProps(hybridView, stateWrapper)
    hybridView.afterUpdate()

    return super.updateState(view, props, stateWrapper)
  }

  override fun onDropViewInstance(view: BlurTargetContainer) {
    val hybridView = getHybridView(view)
    hybridView?.onDropView()
    return super.onDropViewInstance(view)
  }

  private fun getHybridView(view: ViewGroup): HybridNitroBlurTarget? {
    return view.getTag(associated_hybrid_view_tag) as? HybridNitroBlurTarget
  }
}
