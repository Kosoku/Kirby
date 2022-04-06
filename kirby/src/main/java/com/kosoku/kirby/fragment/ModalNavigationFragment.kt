package com.kosoku.kirby.fragment

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import com.kosoku.kirby.R
import com.kosoku.kirby.databinding.FragmentModalNavigationBinding
import com.kosoku.kirby.extension.setDebounceMenuOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.lang.ref.WeakReference

//@AndroidEntryPoint
open class ModalNavigationFragment constructor(val rootFragment: KBYFragment) : DialogFragment() {
    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    private lateinit var navController: NavController
    private var currentFragment: KBYFragment? = null
    private var binding: FragmentModalNavigationBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_Kirby_Modal)
        isCancelable = false

        childFragmentManager.addOnBackStackChangedListener {
            updateNavBar()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_modal_navigation, container, false)

        pushFragment(rootFragment)

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

    private fun backOrDismiss() {
        var isDismissType: Boolean = childFragmentManager.backStackEntryCount <= 1

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
                dismiss = { handleDismiss() }
                this@ModalNavigationFragment.binding?.toolbar?.menu?.let { menu ->
                    configureOptionsMenu(menu, this@ModalNavigationFragment.binding?.root?.context)
                }
                navigationController = WeakReference(this@ModalNavigationFragment)
            }

            binding?.toolbar?.title = currentFragment.title
            binding?.toolbar?.navigationIcon = when (currentFragment.hideNavigationIcon) {
                true -> null
                else -> {
                    when (currentFragment.replaceBackButtonWithCloseButton) {
                        true -> ResourcesCompat.getDrawable(resources, R.drawable.ic_close, null)
                        false -> ResourcesCompat.getDrawable(resources, R.drawable.ic_back, null)
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

    }
}