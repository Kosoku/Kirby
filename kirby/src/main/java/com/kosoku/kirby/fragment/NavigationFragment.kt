package com.kosoku.kirby.fragment

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.kosoku.kirby.BuildConfig
import com.kosoku.kirby.R
import com.kosoku.kirby.databinding.FragmentNavigationBinding
import com.kosoku.kirby.extension.setDebounceMenuOnClickListener
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * A fragment that mimics UINavigationController for iOS, with modal presentation capability
 */
open class NavigationFragment : DialogFragment() {
    /**
     * Data class model representing a [KBYFragment] and its position in the backstack
     */
    data class FragmentWithPosition(
        val fragment: KBYFragment<*>,
        val position: Int
    )

    private val _currentFragmentRx = BehaviorSubject.create<KBYFragment<*>>()
    private val _currentPositionRx = BehaviorSubject.createDefault(-1)
    private val _currentFragment: MutableLiveData<KBYFragment<*>> by lazy { MutableLiveData() }
    private val _currentPosition: MutableLiveData<Int> by lazy { MutableLiveData(-1) }
    private var binding: FragmentNavigationBinding? = null

    /**
     * Read-only property to determine if the view is being presented as a modal
     */
    var isModal: Boolean = false
        protected set

    /**
     * Read-only property for the initial fragment in the navigation controller's backstack
     */
    var rootFragment: KBYFragment<*>? = null
        protected set

    /**
     * Read/write property list of fragments in the navigation controller's backstack with 0 index
     * being first in the backstack
     */
    var fragments: List<KBYFragment<*>>
        get() = childFragmentManager.fragments.toList().filterIsInstance(KBYFragment::class.java)
        set(value) {
            replaceFragments(value)
        }

    /**
     * Read-only property to get the [AppBarLayout] container for the navigation bar
     */
    val appBarLayout: AppBarLayout?
        get() = binding?.appBarLayout

    /**
     * Read-only property to get the [MaterialToolbar] contained in the [appBarLayout]
     */
    val appBarToolbar: MaterialToolbar?
        get() = binding?.toolbar

    /**
     * Transparent view overlay for intercepting touch events
     */
    val touchInterceptor: View?
        get() = binding?.touchInterceptor

    /**
     * Read-only property to get the visibility state of the navigation bar
     */
    val isNavigationBarHidden: Boolean
        get() = appBarLayout?.isVisible == false

