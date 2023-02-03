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

/**
 * A fragment that mimics UIViewController on iOS, with modal presentation capability. Also includes
 * a number of properties for use in tandem with [NavigationFragment] for handling backstack
 * navigation and screen title
 */
abstract class KBYFragment : DialogFragment() {
    /**
     * Read-only property to determine if the view is being presented as a modal
     */
    var isModal: Boolean = false
        protected set

    /**
     * RxCompositeDisposable for handling cleanup of Rx streams
     */
    @Deprecated("This property will be moved to KirbyRxExtensions in the future.")
    open val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    /**
     * The view model to be used by the fragment
     */
    open val viewModel: ViewModel? = null

    /**
     * The title to be displayed on the screen when contained in a [NavigationFragment]
     */
    open val title: String? = null

    /**
     * Hides the navigation button (back/close) when contained in a [NavigationFragment]
     */
    open val hideNavigationIcon: Boolean = false

    /**
     * Changes the navigation button from back to close when contained in a [NavigationFragment]
     */
    open val replaceBackButtonWithCloseButton: Boolean = false

    /**
     * A unique identifier to use with [NavigationFragment.popToIdentifier]
     */
    open val backstackIdentifier: String = UUID.randomUUID().toString()

    /**
     * A resource identifier for showing an options menu when contained in a [NavigationFragment]
     */
    open val menuResourceId: Int = NO_OPTIONS_MENU

    /**
     * A handler for options menu item click when contained in a [NavigationFragment]
     */
    open val menuOnClickListener: (MenuItem) -> Boolean = { false }

    /**
     * View binding used by DataBindings
     */
    open var binding: ViewDataBinding? = null

    /**
     * A weak reference to the containing [NavigationFragment]
     */
    open var navigationController: WeakReference<NavigationFragment>? = null

    /**
     * Callback method for customizing the options menu after creation
     */
    open fun configureOptionsMenu(menu: Menu, context: Context? = null) {}

    /**
     * Callback method when the navigation controller will start navigating back
     */
    open fun wilNavigateBack(closure: (() -> Unit)? = null) {
        closure?.let { it() }
    }

    /**
     * Callback method for when the modal view will get dismissed
     */
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

    /**
     * Convenience method for subclassing a modal instance of the [KBYFragment]
     *
     * @param bundle a bundle for passing extra data to the instance at creation
     * when subclassing
     */
    protected fun getModalInstance(bundle: Bundle? = null): KBYFragment {
        return this.apply {
            arguments = bundle?.apply { putBoolean(IS_MODAL_KEY, true) } ?: bundleOf(IS_MODAL_KEY to true)
        }
    }

    /**
     * Convenience method for subclassing a non-modal instance of the [KBYFragment]
     *
     * @param bundle a bundle for passing extra data to the instance at creation
     * when subclassing
     */
    protected fun getInstance(bundle: Bundle? = null): KBYFragment {
        return this.apply { arguments = bundle }
    }

    companion object {
        /**
         * Key for getting the modal presentation value after creation
         */
        private const val IS_MODAL_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.isModalKey"

        /**
         * Constant value representing a [KBYFragment] that doesn't have an options menu to show
         */
        const val NO_OPTIONS_MENU = -1
    }
}