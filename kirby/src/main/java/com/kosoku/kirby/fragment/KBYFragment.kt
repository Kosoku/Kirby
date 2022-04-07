package com.kosoku.kirby.fragment

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.kosoku.kirby.BuildConfig
import com.kosoku.kirby.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import timber.log.Timber
import java.lang.ref.WeakReference

abstract class KBYFragment : DialogFragment() {
    protected var isModal: Boolean = false

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

    open fun configureOptionsMenu(menu: Menu, context: Context? = null) {}

    open fun wilNavigateBack(closure: (() -> Unit)? = null) {
        closure?.let { it() }
    }

    open fun wilDismiss(closure: (() -> Unit)? = null) {
        closure?.let { it() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isModal = arguments?.getBoolean(IS_MODAL_KEY) ?: false
        if (isModal) {
            setStyle(STYLE_NORMAL, R.style.Theme_Kirby_Modal)
            isCancelable = false
        }
    }

    override fun dismiss() {
        if (isModal) {
            super.dismiss()
        } else {
            (parentFragment as? DialogFragment)?.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
        binding = null
        disposables.clear()
    }

    protected fun getModalInstance(): KBYFragment {
        val retval = this
        retval.arguments = bundleOf(IS_MODAL_KEY to true)
        return retval
    }

    companion object {
        private const val IS_MODAL_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.isModalKey"
        const val NO_OPTIONS_MENU = -1
    }
}