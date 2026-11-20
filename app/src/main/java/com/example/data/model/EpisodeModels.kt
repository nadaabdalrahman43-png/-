package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

data class KeyPoint(
    val title: String,
    val detail: String
)

data class EpisodeSummary(
    val friendlyTitle: String,
    val hookIntro: String,
    val easyStorySummary: String,
    val keyPoints: List<KeyPoint>,
    val practicalTip: String,
    val encouragingQuote: String
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val hint: String,
    val explanation: String
)

data class TrueFalseQuestion(
    val statement: String,
    val isTrue: Boolean,
    val explanation: String
)

data class MatchPair(
    val concept: String,
    val meaning: String
)

data class EpisodeGamePack(
    val quizQuestions: List<QuizQuestion>,
    val trueFalseQuestions: List<TrueFalseQuestion>,
    val matchPairs: List<MatchPair>
)

data class GeneratedEpisodeContent(
    val summary: EpisodeSummary,
    val gamePack: EpisodeGamePack
)

data class EpisodeItem(
    val videoId: String,
    val playlistId: String,
    val episodeNumber: Int,
    val title: String,
    val channelName: String,
    val durationText: String,
    val thumbnailUrl: String,
    val description: String,
    val summary: EpisodeSummary? = null,
    val gamePack: EpisodeGamePack? = null,
    val quizBestStars: Int = 0,
    val tfBestStars: Int = 0,
    val matchBestStars: Int = 0,
    val customNotes: String = ""
) {
    val isReady: Boolean get() = summary != null && gamePack != null
    val totalStars: Int get() = quizBestStars + tfBestStars + matchBestStars
    val maxStars: Int get() = 9
    val youtubeUrl: String get() = "https://www.youtube.com/watch?v=$videoId"
}

enum class GameType {
    QUIZ,
    TRUE_FALSE,
    MATCH
}

enum class BadgeIconType {
    FIRST_SUMMARY,
    QUIZ_CHAMP,
    SPEED_STAR,
    MATCH_QUEEN,
    TEN_STARS,
    EXPLORER
}

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val progressLabel: String,
    val iconType: BadgeIconType
)

object EpisodeJsonCodec {
    fun encodeSummary(summary: EpisodeSummary): String {
        val obj = JSONObject()
        obj.put("friendlyTitle", summary.friendlyTitle)
        obj.put("hookIntro", summary.hookIntro)
        obj.put("easyStorySummary", summary.easyStorySummary)
        obj.put("practicalTip", summary.practicalTip)
        obj.put("encouragingQuote", summary.encouragingQuote)
        val pointsArr = JSONArray()
        summary.keyPoints.forEach { pt ->
            val pObj = JSONObject()
            pObj.put("title", pt.title)
            pObj.put("detail", pt.detail)
            pointsArr.put(pObj)
        }
        obj.put("keyPoints", pointsArr)
        return obj.toString()
    }

    fun decodeSummary(raw: String?): EpisodeSummary? {
        if (raw.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(raw)
            val ptsArr = obj.optJSONArray("keyPoints") ?: JSONArray()
            val pts = buildList {
                for (i in 0 until ptsArr.length()) {
                    val p = ptsArr.optJSONObject(i) ?: continue
                    add(
                        KeyPoint(
                            title = p.optString("title", ""),
                            detail = p.optString("detail", "")
                        )
                    )
                }
            }
            EpisodeSummary(
                friendlyTitle = obj.optString("friendlyTitle", ""),
                hookIntro = obj.optString("hookIntro", ""),
                easyStorySummary = obj.optString("easyStorySummary", ""),
                keyPoints = pts,
                practicalTip = obj.optString("practicalTip", ""),
                encouragingQuote = obj.optString("encouragingQuote", "")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun encodeGamePack(pack: EpisodeGamePack): String {
        val root = JSONObject()

        val quizArr = JSONArray()
        pack.quizQuestions.forEach { q ->
            val qObj = JSONObject()
            qObj.put("question", q.question)
            val opts = JSONArray()
            q.options.forEach { opts.put(it) }
            qObj.put("options", opts)
            qObj.put("correctIndex", q.correctIndex)
            qObj.put("hint", q.hint)
            qObj.put("explanation", q.explanation)
            quizArr.put(qObj)
        }
        root.put("quizQuestions", quizArr)

        val tfArr = JSONArray()
        pack.trueFalseQuestions.forEach { tf ->
            val tfObj = JSONObject()
            tfObj.put("statement", tf.statement)
            tfObj.put("isTrue", tf.isTrue)
            tfObj.put("explanation", tf.explanation)
            tfArr.put(tfObj)
        }
        root.put("trueFalseQuestions", tfArr)

        val matchArr = JSONArray()
        pack.matchPairs.forEach { mp ->
            val mpObj = JSONObject()
            mpObj.put("concept", mp.concept)
            mpObj.put("meaning", mp.meaning)
            matchArr.put(mpObj)
        }
        root.put("matchPairs", matchArr)

        return root.toString()
    }

    fun decodeGamePack(raw: String?): EpisodeGamePack? {
        if (raw.isNullOrBlank()) return null
        return try {
            val root = JSONObject(raw)

            val quizArr = root.optJSONArray("quizQuestions") ?: JSONArray()
            val quiz = buildList {
                for (i in 0 until quizArr.length()) {
                    val q = quizArr.optJSONObject(i) ?: continue
                    val optsArr = q.optJSONArray("options") ?: JSONArray()
                    val options = (0 until optsArr.length()).map { optsArr.optString(it, "") }
                    if (options.size >= 2) {
                        add(
                            QuizQuestion(
                                question = q.optString("question", ""),
                                options = options,
                                correctIndex = q.optInt("correctIndex", 0).coerceIn(0, options.lastIndex),
                                hint = q.optString("hint", ""),
                                explanation = q.optString("explanation", "")
                            )
                        )
                    }
                }
            }

            val tfArr = root.optJSONArray("trueFalseQuestions") ?: JSONArray()
            val tfs = buildList {
                for (i in 0 until tfArr.length()) {
                    val tf = tfArr.optJSONObject(i) ?: continue
                    add(
                        TrueFalseQuestion(
                            statement = tf.optString("statement", ""),
                            isTrue = tf.optBoolean("isTrue", true),
                            explanation = tf.optString("explanation", "")
                        )
                    )
                }
            }

            val matchArr = root.optJSONArray("matchPairs") ?: JSONArray()
            val pairs = buildList {
                for (i in 0 until matchArr.length()) {
                    val mp = matchArr.optJSONObject(i) ?: continue
                    add(
                        MatchPair(
                            concept = mp.optString("concept", ""),
                            meaning = mp.optString("meaning", "")
                        )
                    )
                }
            }

            if (quiz.isEmpty() && tfs.isEmpty() && pairs.isEmpty()) null
            else EpisodeGamePack(
                quizQuestions = quiz,
                trueFalseQuestions = tfs,
                matchPairs = pairs
            )
        } catch (_: Exception) {
            null
        }
    }
}
