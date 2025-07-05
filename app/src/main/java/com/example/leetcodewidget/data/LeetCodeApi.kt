package com.example.leetcodewidget.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
// Ensure OkHttp engine is imported if not already
import io.ktor.client.engine.okhttp.OkHttp // Explicit import for OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- Data class for GraphQL Request Body ---
@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, String>,
    val operationName: String
)

// --- Data classes for LeetCode API Response ---
@Serializable
data class LeetCodeResponse(
    @SerialName("data")
    val data: LeetCodeData? = null
)

@Serializable
data class LeetCodeData(
    @SerialName("allQuestionsCount")
    val allQuestionsCount: List<AllQuestionsCount>? = null,
    @SerialName("matchedUser")
    val matchedUser: MatchedUser? = null
)

@Serializable
data class AllQuestionsCount(
    @SerialName("difficulty")
    val difficulty: String? = null,
    @SerialName("count")
    val count: Int? = null
)

@Serializable
data class MatchedUser(
    @SerialName("submitStats")
    val submitStats: SubmitStats? = null
)

@Serializable
data class SubmitStats(
    @SerialName("acSubmissionNum")
    val acSubmissionNum: List<SubmissionCount>? = null,
    @SerialName("totalSubmissionNum")
    val totalSubmissionNum: List<SubmissionCount>? = null
)

@Serializable
data class SubmissionCount(
    @SerialName("difficulty")
    val difficulty: String? = null,
    @SerialName("count")
    val count: Int? = null,
    @SerialName("submissions")
    val submissions: Int? = null
)

// --- Ktor HTTP Client ---
object LeetCodeApiClient {
    private val client = HttpClient(OkHttp) { // Using the explicit OkHttp engine
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
    }

    private const val LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql"

    suspend fun getLeetCodeStats(username: String): LeetCodeResponse? {
        val query = """
            query userSessionProgress(${'$'}username: String!) {
              allQuestionsCount {
                difficulty
                count
              }
              matchedUser(username: ${'$'}username) {
                submitStats {
                  acSubmissionNum {
                    difficulty
                    count
                    submissions
                  }
                  totalSubmissionNum {
                    difficulty
                    count
                    submissions
                  }
                }
              }
            }
        """.trimIndent()

        val graphQLRequestBody = GraphQLRequest(
            query = query,
            variables = mapOf("username" to username),
            operationName = "userSessionProgress"
        )

        return try {
            val response = client.post(LEETCODE_GRAPHQL_URL) {
                contentType(ContentType.Application.Json)
                setBody(graphQLRequestBody) // Use the serializable data class
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<LeetCodeResponse>()
            } else {
                println("Error fetching LeetCode stats: HTTP Status ${'$'}{response.status}")
                val errorBody = response.body<String>()
                println("Error body: ${'$'}errorBody")
                null
            }
        } catch (e: Exception) {
            println("Detailed error in getLeetCodeStats: ${'$'}e") // This will call e.toString()
            println("Stack trace for getLeetCodeStats error:")
            e.printStackTrace(System.out) // Explicitly print stack trace to System.out
            null
        }
    }
}
