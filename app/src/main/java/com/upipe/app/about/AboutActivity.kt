package com.upipe.app.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.upipe.app.BuildConfig
import com.upipe.app.R
import com.upipe.app.databinding.ActivityAboutBinding
import com.upipe.app.databinding.FragmentAboutBinding
import com.upipe.app.databinding.FragmentUpipeLicenseBinding
import com.upipe.app.util.ThemeHelper
import com.upipe.app.util.external_communication.ShareUtils

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHelper.setTheme(this)
        title = getString(R.string.title_activity_about)

        val binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.aboutToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.aboutViewPager.adapter = AboutPagerAdapter(this)
        binding.aboutViewPager.isUserInputEnabled = false
        TabLayoutMediator(binding.aboutTabs, binding.aboutViewPager) { tab, position ->
            tab.text = getString(if (position == 0) R.string.tab_about else R.string.tab_licenses)
        }.attach()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class AboutFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val binding = FragmentAboutBinding.inflate(inflater, container, false)
            val githubUrl = getString(R.string.github_url)

            binding.aboutAppName.text = getString(R.string.app_name)
            binding.aboutAppVersion.text = BuildConfig.VERSION_NAME
            binding.aboutViewOnGithub.setOnClickListener {
                ShareUtils.openUrlInBrowser(requireContext(), githubUrl)
            }

            return binding.root
        }
    }

    class UpipeLicenseFragment : Fragment() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val binding = FragmentUpipeLicenseBinding.inflate(inflater, container, false)
            binding.licenseCopyright.text = getString(R.string.upipe_copyright_notice)
            binding.licenseWebView.loadDataWithBaseURL(
                null,
                getFormattedLicense(requireContext(), StandardLicenses.GPL3),
                "text/html",
                "UTF-8",
                null
            )
            return binding.root
        }
    }

    private class AboutPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) AboutFragment() else UpipeLicenseFragment()
        }
    }
}
