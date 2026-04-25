package com.culturesync.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.culturesync.app.R
import com.culturesync.app.databinding.ActivityOnboardingBinding
import com.culturesync.app.ui.auth.LoginActivity
import com.culturesync.app.utils.SessionManager
import com.google.android.material.tabs.TabLayoutMediator

data class OnboardingPage(val title: String, val desc: String, val iconRes: Int)

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding

    private fun getPages() = listOf(
        OnboardingPage(
            getString(R.string.onboard_title_1),
            getString(R.string.onboard_desc_1),
            R.drawable.ic_onboarding_1
        ),
        OnboardingPage(
            getString(R.string.onboard_title_2),
            getString(R.string.onboard_desc_2),
            R.drawable.ic_onboarding_2
        ),
        OnboardingPage(
            getString(R.string.onboard_title_3),
            getString(R.string.onboard_desc_3),
            R.drawable.ic_onboarding_3
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pagesList = getPages()
        val adapter = OnboardingAdapter(pagesList)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.dotsIndicator, binding.viewPager) { _, _ -> }.attach()

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            if (current < pagesList.size - 1) {
                binding.viewPager.currentItem = current + 1
            } else {
                finish()
                startActivity(Intent(this, UserTypeSelectionActivity::class.java))
                SessionManager(this).setFirstLaunchDone()
            }
        }

        binding.tvSkip.setOnClickListener {
            SessionManager(this).setFirstLaunchDone()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.btnNext.text = if (position == pagesList.size - 1) getString(R.string.btn_get_started) else getString(R.string.btn_next)
            }
        })
    }
}
