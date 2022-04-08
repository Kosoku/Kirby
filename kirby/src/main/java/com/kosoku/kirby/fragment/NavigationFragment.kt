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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.kosoku.kirby.BuildConfig
import com.kosoku.kirby.R
import com.kosoku.kirby.databinding.FragmentNavigationBinding
import com.kosoku.kirby.extension.setDebounceMenuOnClickListener
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.lang.Exception
import java.lang.ref.WeakReference

//@AndroidEntryPoint
open class NavigationFragment : DialogFragment() {
    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }
    private var isModal: Boolean = false

    private val currentFragment = BehaviorSubject.create<KBYFragment>()
    private val currentPosition = BehaviorSubject.createDefault(0)
    private var binding: FragmentNavigationBinding? = null

    var rootFragment: KBYFragment? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isModal = arguments?.getBoolean(IS_MODAL_NAVIGATION_FRAGMENT_KEY) ?: false
        if (isModal) {
            setStyle(STYLE_NORMAL, R.style.Theme_Kirby_Modal)
            isCancelable = false
        }

        (arguments?.get(ROOT_FRAGMENT_CLASS_NAME_KEY) as? String)?.let { fragmentName ->
            try {
                rootFragment = parentFragmentManager.fragmentFactory.instantiate(ClassLoader.getSystemClassLoader(), fragmentName) as? KBYFragment
                rootFragment?.let {
                    it.arguments = arguments
                }
            } catch (e: Exception) {
                dismiss()
            }
        }

        childFragmentManager.addOnBackStackChangedListener {
            childFragmentManager.fragments.lastOrNull()?.let {
                postCurrentFragment(it)
            }
            val stackCount = childFragmentManager.backStackEntryCount
            currentPosition.onNext(stackCount)
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
            pushFragment(root)
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

    fun observeCurrentFragmentWithPosition(): Observable<Pair<Int, KBYFragment>> {
        return Observables.zip(currentPosition, currentFragment)
    }

    fun pushFragment(fragment: KBYFragment) {
        fragment.navigationController = WeakReference(this@NavigationFragment)
        val identifier = fragment.backstackIdentifier
        binding?.contentContainer?.id?.let { container ->
            childFragmentManager.beginTransaction()
                .apply {
                    setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
                    replace(container, fragment)
                    addToBackStack(identifier)
                    commit()
                }
        }
    }

    fun popToIdentifier(identifier: String) {
        childFragmentManager.popBackStack(identifier, 0)
    }

    fun popToRootFragment() {
        childFragmentManager.popBackStack(rootFragment?.backstackIdentifier, 0)
    }

    private fun postCurrentFragment(fragment: Fragment) {
        if (fragment == currentFragment.value) { return }
        if (fragment is KBYFragment) {
            currentFragment.onNext(fragment)
        }
    }

    private fun backOrDismiss() {
        var isDismissType: Boolean = childFragmentManager.backStackEntryCount <= 1 && isModal

        currentFragment.value?.let { fragment ->
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
        (childFragmentManager.fragments.lastOrNull() as? KBYFragment)?.let { currentFragment ->
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
            childFragmentManager.popBackStack()
        }
    }

    protected fun getModalInstance(rootFragment: KBYFragment): NavigationFragment {
        return this.apply {
            arguments = rootFragment.arguments?.apply {
                putBoolean(IS_MODAL_NAVIGATION_FRAGMENT_KEY, true)
                putString(ROOT_FRAGMENT_CLASS_NAME_KEY, rootFragment.javaClass.name)
            } ?: bundleOf(
                IS_MODAL_NAVIGATION_FRAGMENT_KEY to true,
                ROOT_FRAGMENT_CLASS_NAME_KEY to rootFragment.javaClass.name
            )
        }
    }

    protected fun getInstance(rootFragment: KBYFragment): NavigationFragment {
        return this.apply {
            arguments = rootFragment.arguments?.apply {
                putString(ROOT_FRAGMENT_CLASS_NAME_KEY, rootFragment.javaClass.name)
            } ?: bundleOf(ROOT_FRAGMENT_CLASS_NAME_KEY to rootFragment.javaClass.name)
        }
    }

    companion object {
        private const val ROOT_FRAGMENT_CLASS_NAME_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.rootFragmentClassNameKey"
        private const val IS_MODAL_NAVIGATION_FRAGMENT_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.isModalNavigationFragmentKey"

        fun getModalInstance(rootFragment: KBYFragment): NavigationFragment {
            val retval = NavigationFragment().apply {
                arguments = rootFragment.arguments?.apply {
                    putBoolean(IS_MODAL_NAVIGATION_FRAGMENT_KEY, true)
                    putString(ROOT_FRAGMENT_CLASS_NAME_KEY, rootFragment.javaClass.name)
                } ?: bundleOf(
                    IS_MODAL_NAVIGATION_FRAGMENT_KEY to true,
                    ROOT_FRAGMENT_CLASS_NAME_KEY to rootFragment.javaClass.name
                )
            }
            return retval
        }

        fun getInstance(rootFragment: KBYFragment): NavigationFragment {
            val retval = NavigationFragment().apply {
                arguments = rootFragment.arguments?.apply {
                    putString(ROOT_FRAGMENT_CLASS_NAME_KEY, rootFragment.javaClass.name)
                } ?: bundleOf(ROOT_FRAGMENT_CLASS_NAME_KEY to rootFragment.javaClass.name)
            }
            return retval
        }
    }
}