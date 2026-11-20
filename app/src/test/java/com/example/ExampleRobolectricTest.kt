package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EpisodeGamePack
import com.example.data.model.EpisodeJsonCodec
import com.example.data.model.EpisodeSummary
import com.example.data.model.KeyPoint
import com.example.data.model.MatchPair
import com.example.data.model.QuizQuestion
import com.example.data.model.TrueFalseQuestion
import com.example.data.remote.YouTubePlaylistService
import com.example.data.remote.YouTubeTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("نجمة الحلقات", appName)
    }

    @Test
    fun `parse user playlist url extracts exact playlist id`() {
        val service = YouTubePlaylistService()
        val url = "https://youtube.com/playlist?list=PL7ZS5BFtiPLKP_GwZwaNuNA6N37f95xEt&si=9NDH60RUD4_Li1zd"
        val target = service.parseYouTubeTarget(url)
        assertEquals(
            YouTubeTarget.Playlist("PL7ZS5BFtiPLKP_GwZwaNuNA6N37f95xEt"),
            target
        )
    }

    @Test
    fun `json codec round trips summary and games`() {
        val summary = EpisodeSummary(
            friendlyTitle = "عنوان بسيط",
            hookIntro = "أهلاً يا نجمة!",
            easyStorySummary = "شرح سهل وممتع.",
            keyPoints = listOf(KeyPoint("نقطة أولى", "تفصيل بسيط")),
            practicalTip = "جربي تطبيق هذا اليوم.",
            encouragingQuote = "أنتِ رائعة!"
        )
        val decodedSummary = EpisodeJsonCodec.decodeSummary(EpisodeJsonCodec.encodeSummary(summary))
        assertEquals(summary, decodedSummary)

        val pack = EpisodeGamePack(
            quizQuestions = listOf(
                QuizQuestion("سؤال؟", listOf("أ", "ب", "ج", "د"), 1, "تلميح", "شرح")
            ),
            trueFalseQuestions = listOf(
                TrueFalseQuestion("عبارة صحيحة", true, "توضيح")
            ),
            matchPairs = listOf(
                MatchPair("الفكرة", "المعنى")
            )
        )
        val decodedPack = EpisodeJsonCodec.decodeGamePack(EpisodeJsonCodec.encodeGamePack(pack))
        assertNotNull(decodedPack)
        assertEquals(pack, decodedPack)
    }
}
