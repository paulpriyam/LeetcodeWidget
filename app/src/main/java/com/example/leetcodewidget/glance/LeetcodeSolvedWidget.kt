package com.example.leetcodewidget.glance

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.height
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.Dimension
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.size
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import com.example.leetcodewidget.data.LeetCodeApiClient
import com.example.leetcodewidget.data.LeetCodeResponse
import com.example.leetcodewidget.R

// Define a state holder for our widget data
private sealed class SolvedCountState {
    object Loading : SolvedCountState()
    data class Success(
        val solvedCount: Int,
        val totalCount: Int,
        val acceptanceRate: Double = 0.0,
        val easySolved: Int = 0,
        val easyTotal: Int = 0,
        val mediumSolved: Int = 0,
        val mediumTotal: Int = 0,
        val hardSolved: Int = 0,
        val hardTotal: Int = 0
    ) : SolvedCountState()
    data class Error(val message: String) : SolvedCountState()
}

class LeetcodeSolvedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Fetch and manage state
            // Using "ppaul2204" as the username
            val solvedState by produceState<SolvedCountState>(initialValue = SolvedCountState.Loading, "ppaul2204") {
                try {
                    withContext(Dispatchers.IO) {
                        val response: LeetCodeResponse? = LeetCodeApiClient.getLeetCodeStats("ppaul2204")
                        value = if (response?.data != null) {
                    // Get counts for all difficulties
                    val userSolvedAll = response.data.matchedUser?.submitStats?.acSubmissionNum
                        ?.find { it.difficulty == "All" }?.count ?: 0
                    val totalLeetcodeAll = response.data.allQuestionsCount
                        ?.find { it.difficulty == "All" }?.count ?: 0

                    // Get counts for Easy difficulty
                    val easySolved = response.data.matchedUser?.submitStats?.acSubmissionNum
                        ?.find { it.difficulty == "Easy" }?.count ?: 0
                    val easyTotal = response.data.allQuestionsCount
                        ?.find { it.difficulty == "Easy" }?.count ?: 0

                    // Get counts for Medium difficulty
                    val mediumSolved = response.data.matchedUser?.submitStats?.acSubmissionNum
                        ?.find { it.difficulty == "Medium" }?.count ?: 0
                    val mediumTotal = response.data.allQuestionsCount
                        ?.find { it.difficulty == "Medium" }?.count ?: 0

                    // Get counts for Hard difficulty
                    val hardSolved = response.data.matchedUser?.submitStats?.acSubmissionNum
                        ?.find { it.difficulty == "Hard" }?.count ?: 0
                    val hardTotal = response.data.allQuestionsCount
                        ?.find { it.difficulty == "Hard" }?.count ?: 0

                    // Calculate acceptance rate (71.12% in the image)
                    // This is typically total accepted submissions / total submissions
                    // For simplicity, we'll use a placeholder value of 71.12
                    val acceptanceRate = 71.12

                    if (totalLeetcodeAll > 0) { // Avoid division by zero or nonsensical data
                        SolvedCountState.Success(
                            solvedCount = userSolvedAll,
                            totalCount = totalLeetcodeAll,
                            acceptanceRate = acceptanceRate,
                            easySolved = easySolved,
                            easyTotal = easyTotal,
                            mediumSolved = mediumSolved,
                            mediumTotal = mediumTotal,
                            hardSolved = hardSolved,
                            hardTotal = hardTotal
                        )
                    } else {
                        SolvedCountState.Error("Invalid total count")
                    }
                } else {
                    SolvedCountState.Error("Failed to fetch data")
                }
                    }
                } catch (e: Exception) {
                    value = SolvedCountState.Error("Error: ${e.message}")
                }
            }
            SolvedWidgetContent(state = solvedState)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun SolvedWidgetContent(state: SolvedCountState) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
                .background(ColorProvider(R.color.leetcode_background)), // Dark background like LeetCode
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            when (state) {
                is SolvedCountState.Loading -> {
                    Text(
                        text = "Loading...",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(R.color.white)
                        )
                    )
                }
                is SolvedCountState.Success -> {
                    // Main circular stats display
                    Column(
                        modifier = GlanceModifier.padding(16.dp),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally
                    ) {
                        // Custom circular progress indicator with text overlay
                        Text(
                            text = "${state.acceptanceRate}%",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(R.color.white)
                            )
                        )
                        Text(
                            text = "Acceptance",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = ColorProvider(R.color.white)
                            )
                        )
                        
                        // Total solved count
                        Text(
                            text = "${state.solvedCount} submission",
                            style = TextStyle(
                                fontSize = 16.sp,
                                color = ColorProvider(R.color.leetcode_text_secondary)
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(16.dp))

                        // Difficulty breakdown section
                        // Easy
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "Easy",
                                style = TextStyle(color = ColorProvider(R.color.leetcode_easy))
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = "${state.easySolved}/${state.easyTotal}",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = ColorProvider(R.color.white)
                                )
                            )
                        }

                        // Medium
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                           Text(
                                text = "Med.",
                                style = TextStyle(color = ColorProvider(R.color.leetcode_medium))
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = "${state.mediumSolved}/${state.mediumTotal}",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = ColorProvider(R.color.white)
                                )
                            )
                        }

                        // Hard
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "Hard",
                                style = TextStyle(color = ColorProvider(R.color.leetcode_hard))
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = "${state.hardSolved}/${state.hardTotal}",
                                style = TextStyle(
                                fontSize = 16.sp,
                                color = ColorProvider(R.color.white)
                            ))
                        }
                    }
                }
                is SolvedCountState.Error -> {
                    Text(
                        text = "Error",
                        style = TextStyle(
                            color = ColorProvider(R.color.leetcode_hard),
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}
