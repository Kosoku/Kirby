package com.kosoku.kirby.extension

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout

/**
 * Extension method on [View] that uses the [DebounceOnClickListener] to handle the click event
 */
fun View.setDebounceOnClickListener(debounceInterval: Long = 1000, onClickDebounce: (View) -> Unit) =
    setOnClickListener(DebounceOnClickListener(debounceInterval, onClickDebounce))

/**
 * Extension method on [Toolbar] that uses the [DebounceOnClickListener] to handle the click event
 */
fun Toolbar.setDebounceMenuOnClickListener(debounceInterval: Long = 1000, onClickDebounce: (MenuItem) -> Boolean) =
    setOnMenuItemClickListener(DebounceMenuOnClickListener(debounceInterval, onClickDebounce))

/**
 * Extension method on [TextInputLayout] start icon that uses the [DebounceOnClickListener] to
 * handle the click event
 */
fun TextInputLayout.setStartIconDebounceOnClickListener(debounceInterval: Long = 1000, onClickDebounce: (View) -> Unit) =
    setStartIconOnClickListener(DebounceOnClickListener(debounceInterval, onClickDebounce))

/**
 * Extension method on [TextInputLayout] end icon that uses the [DebounceOnClickListener] to
 * handle the click event
 */
fun TextInputLayout.setEndIconDebounceOnClickListener(debounceInterval: Long = 1000, onClickDebounce: (View) -> Unit) =
    setEndIconOnClickListener(DebounceOnClickListener(debounceInterval, onClickDebounce))