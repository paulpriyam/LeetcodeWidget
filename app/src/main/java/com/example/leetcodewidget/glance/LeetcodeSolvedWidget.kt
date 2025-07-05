package com.example.leetcodewidget.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.leetcodewidget.data.LeetCodeApiClient
import com.example.leetcodewidget.data.LeetCodeResponse

// Define a state holder for our widget data
private sealed class SolvedCountState {
    object Loading : SolvedCountState()
    data class Success(val solvedCount: Int, val totalCount: Int) : SolvedCountState()
    data class Error(val message: String) : SolvedCountState()
}

class LeetcodeSolvedWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Fetch and manage state
            // Using "ppaul2204" as the username
            val solvedState by produceState<SolvedCountState>(initialValue = SolvedCountState.Loading, "ppaul2204") {
                val response: LeetCodeResponse? = LeetCodeApiClient.getLeetCodeStats("ppaul2204")
                value = if (response?.data != null) {
                    val userSolvedAll = response.data.matchedUser?.submitStats?.acSubmissionNum
                        ?.find { it.difficulty == "All" }?.count ?: 0
                    val totalLeetcodeAll = response.data.allQuestionsCount
                        ?.find { it.difficulty == "All" }?.count ?: 0

                    if (totalLeetcodeAll > 0) { // Avoid division by zero or nonsensical data
                        SolvedCountState.Success(userSolvedAll, totalLeetcodeAll)
                    } else {
                        SolvedCountState.Error("Invalid total count")
                    }
                } else {
                    SolvedCountState.Error("Failed to fetch data")
                }
            }
            SolvedWidgetContent(state = solvedState)
        }
    }

    @Composable
    private fun SolvedWidgetContent(state: SolvedCountState) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            when (state) {
                is SolvedCountState.Loading -> {
                    Text("Loading...")
                }
                is SolvedCountState.Success -> {
                    Text("Solved: ${state.solvedCount} / ${state.totalCount}")
                }
                is SolvedCountState.Error -> {
                    Text("Error: ${state.message}", style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color.Red)))
                }
            }
        }
    }
}
