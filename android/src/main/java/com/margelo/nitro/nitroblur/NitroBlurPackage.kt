package com.margelo.nitro.nitroblur

import com.facebook.react.BaseReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfoProvider
import com.facebook.react.uimanager.ViewManager

import com.margelo.nitro.nitroblur.views.HybridNitroBlurManager

class NitroBlurPackage : BaseReactPackage() {
    override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
        return null
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        return ReactModuleInfoProvider { HashMap() }
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        // Use custom NitroBlurTargetViewGroupManager instead of generated HybridNitroBlurTargetManager
        // because BlurTargetView needs ViewGroupManager (supports children), not SimpleViewManager
        return listOf(HybridNitroBlurManager(), NitroBlurTargetViewGroupManager())
    }

    companion object {
        init {
            System.loadLibrary("nitroblur")
        }
    }
}
