package com.example.data.remote

import android.net.Uri
import android.util.Log
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader
import java.util.concurrent.TimeUnit

data class RemoteEpisode(
    val videoId: String,
    val episodeNumber: Int,
    val title: String,
    val channelName: String,
    val durationText: String,
    val thumbnailUrl: String,
    val description: String = ""
)

data class RemotePlaylistResult(
    val playlistId: String,
    val playlistTitle: String,
    val episodes: List<RemoteEpisode>
)

data class VideoTranscriptContext(
    val title: String,
    val channelName: String,
    val description: String,
    val transcript: String
)

sealed interface YouTubeTarget {
    data class Playlist(val playlistId: String) : YouTubeTarget
    data class SingleVideo(val videoId: String) : YouTubeTarget
}

class YouTubePlaylistService {

    companion object {
        private const val TAG = "YouTubePlaylistService"
        const val DEFAULT_PLAYLIST_ID = "PL7ZS5BFtiPLKP_GwZwaNuNA6N37f95xEt"
        const val DEFAULT_PLAYLIST_URL =
            "https://youtube.com/playlist?list=PL7ZS5BFtiPLKP_GwZwaNuNA6N37f95xEt"
        private const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun parseYouTubeTarget(rawInput: String): YouTubeTarget? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return null

        // Check direct playlist ID (starts with PL, UU, OL, RD, LL)
        if (trimmed.matches(Regex("^(PL|UU|OL|LL)[A-Za-z0-9_-]{10,}$"))) {
            return YouTubeTarget.Playlist(trimmed)
        }

        // Check direct 11-char video ID
        if (trimmed.matches(Regex("^[A-Za-z0-9_-]{11}$"))) {
            return YouTubeTarget.SingleVideo(trimmed)
        }

        val normalizedUrl = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }

        return try {
            val uri = Uri.parse(normalizedUrl)
            val listParam = uri.getQueryParameter("list")
            if (!listParam.isNullOrBlank()) {
                return YouTubeTarget.Playlist(listParam)
            }
            val videoParam = uri.getQueryParameter("v")
            if (!videoParam.isNullOrBlank()) {
                return YouTubeTarget.SingleVideo(videoParam)
            }
            val host = uri.host.orEmpty().lowercase()
            val segments = uri.pathSegments.orEmpty()
            when {
                host.contains("youtu.be") && segments.isNotEmpty() ->
                    YouTubeTarget.SingleVideo(segments.first())
                segments.size >= 2 && (segments[0] == "shorts" || segments[0] == "embed" || segments[0] == "live") ->
                    YouTubeTarget.SingleVideo(segments[1])
                else -> null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse YouTube URL: $rawInput", e)
            null
        }
    }

    suspend fun fetchTarget(target: YouTubeTarget): RemotePlaylistResult = withContext(Dispatchers.IO) {
        when (target) {
            is YouTubeTarget.Playlist -> fetchPlaylist(target.playlistId)
            is YouTubeTarget.SingleVideo -> fetchSingleVideoAsPlaylist(target.videoId)
        }
    }

    suspend fun fetchPlaylist(playlistId: String): RemotePlaylistResult = withContext(Dispatchers.IO) {
        // 1. Try desktop playlist HTML (contains full ytInitialData with all episodes & durations)
        val htmlResult = runCatching { fetchPlaylistFromHtml(playlistId) }
            .onFailure { Log.w(TAG, "HTML playlist fetch failed, trying RSS fallback", it) }
            .getOrNull()

        if (htmlResult != null && htmlResult.episodes.isNotEmpty()) {
            return@withContext htmlResult
        }

        // 2. Fallback: YouTube official Atom RSS feed (public playlists)
        val rssResult = runCatching { fetchPlaylistFromRss(playlistId) }
            .onFailure { Log.w(TAG, "RSS playlist fetch failed", it) }
            .getOrNull()

        if (rssResult != null && rssResult.episodes.isNotEmpty()) {
            return@withContext rssResult
        }

        throw IllegalStateException(
            "تعذر تحميل الحلقات من قائمة التشغيل. تأكدي من اتصال الإنترنت وأن قائمة التشغيل عامة (Public) وليست خاصة."
        )
    }