    /**
     * A [LiveData<FragmentWithPosition>] property for observing changes in the top-level fragment
     * and its position on the backstack
     */
    val currentFragmentWithPosition: LiveData<FragmentWithPosition>
        get() {

            val retval = MediatorLiveData<FragmentWithPosition>()

            retval.addSource(_currentFragment) {
                retval.postValue(combineFragmentAndPosition(_currentFragment, _currentPosition))
            }
            retval.addSource(_currentPosition) {
                retval.postValue(combineFragmentAndPosition(_currentFragment, _currentPosition))
            }

            return retval
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isModal = arguments?.getBoolean(IS_MODAL_NAVIGATION_FRAGMENT_KEY) ?: false
        if (isModal) {
            setStyle(STYLE_NORMAL, R.style.Theme_Kirby_Modal)
            isCancelable = false
        }

        arguments?.let { args ->
            args.getString(ROOT_FRAGMENT_CLASS_NAME_KEY)?.let { fragmentName ->
                try {
                    rootFragment = parentFragmentManager.fragmentFactory.instantiate(ClassLoader.getSystemClassLoader(), fragmentName) as? KBYFragment<*>
                    rootFragment?.let {
                        it.arguments = arguments
                    }
                } catch (e: Exception) {
                    dismiss()
                }
            }
        }

        childFragmentManager.addOnBackStackChangedListener {
            childFragmentManager.fragments.lastOrNull()?.let {
                postCurrentFragment(it)
            }
            val stackCount = childFragmentManager.backStackEntryCount - 1
            _currentPositionRx.onNext(stackCount)
            _currentPosition.postValue(stackCount)
            updateNavBar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_navigation, container, false)

        rootFragment?.let { root ->
            if (childFragmentManager.fragments.size == 0) {
                pushFragment(root)
            }
        }

        binding?.toolbar?.setNavigationOnClickListener { backOrDismiss() }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isModal) {
            dialog?.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    backOrDismiss()
                }
                false
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            postCurrentFragment(fragment)
        }

        if (!isModal) {
            requireActivity().onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        backOrDismiss()
                    }
                })
        }
    }

    override fun dismiss() {
        if (isModal) {
            super.dismiss()
        }
    }

    /**
     * Sets whether the navigation bar is hidden
     */
    fun setNavigationBarHidden(hidden: Boolean) {
        appBarLayout?.isVisible = !hidden
    }

    /**
     * A method that returns an RxObservable for the top-most fragment in the navigation
     * controller's backstack along with its position index
     */
    @Deprecated("This method will be moved to KirbyRxExtensions library in the future. " +
            "Consider using [currentFragmentWithPosition] [LiveData] object instead.")
    fun observeCurrentFragmentWithPosition(): Observable<Pair<Int, KBYFragment<*>>> {
        return Observables.zip(_currentPositionRx, _currentFragmentRx)
    }

    /**
     * Pushes a new fragment onto the navigation controller's backstack.
     *
     * @param fragment a KYBFragment to add to the backstack
     * @param animated property to animate the transition of the new fragment to replace the
     * existing fragment. Default value is true
     */
    fun pushFragment(fragment: KBYFragment<*>, animated: Boolean = true) {
        setNavigationBarHidden(false)
        fragment.navigationController = WeakReference(this@NavigationFragment)
        val identifier = fragment.backstackIdentifier
        binding?.contentContainer?.id?.let { container ->
            childFragmentManager.beginTransaction()
                .apply {
                    if (animated) {
                        setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                        )
                    }
                    replace(container, fragment)
                    addToBackStack(identifier)
                    commit()
                }
        }
    }

    /**
     * Pops the top most fragment from the navigation controller's backstack.
     */
    fun pop() {
        setNavigationBarHidden(false)
        childFragmentManager.popBackStack()
    }

    /**
     * Pops the top most fragment from the backstack until it finds the fragment with the
     * designated identifier
     *
     * @param identifier The unique ID for the fragment that was previously pushed onto the backstack
     */
    fun popToIdentifier(identifier: String) {
        setNavigationBarHidden(false)
        childFragmentManager.popBackStack(identifier, 0)
    }

    /**
     * Pops to the bottom most fragment in the backstack
     */
    fun popToRootFragment() {
        setNavigationBarHidden(false)
        childFragmentManager.popBackStack(rootFragment?.backstackIdentifier, 0)
    }

    private fun combineFragmentAndPosition(fragment: LiveData<KBYFragment<*>>, position: LiveData<Int>): FragmentWithPosition? {
        val fragmentValue = fragment.value
        val positionValue = position.value

        return if (fragmentValue == null || positionValue == null) {
            null
        } else {
            FragmentWithPosition(fragmentValue, positionValue)
        }
    }

    private fun replaceFragments(fragments: List<KBYFragment<*>>) {
        setNavigationBarHidden(false)
        childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        for (fragment in fragments) {
            pushFragment(fragment, true)
        }
        rootFragment = fragments.firstOrNull()
    }

    private fun postCurrentFragment(fragment: Fragment) {
        if (fragment == _currentFragmentRx.value) { return }
        if (fragment is KBYFragment<*>) {
            _currentFragmentRx.onNext(fragment)
            _currentFragment.postValue(fragment)
        }
    }

    private fun backOrDismiss() {
        var isDismissType: Boolean = childFragmentManager.backStackEntryCount <= 1 && isModal

        _currentFragment.value?.let { fragment ->
            if (fragment.hideNavigationIcon) { return }
            if (fragment.replaceBackButtonWithCloseButton && isModal) {
                isDismissType = true
            }
            if (isDismissType) {
                fragment.wilDismiss { handleDismiss() }
            } else {
                fragment.wilNavigateBack { handleBack() }
            }

            return
        }

        if (isDismissType) {
            handleDismiss()
        } else {
            handleBack()
        }
    }

    private fun updateNavBar() {
        (childFragmentManager.fragments.lastOrNull() as? KBYFragment<*>)?.let { currentFragment ->
            inflateMenu(currentFragment.menuResourceId)
            binding?.toolbar?.setDebounceMenuOnClickListener(onClickDebounce = currentFragment.menuOnClickListener)

            currentFragment.apply {
                this@NavigationFragment.binding?.toolbar?.menu?.let { menu ->
                    configureOptionsMenu(menu, this@NavigationFragment.binding?.root?.context)
                }
            }

            binding?.toolbar?.title = currentFragment.title
            binding?.toolbar?.navigationIcon = when (currentFragment.hideNavigationIcon) {
                true -> null
                else -> {
                    if (childFragmentManager.backStackEntryCount > 1) {
                        when (currentFragment.replaceBackButtonWithCloseButton && isModal) {
                            true -> ResourcesCompat.getDrawable(resources, R.drawable.ic_close, null)
                            false -> ResourcesCompat.getDrawable(resources, R.drawable.ic_back, null)
                        }
                    } else {
                        if (isModal) {
                            ResourcesCompat.getDrawable(resources, R.drawable.ic_close, null)
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }

    private fun inflateMenu(menuId: Int) {
        binding?.toolbar?.menu?.clear()
        if (menuId != KBYFragment.NO_OPTIONS_MENU) {
            binding?.toolbar?.inflateMenu(menuId)
        }
    }

    private fun handleDismiss() {
        if (isModal) {
            dismiss()
        }
    }

    private fun handleBack() {
        if (childFragmentManager.backStackEntryCount > 1) {
            pop()
        }
    }

    /**
     * Convenience method for subclassing a modal instance of the [NavigationFragment]
     *
     * @param rootFragment the fragment to set as the root
     * @param bundle a bundle for passing extra data to the navigation controller when subclassing
     */
    protected fun getModalInstance(rootFragment: KBYFragment<*>?, bundle: Bundle? = null): NavigationFragment {
        return this.apply {
            arguments = combinedBundle(true, rootFragment, bundle)
        }
    }

    /**
     * Convenience method for subclassing a non-modal instance of the [NavigationFragment]
     *
     * @param rootFragment the fragment to set as the root
     * @param bundle a bundle for passing extra data to the navigation controller when subclassing
     */
    protected fun getInstance(rootFragment: KBYFragment<*>?, bundle: Bundle? = null): NavigationFragment {
        return this.apply {
            arguments = combinedBundle(false, rootFragment, bundle)
        }
    }

    companion object {
        /**
         * Key for getting the root fragment's javaClass.name value from the bundle after creation
         */
        const val ROOT_FRAGMENT_CLASS_NAME_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.rootFragmentClassNameKey"

        /**
         * Key for getting the modal presentation value after creation
         */
        const val IS_MODAL_NAVIGATION_FRAGMENT_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.isModalNavigationFragmentKey"

        /**
         * Convenience method for creating a modal instance of the [NavigationFragment]
         *
         * @param rootFragment the fragment to set as the root
         * @param bundle a bundle for passing extra data to the navigation controller when subclassing
         */
        fun getModalInstance(rootFragment: KBYFragment<*>?, bundle: Bundle? = null): NavigationFragment {
            return NavigationFragment().getModalInstance(rootFragment, bundle)
        }

        /**
         * Convenience method for creating a non-modal instance of the [NavigationFragment]
         *
         * @param rootFragment the fragment to set as the root
         * @param bundle a bundle for passing extra data to the navigation controller when subclassing
         */
        fun getInstance(rootFragment: KBYFragment<*>?, bundle: Bundle? = null): NavigationFragment {
            return NavigationFragment().getInstance(rootFragment, bundle)
        }

        private fun combinedBundle(isModal: Boolean, rootFragment: KBYFragment<*>?, bundle: Bundle?): Bundle {
            val rootFragmentBundle = rootFragment?.arguments ?: bundleOf()
            val extraBundle = bundle ?: bundleOf()
            return rootFragmentBundle.apply {
                putAll(extraBundle)
                putAll(
                    bundleOf(
                        IS_MODAL_NAVIGATION_FRAGMENT_KEY to isModal,
                        ROOT_FRAGMENT_CLASS_NAME_KEY to rootFragment?.javaClass?.name
                    )
                )
            }
        }
    }
}