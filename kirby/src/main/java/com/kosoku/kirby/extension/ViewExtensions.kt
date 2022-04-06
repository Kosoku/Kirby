package com.kosoku.kirby.extension

import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout

fun View.setDebounceOnClickListener(debounceInterval: Long = 1000, onClickDebounce: (View) -> Unit) =
    setOnClickListener(DebounceOnClickListener(debounceInterval, onClickDebounce))

fun Toolbar.setDebounceMenuOnClickListener(debounceInterval: Long = 1000, onClickDebounce: (MenuItem) -> Boolean) =
    setOnMenuItemClickListener(DebounceMenuOnClickListener(debounceInterval, onClickDebounce))

fun TextInputLayout.setStartIconDebounceOnClickListener(debounceInterval: Long = 1000, onClickDebounce: (View) -> Unit) =
    setStartIconOnClickListener(DebounceOnClickListener(debounceInterval, onClickDebounce))

fun TextInputLayout.setEndIconDebounceOnClickListener(debounceInterval: Long = 1000, onClickDebounce: (View) -> Unit) =
    setEndIconOnClickListener(DebounceOnClickListener(debounceInterval, onClickDebounce))