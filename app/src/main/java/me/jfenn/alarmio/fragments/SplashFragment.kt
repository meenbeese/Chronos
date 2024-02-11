package me.jfenn.alarmio.fragments

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import me.jfenn.alarmio.R
import me.jfenn.alarmio.views.AppIconView


class SplashFragment : BaseFragment(), Animator.AnimatorListener {
    private var isFinished = false
    private var isVisible = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)
        val iconView = view.findViewById<AppIconView>(R.id.icon)
        iconView.addListener(this)
        return view
    }

    override fun onResume() {
        isVisible = true
        if (isFinished) finish()
        super.onResume()
    }

    override fun onPause() {
        isVisible = false
        super.onPause()
    }

    override fun onDestroyView() {
        isVisible = false
        super.onDestroyView()
    }

    override fun onAnimationStart(animator: Animator) {}
    override fun onAnimationEnd(animator: Animator) {
        isFinished = true
        if (isVisible) finish()
    }

    override fun onAnimationCancel(animator: Animator) {}
    override fun onAnimationRepeat(animator: Animator) {}
    private fun finish() {
        val fragment = HomeFragment()
        fragment.arguments = arguments
        fragmentManager!!.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(R.id.fragment, fragment)
            .commit()
    }
}