    private fun fetchPlaylistFromHtml(playlistId: String): RemotePlaylistResult {
        val url = "https://www.youtube.com/playlist?list=$playlistId"
        val html = executeGet(url, DESKTOP_USER_AGENT)
        val jsonText = extractJsonObjectAfterMarker(html, "var ytInitialData = ")
            ?: extractJsonObjectAfterMarker(html, "window[\"ytInitialData\"] = ")
            ?: throw IllegalStateException("ytInitialData not found")

        val root = JSONObject(jsonText)
        val playlistTitle = root.optJSONObject("metadata")
            ?.optJSONObject("playlistMetadataRenderer")
            ?.optString("title")
            ?.takeIf { it.isNotBlank() }
            ?: root.optJSONObject("header")
                ?.optJSONObject("playlistHeaderRenderer")
                ?.optJSONObject("title")
                ?.optString("simpleText")
                ?.takeIf { it.isNotBlank() }
            ?: "قائمة الحلقات"

        val renderers = mutableListOf<JSONObject>()
        collectObjectsByKey(root, "playlistVideoRenderer", renderers)

        val seen = LinkedHashSet<String>()
        val episodes = mutableListOf<RemoteEpisode>()

        renderers.forEach { renderer ->
            val videoId = renderer.optString("videoId", "")
            if (videoId.isBlank() || !seen.add(videoId)) return@forEach

            val title = extractRunsOrSimpleText(renderer.optJSONObject("title"))
                .ifBlank { "حلقة رقم ${episodes.size + 1}" }
            val channel = extractRunsOrSimpleText(renderer.optJSONObject("shortBylineText"))
            val duration = extractRunsOrSimpleText(renderer.optJSONObject("lengthText"))
            val thumb = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"

            episodes.add(
                RemoteEpisode(
                    videoId = videoId,
                    episodeNumber = episodes.size + 1,
                    title = title,
                    channelName = channel,
                    durationText = duration,
                    thumbnailUrl = thumb
                )
            )
        }

        return RemotePlaylistResult(
            playlistId = playlistId,
            playlistTitle = playlistTitle,
            episodes = episodes
        )
    }

