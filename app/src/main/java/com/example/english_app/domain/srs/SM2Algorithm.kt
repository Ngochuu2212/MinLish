package com.example.english_app.domain.srs

import com.example.english_app.data.local.entity.LearningRecordEntity
import com.example.english_app.utils.DateUtils
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * SM-2 Spaced Repetition Algorithm implementation.
 * Quality ratings:
 * 0 = Again (complete blackout)
 * 1 = Again (wrong answer but remembered)
 * 2 = Hard (correct with serious difficulty)
 * 3 = Good (correct after hesitation)
 * 4 = Easy (correct with little effort)
 * 5 = Easy (perfect response)
 */
object SM2Algorithm {
    const val QUALITY_AGAIN = 1
    const val QUALITY_HARD = 2
    const val QUALITY_GOOD = 3
    const val QUALITY_EASY = 5

    data class ReviewResult(
        val easeFactor: Float,
        val interval: Int,
        val repetitions: Int,
        val nextReviewDate: Long
    )

    fun calculate(record: LearningRecordEntity, quality: Int): ReviewResult {
        val clampedQuality = quality.coerceIn(0, 5)

        // Calculate new ease factor
        var newEF = record.easeFactor + (0.1f - (5 - clampedQuality) * (0.08f + (5 - clampedQuality) * 0.02f))
        newEF = max(1.3f, newEF)

        val newRepetitions: Int
        val newInterval: Int

        if (clampedQuality < 3) {
            // Failed - reset
            newRepetitions = 0
            newInterval = 1
        } else {
            newRepetitions = record.repetitions + 1
            newInterval = when (record.repetitions) {
                0 -> 1
                1 -> 6
                else -> (record.interval * record.easeFactor).roundToInt().coerceAtLeast(1)
            }
        }

        // AGAIN (quality <= 1): keep card due immediately (same day review)
        // HARD (quality == 2): schedule for tomorrow
        // GOOD/EASY (quality >= 3): use SM2 interval
        val nextReviewDate = when {
            clampedQuality <= 1 -> System.currentTimeMillis() // due lại ngay
            else -> DateUtils.addDays(System.currentTimeMillis(), newInterval)
        }

        return ReviewResult(
            easeFactor = newEF,
            interval = newInterval,
            repetitions = newRepetitions,
            nextReviewDate = nextReviewDate
        )
    }
}

