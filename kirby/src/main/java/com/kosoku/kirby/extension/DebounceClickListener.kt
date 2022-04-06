package com.kosoku.kirby.extension

import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar

class DebounceOnClickListener constructor(
    private val debounceInterval: Long = 1000,
    private val onclickDebounce: (View) -> Unit
) : View.OnClickListener {
    private var lastClickEvent: Long = 0

    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastClickEvent < debounceInterval) {
            return
        }

        lastClickEvent = SystemClock.elapsedRealtime()
        onclickDebounce(v)
    }
}

class DebounceMenuOnClickListener constructor(
    private val debounceInterval: Long = 1000,
    private val onclickDebounce: (MenuItem) -> Boolean
) : Toolbar.OnMenuItemClickListener {
    private var lastClickEvent: Long = 0

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (SystemClock.elapsedRealtime() - lastClickEvent < debounceInterval) {
            return false
        }

        lastClickEvent = SystemClock.elapsedRealtime()
        return onclickDebounce(item)
    }
}

class DebounceOnClick constructor(
    private val debounceInterval: Long = 1000,
    private val onclickDebounce: () -> Unit
) : () -> Unit {
    private var lastClickEvent: Long = 0

    override fun invoke() {
        if (SystemClock.elapsedRealtime() - lastClickEvent < debounceInterval) {
            return
        }

        lastClickEvent = SystemClock.elapsedRealtime()
        onclickDebounce()
    }
}