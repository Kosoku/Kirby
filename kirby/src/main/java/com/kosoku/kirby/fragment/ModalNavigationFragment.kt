package com.kosoku.kirby.fragment

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.kosoku.kirby.BuildConfig
import com.kosoku.kirby.R
import com.kosoku.kirby.databinding.FragmentModalNavigationBinding
import com.kosoku.kirby.extension.setDebounceMenuOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import timber.log.Timber
import java.lang.Exception
import java.lang.ref.WeakReference

//@AndroidEntryPoint
open class ModalNavigationFragment : DialogFragment() {
    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    private val currentFragment = BehaviorSubject.create<KBYFragment>()
    private val currentPosition = BehaviorSubject.createDefault(0)
    private var binding: FragmentModalNavigationBinding? = null

    var rootFragment: KBYFragment? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Kirby_Modal)
        isCancelable = false

        (arguments?.get(ROOT_FRAGMENT_CLASS_NAME_KEY) as? String)?.let { fragmentName ->
            try {
                rootFragment = parentFragmentManager.fragmentFactory.instantiate(ClassLoader.getSystemClassLoader(), fragmentName) as? KBYFragment
                rootFragment?.let {
                    it.arguments = arguments
                    pushFragment(it)
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_modal_navigation, container, false)

        rootFragment?.let { root ->
            pushFragment(root)
        }

        binding?.toolbar?.setNavigationOnClickListener { backOrDismiss() }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                backOrDismiss()
            }
            false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            postCurrentFragment(fragment)
        }
    }

    fun observeCurrentFragmentWithPosition(): Observable<Pair<Int, KBYFragment>> {
        return Observables.zip(currentPosition, currentFragment)
    }

    fun pushFragment(fragment: KBYFragment) {
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

    private fun postCurrentFragment(fragment: Fragment) {
        if (fragment == currentFragment.value) { return }
        if (fragment is KBYFragment) {
            currentFragment.onNext(fragment)
        }
    }

    private fun backOrDismiss() {
        var isDismissType: Boolean = childFragmentManager.backStackEntryCount <= 1

        currentFragment.value?.let { fragment ->
            if (fragment.hideNavigationIcon) { return }
            if (fragment.replaceBackButtonWithCloseButton) {
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
                this@ModalNavigationFragment.binding?.toolbar?.menu?.let { menu ->
                    configureOptionsMenu(menu, this@ModalNavigationFragment.binding?.root?.context)
                }
                navigationController = WeakReference(this@ModalNavigationFragment)
            }

            binding?.toolbar?.title = currentFragment.title
            binding?.toolbar?.navigationIcon = when (currentFragment.hideNavigationIcon) {
                true -> null
                else -> {
                    if (childFragmentManager.backStackEntryCount > 1) {
                        when (currentFragment.replaceBackButtonWithCloseButton) {
                            true -> ResourcesCompat.getDrawable(resources, R.drawable.ic_close, null)
                            false -> ResourcesCompat.getDrawable(resources, R.drawable.ic_back, null)
                        }
                    } else {
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_close, null)
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
        dismiss()
    }

    private fun handleBack() {
        childFragmentManager.popBackStack()
    }

    companion object {
        private const val ROOT_FRAGMENT_CLASS_NAME_KEY = "${BuildConfig.LIBRARY_PACKAGE_NAME}.rootFragmentClassNameKey"

        fun getInstance(rootFragment: KBYFragment): ModalNavigationFragment {
            val retval = ModalNavigationFragment().apply {
                arguments = rootFragment.arguments?.apply { putString(ROOT_FRAGMENT_CLASS_NAME_KEY, rootFragment.javaClass.name) } ?: bundleOf(ROOT_FRAGMENT_CLASS_NAME_KEY to rootFragment.javaClass.name)
            }
            return retval
        }
    }
}