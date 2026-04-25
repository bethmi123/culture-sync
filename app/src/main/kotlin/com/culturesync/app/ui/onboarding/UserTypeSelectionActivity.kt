package com.culturesync.app.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.culturesync.app.databinding.ActivityUserTypeSelectionBinding
import com.culturesync.app.ui.auth.RegisterActivity
import com.culturesync.app.utils.SessionManager

/**
 * User type selection screen (PRD Screen #3).
 * 2×2 card grid: Student, Tourist, Artisan, Shop Owner.
 * Saves to SessionManager and passed to RegisterActivity.
 */
class UserTypeSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserTypeSelectionBinding
    private var selectedType: String = "Student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserTypeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cards = mapOf(
            binding.cardStudent   to "Student",
            binding.cardTourist   to "Tourist",
            binding.cardArtisan   to "Artisan",
            binding.cardShopOwner to "ShopOwner"
        )

        fun selectCard(type: String) {
            selectedType = type
            cards.forEach { (card, t) ->
                card.strokeWidth = if (t == type) 6 else 0
            }
        }

        // Default selection
        selectCard("Student")

        cards.forEach { (card, type) ->
            card.setOnClickListener { selectCard(type) }
        }

        binding.btnContinue.setOnClickListener {
            val session = SessionManager(this)
            // Store type temporarily — RegisterActivity will save to Room
            startActivity(
                Intent(this, RegisterActivity::class.java).apply {
                    putExtra("user_type", selectedType)
                }
            )
        }
    }
}
