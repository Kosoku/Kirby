package com.kosoku.kirby.fragment

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModel
import com.kosoku.kirby.BuildConfig
import com.kosoku.kirby.R
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.lang.ref.WeakReference
import java.util.*

abstract class KBYFragment : DialogFragment() {
    var isModal: Boolean = false
        protected set

    open val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    open val viewModel: ViewModel? = null

    open val title: String? = null

    open val hideNavigationIcon: Boolean = false

    open val replaceBackButtonWithCloseButton: Boolean = false

    open val backstackIdentifier: String = UUID.randomUUID().toString()

    open val menuResourceId: Int = NO_OPTIONS_MENU

    open val menuOnClickListener: (MenuItem) -> Boolean = { false }

    open var binding: ViewDataBinding? = null

    open var navigationController: WeakReference<NavigationFragment>? = null

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
            showsDialog = true
        } else {
            showsDialog = false
        }
    }

    override fun dismiss() {
        if (isModal) {
            super.dismiss()
        } else {
            (parentFragment as? NavigationFragment)?.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.unbind()
        binding = null
        disposables.clear()
    }

    protected fun getModalInstance(bundle: Bundle? = null): KBYFragment {
        val retval = this
        retval.arguments = bundle?.apply { putBoolean(IS_MODAL_KEY, true) } ?: bundleOf(IS_MODAL_KEY to true)
        return retval
    }

    protected fun getInstance(bundle: Bundle? = null): KBYFragment {
        val retval = this
        retval.arguments = bundle
        return retval
    }

    companion object {
        private const val IS_MODAL_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.isModalKey"
        const val NO_OPTIONS_MENU = -1
    }
}