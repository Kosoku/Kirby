package com.kosoku.kirby.fragment

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.lang.ref.WeakReference

abstract class KBYFragment : Fragment() {
    open val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    open val viewModel: ViewModel? = null

    open val title: String? = null

    open val hideNavigationIcon: Boolean = false

    open val replaceBackButtonWithCloseButton: Boolean = false

    open val backstackIdentifier: String? = null

    open val menuResourceId: Int = NO_OPTIONS_MENU

    open val menuOnClickListener: (MenuItem) -> Boolean = { false }

    open var binding: ViewDataBinding? = null

    open var navigationController: WeakReference<ModalNavigationFragment>? = null

    open var dismiss: () -> Unit = {}

    open fun configureOptionsMenu(menu: Menu, context: Context? = null) {}

    open fun wilNavigateBack(closure: (() -> Unit)? = null) {
        closure?.let { it() }
    }

    open fun wilDismiss(closure: (() -> Unit)? = null) {
        closure?.let { it() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
        binding = null
        disposables.clear()
    }

    companion object {
        const val NO_OPTIONS_MENU = -1
    }
}