package com.example.data.remote

import android.util.Log
import com.example.data.model.EpisodeGamePack
import com.example.data.model.EpisodeItem
import com.example.data.model.EpisodeSummary
import com.example.data.model.GeneratedEpisodeContent
import com.example.data.model.KeyPoint
import com.example.data.model.MatchPair
import com.example.data.model.QuizQuestion
import com.example.data.model.TrueFalseQuestion
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject

class EpisodeAiService {

    companion object {
        private const val TAG = "EpisodeAiService"
        private const val MODEL_NAME = "gemini-3.8-flash"
    }

    private val responseSchema: Schema = Schema.obj(
        properties = mapOf(
            "summary" to Schema.obj(
                properties = mapOf(
                    "friendlyTitle" to Schema.string("عنوان جذاب وبسيط للحلقة مناسب لبنات إعدادي"),
                    "hookIntro" to Schema.string("مقدمة دافئة ومشجعة في جملتين لبنات المرحلة الإعدادية"),
                    "easyStorySummary" to Schema.string("شرح مبسط جداً وممتع لأفكار الحلقة في فقرتين قصيرتين بأسلوب قريب من البنات"),
                    "keyPoints" to Schema.array(
                        items = Schema.obj(
                            properties = mapOf(
                                "title" to Schema.string("عنوان قصير للنقطة"),
                                "detail" to Schema.string("شرح سهل جداً في جملة واحدة")
                            )
                        ),
                        description = "4 إلى 5 نقاط أساسية من الحلقة"
                    ),
                    "practicalTip" to Schema.string("تطبيق عملي بسيط يمكن للبنت فعله اليوم في حياتها"),
                    "encouragingQuote" to Schema.string("جملة تشجيعية لطيفة وإيجابية في نهاية الملخص")
                )
            ),
            "gamePack" to Schema.obj(
                properties = mapOf(
                    "quizQuestions" to Schema.array(
                        items = Schema.obj(
                            properties = mapOf(
                                "question" to Schema.string("سؤال ممتع وواضح من الحلقة"),
                                "options" to Schema.array(
                                    items = Schema.string(),
                                    description = "4 اختيارات قصيرة وواضحة"
                                ),
                                "correctIndex" to Schema.integer("رقم الإجابة الصحيحة من 0 إلى 3"),
                                "hint" to Schema.string("تلميح لطيف يساعد الطالبة دون حرق الإجابة"),
                                "explanation" to Schema.string("شرح مشجع ومختصر يوضح سبب صحة الإجابة")
                            )
                        ),
                        description = "5 أسئلة اختيار من متعدد ممتعة"
                    ),
                    "trueFalseQuestions" to Schema.array(
                        items = Schema.obj(
                            properties = mapOf(
                                "statement" to Schema.string("عبارة قصيرة وواضحة من الحلقة للصح والخطأ"),
                                "isTrue" to Schema.boolean("هل العبارة صحيحة أم خاطئة"),
                                "explanation" to Schema.string("توضيح سريع ومشجع للعبارة")
                            )
                        ),
                        description = "5 عبارات للعبة صح أم خطأ السريعة"
                    ),
                    "matchPairs" to Schema.array(
                        items = Schema.obj(
                            properties = mapOf(
                                "concept" to Schema.string("فكرة أو مفهوم قصير من الحلقة (2 إلى 5 كلمات)"),
                                "meaning" to Schema.string("معناه أو تطبيقه البسيط (3 إلى 7 كلمات)")
                            )
                        ),
                        description = "4 أزواج للعبة التوصيل السحرية"
                    )
                )
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = this@EpisodeAiService.responseSchema
                temperature = 0.7f
            }
        )
    }