    private fun fetchPlaylistFromRss(playlistId: String): RemotePlaylistResult {
        val url = "https://www.youtube.com/feeds/videos.xml?playlist_id=$playlistId"
        val xmlContent = executeGet(url, USER_AGENT)

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xmlContent))

        var feedTitle = "قائمة الحلقات"
        val episodes = mutableListOf<RemoteEpisode>()

        var inEntry = false
        var inAuthor = false
        var currentTag = ""
        var currentVideoId = ""
        var currentTitle = ""
        var currentAuthor = ""
        var currentDescription = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "entry") {
                        inEntry = true
                        currentVideoId = ""
                        currentTitle = ""
                        currentAuthor = ""
                        currentDescription = ""
                    } else if (currentTag == "author") {
                        inAuthor = true
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text.orEmpty().trim()
                    if (text.isNotEmpty()) {
                        if (!inEntry && currentTag == "title") {
                            feedTitle = text
                        } else if (inEntry) {
                            when (currentTag) {
                                "yt:videoId" -> currentVideoId = text
                                "title" -> if (currentTitle.isEmpty()) currentTitle = text
                                "name" -> if (inAuthor) currentAuthor = text
                                "media:description" -> currentDescription = text
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "author") {
                        inAuthor = false
                    } else if (parser.name == "entry" && inEntry) {
                        inEntry = false
                        if (currentVideoId.isNotBlank()) {
                            episodes.add(
                                RemoteEpisode(
                                    videoId = currentVideoId,
                                    episodeNumber = episodes.size + 1,
                                    title = currentTitle.ifBlank { "حلقة ${episodes.size + 1}" },
                                    channelName = currentAuthor,
                                    durationText = "",
                                    thumbnailUrl = "https://i.ytimg.com/vi/$currentVideoId/hqdefault.jpg",
                                    description = currentDescription
                                )
                            )
                        }
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        return RemotePlaylistResult(
            playlistId = playlistId,
            playlistTitle = feedTitle,
            episodes = episodes
        )
    }

    private fun fetchSingleVideoAsPlaylist(videoId: String): RemotePlaylistResult {
        val context = runCatching { fetchVideoContextSync(videoId) }.getOrNull()
        val oEmbed = if (context == null || context.title.isBlank()) {
            runCatching {
                val body = executeGet(
                    "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json",
                    USER_AGENT
                )
                JSONObject(body)
            }.getOrNull()
        } else null

        val title = context?.title?.takeIf { it.isNotBlank() }
            ?: oEmbed?.optString("title")?.takeIf { it.isNotBlank() }
            ?: "حلقة يوتيوب"
        val channel = context?.channelName?.takeIf { it.isNotBlank() }
            ?: oEmbed?.optString("author_name").orEmpty()

        return RemotePlaylistResult(
            playlistId = "single_$videoId",
            playlistTitle = title,
            episodes = listOf(
                RemoteEpisode(
                    videoId = videoId,
                    episodeNumber = 1,
                    title = title,
                    channelName = channel,
                    durationText = "",
                    thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                    description = context?.description.orEmpty()
                )
            )
        )
    }

    suspend fun fetchVideoContext(videoId: String): VideoTranscriptContext = withContext(Dispatchers.IO) {
        runCatching { fetchVideoContextSync(videoId) }
            .onFailure { Log.w(TAG, "Could not fetch extra video context for $videoId", it) }
            .getOrDefault(VideoTranscriptContext("", "", "", ""))
    }

    private fun fetchVideoContextSync(videoId: String): VideoTranscriptContext {
        val html = executeGet("https://www.youtube.com/watch?v=$videoId", DESKTOP_USER_AGENT)
        val playerJsonText = extractJsonObjectAfterMarker(html, "var ytInitialPlayerResponse = ")
            ?: return VideoTranscriptContext("", "", "", "")

        val playerJson = JSONObject(playerJsonText)
        val details = playerJson.optJSONObject("videoDetails")
        val title = details?.optString("title").orEmpty()
        val author = details?.optString("author").orEmpty()
        val description = details?.optString("shortDescription").orEmpty()

        val tracks = playerJson.optJSONObject("captions")
            ?.optJSONObject("playerCaptionsTracklistRenderer")
            ?.optJSONArray("captionTracks")

        val transcript = if (tracks != null && tracks.length() > 0) {
            extractBestTranscript(tracks)
        } else {
            ""
        }

        return VideoTranscriptContext(
            title = title,
            channelName = author,
            description = description,
            transcript = transcript
        )
    }

    private fun extractBestTranscript(tracks: JSONArray): String {
        var chosenUrl = ""
        // Prefer Arabic track first, then any track
        for (i in 0 until tracks.length()) {
            val track = tracks.optJSONObject(i) ?: continue
            val lang = track.optString("languageCode", "")
            val url = track.optString("baseUrl", "")
            if (lang.startsWith("ar") && url.isNotBlank()) {
                chosenUrl = url
                break
            }
            if (chosenUrl.isBlank() && url.isNotBlank()) {
                chosenUrl = url
            }
        }
        if (chosenUrl.isBlank()) return ""

        return runCatching {
            val xml = executeGet(chosenUrl, DESKTOP_USER_AGENT)
            val sb = StringBuilder()
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(StringReader(xml))
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT && sb.length < 9000) {
                if (event == XmlPullParser.TEXT) {
                    val cleaned = decodeHtmlEntities(parser.text.orEmpty())
                        .replace('\n', ' ')
                        .trim()
                    if (cleaned.isNotEmpty()) {
                        sb.append(cleaned).append(' ')
                    }
                }
                event = parser.next()
            }
            sb.toString().trim().take(9000)
        }.getOrDefault("")
    }

    private fun executeGet(url: String, userAgent: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Accept-Language", "ar,en-US;q=0.9,en;q=0.8")
            .header("Cookie", "CONSENT=YES+cb.20210328-17-p0.en+FX+667")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code} while fetching YouTube data")
            }
            return response.body?.string().orEmpty()
        }
    }

    private fun extractJsonObjectAfterMarker(source: String, marker: String): String? {
        val markerIdx = source.indexOf(marker)
        if (markerIdx == -1) return null
        val start = source.indexOf('{', markerIdx + marker.length)
        if (start == -1) return null

        var depth = 0
        var inString = false
        var escaped = false
        for (i in start until source.length) {
            val c = source[i]
            if (escaped) {
                escaped = false
                continue
            }
            if (inString) {
                if (c == '\\') {
                    escaped = true
                } else if (c == '"') {
                    inString = false
                }
            } else {
                when (c) {
                    '"' -> inString = true
                    '{' -> depth++
                    '}' -> {
                        depth--
                        if (depth == 0) {
                            return source.substring(start, i + 1)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun collectObjectsByKey(node: Any?, key: String, out: MutableList<JSONObject>) {
        when (node) {
            is JSONObject -> {
                if (node.has(key)) {
                    node.optJSONObject(key)?.let { out.add(it) }
                }
                val keys = node.keys()
                while (keys.hasNext()) {
                    val nextKey = keys.next()
                    if (nextKey != key) {
                        collectObjectsByKey(node.opt(nextKey), key, out)
                    }
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    collectObjectsByKey(node.opt(i), key, out)
                }
            }
        }
    }

    private fun extractRunsOrSimpleText(obj: JSONObject?): String {
        if (obj == null) return ""
        val simple = obj.optString("simpleText", "")
        if (simple.isNotBlank()) return simple
        val runs = obj.optJSONArray("runs") ?: return ""
        val sb = StringBuilder()
        for (i in 0 until runs.length()) {
            sb.append(runs.optJSONObject(i)?.optString("text").orEmpty())
        }
        return sb.toString().trim()
    }

    private fun decodeHtmlEntities(input: String): String {
        return input
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#10;", " ")
    }
}
