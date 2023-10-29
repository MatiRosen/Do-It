package team.doit.do_it.listeners

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

class InsetsWithKeyboardCallback(
    private val window: Window,
    private val v: ConstraintLayout
) : OnApplyWindowInsetsListener, WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {

    private var lastWindowInsets: WindowInsetsCompat? = null
    private var deferredInsets = false
    private var shouldAdjustPan : Boolean = false
    private var windowSoftInputMode : Int = 0

    init {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        windowSoftInputMode = window.attributes.softInputMode
        shouldAdjustPan = true

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onApplyWindowInsets(v: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        if (!shouldAdjustPan) {
            return WindowInsetsCompat.CONSUMED
        }

        this.lastWindowInsets = insets

        val types = when {
            deferredInsets -> WindowInsetsCompat.Type.systemBars()
            else -> WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime()
        }

        val typeInsets = insets.getInsets(types)
        v.setPadding(typeInsets.left, typeInsets.top, typeInsets.right, typeInsets.bottom)
        return WindowInsetsCompat.CONSUMED
    }

    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
            deferredInsets = true
        }
    }

    override fun onProgress(insets: WindowInsetsCompat, runningAnimations: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        if (deferredInsets && (animation.typeMask and WindowInsetsCompat.Type.ime()) != 0) {
            deferredInsets = false

            if (lastWindowInsets != null) {
                ViewCompat.dispatchApplyWindowInsets(v, lastWindowInsets!!)
            }
        }
    }

    fun removeListener() {
        ViewCompat.dispatchApplyWindowInsets(v, lastWindowInsets!!)
        shouldAdjustPan = false

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            window.setSoftInputMode(windowSoftInputMode)
        }
    }
}