    suspend fun generateEpisodeSummaryAndGames(
        episode: EpisodeItem,
        playlistTitle: String,
        transcriptContext: VideoTranscriptContext
    ): GeneratedEpisodeContent {
        val mergedDescription = transcriptContext.description.ifBlank { episode.description }
        val prompt = buildString {
            appendLine("أنتِ معلمة مبدعة ولطيفة تصنعين ملخصات مبسطة وألعاباً تفاعلية ممتعة لبنات المرحلة الإعدادية (أعمار 11 إلى 14 سنة).")
            appendLine("اكتبي كل المحتوى باللغة العربية البسيطة الواضحة والدافئة والمشجعة (بأسلوب قريب من البنات، سهل الفهم، بدون تعقيد أو ألفاظ صعبة).")
            appendLine()
            appendLine("معلومات الحلقة:")
            appendLine("- اسم قائمة التشغيل: $playlistTitle")
            appendLine("- رقم الحلقة: ${episode.episodeNumber}")
            appendLine("- عنوان الحلقة: ${episode.title}")
            if (episode.channelName.isNotBlank()) {
                appendLine("- القناة / المقدم: ${episode.channelName}")
            }
            if (mergedDescription.isNotBlank()) {
                appendLine("- وصف الحلقة:")
                appendLine(mergedDescription.take(2500))
            }
            if (transcriptContext.transcript.isNotBlank()) {
                appendLine("- نص محتوى الحلقة (الترجمة/التفريغ):")
                appendLine(transcriptContext.transcript.take(8000))
            }
            if (episode.customNotes.isNotBlank()) {
                appendLine("- ملاحظات إضافية من المستخدمة يجب التركيز عليها:")
                appendLine(episode.customNotes.take(2000))
            }
            appendLine()
            appendLine("المطلوب بدقة:")
            appendLine("1. ملخص بسيط وسهل جداً ومرتب ومناسب لبنات إعدادي (keyPoints: 4 إلى 5 نقاط).")
            appendLine("2. لعبة مغامرة الاختيارات: 5 أسئلة اختيار من متعدد ممتعة، لكل سؤال 4 اختيارات بالضبط، مع correctIndex دقيق بين 0 و 3، وتلميح لطيف، وشرح مشجع.")
            appendLine("3. لعبة صح أم خطأ السريعة: 5 عبارات متنوعة (بعضها صح وبعضها خطأ) مع توضيح لطيف.")
            appendLine("4. لعبة التوصيل السحرية: 4 أزواج قصيرة وواضحة جداً بين الفكرة ومعناها أو تطبيقها.")
        }

        try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text
            if (rawJson.isNullOrBlank()) {
                throw IllegalStateException("لم يُرجع الذكاء الاصطناعي محتوى. حاولي مرة أخرى.")
            }
            return parseGeneratedJson(rawJson)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI generation timed out for ${episode.videoId}", e)
            throw IllegalStateException("استغرق تحضير الملخص وقتاً أطول من المعتاد. يُرجى المحاولة مرة أخرى.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI generation failed for ${episode.videoId}", e)
            throw IllegalStateException(
                e.localizedMessage ?: "حدث خطأ أثناء تحضير الملخص والألعاب. تأكدي من الإنترنت وحاولي مجدداً."
            )
        }
    }

    private fun parseGeneratedJson(rawJson: String): GeneratedEpisodeContent {
        val root = JSONObject(rawJson)
        val summaryObj = root.getJSONObject("summary")
        val keyPointsArr = summaryObj.optJSONArray("keyPoints") ?: JSONArray()
        val keyPoints = buildList {
            for (i in 0 until keyPointsArr.length()) {
                val item = keyPointsArr.optJSONObject(i) ?: continue
                add(
                    KeyPoint(
                        title = item.optString("title", ""),
                        detail = item.optString("detail", "")
                    )
                )
            }
        }

        val summary = EpisodeSummary(
            friendlyTitle = summaryObj.optString("friendlyTitle", ""),
            hookIntro = summaryObj.optString("hookIntro", ""),
            easyStorySummary = summaryObj.optString("easyStorySummary", ""),
            keyPoints = keyPoints,
            practicalTip = summaryObj.optString("practicalTip", ""),
            encouragingQuote = summaryObj.optString("encouragingQuote", "")
        )

        val packObj = root.getJSONObject("gamePack")

        val quizArr = packObj.optJSONArray("quizQuestions") ?: JSONArray()
        val quizList = buildList {
            for (i in 0 until quizArr.length()) {
                val q = quizArr.optJSONObject(i) ?: continue
                val optsArr = q.optJSONArray("options") ?: JSONArray()
                val options = (0 until optsArr.length()).map { optsArr.optString(it, "") }
                    .filter { it.isNotBlank() }
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

        val tfArr = packObj.optJSONArray("trueFalseQuestions") ?: JSONArray()
        val tfList = buildList {
            for (i in 0 until tfArr.length()) {
                val tf = tfArr.optJSONObject(i) ?: continue
                val stmt = tf.optString("statement", "")
                if (stmt.isNotBlank()) {
                    add(
                        TrueFalseQuestion(
                            statement = stmt,
                            isTrue = tf.optBoolean("isTrue", true),
                            explanation = tf.optString("explanation", "")
                        )
                    )
                }
            }
        }

        val matchArr = packObj.optJSONArray("matchPairs") ?: JSONArray()
        val matchList = buildList {
            for (i in 0 until matchArr.length()) {
                val mp = matchArr.optJSONObject(i) ?: continue
                val concept = mp.optString("concept", "")
                val meaning = mp.optString("meaning", "")
                if (concept.isNotBlank() && meaning.isNotBlank()) {
                    add(MatchPair(concept = concept, meaning = meaning))
                }
            }
        }

        return GeneratedEpisodeContent(
            summary = summary,
            gamePack = EpisodeGamePack(
                quizQuestions = quizList,
                trueFalseQuestions = tfList,
                matchPairs = matchList
            )
        )
    }
}
