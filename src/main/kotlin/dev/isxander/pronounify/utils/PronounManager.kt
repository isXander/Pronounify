package dev.isxander.pronounify.utils

import com.google.common.cache.CacheBuilder
import com.google.common.collect.Sets
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.*

object PronounManager {
    private val pronounsCache = CacheBuilder.newBuilder().apply {
        expireAfterAccess(Duration.ofMinutes(10))
        maximumSize(500)
    }.build<UUID, Pronouns>()

    private val inProgressFetching = Sets.newConcurrentHashSet<UUID>()

    private val pronounEvents = mutableMapOf<UUID, MutableList<(Pronouns) -> Unit>>()

    fun isCurrentlyFetching(uuid: UUID) = uuid in inProgressFetching

    fun isPronounCached(uuid: UUID) = pronounsCache.getIfPresent(uuid) != null

    fun getPronoun(uuid: UUID) = pronounsCache.getIfPresent(uuid)!!

    @JvmOverloads
    fun cachePronoun(uuid: UUID, completion: (Pronouns) -> Unit = {}) {
        if (isPronounCached(uuid) || isCurrentlyFetching(uuid))
            return

        listenToPronounGet(uuid, completion)

        inProgressFetching += uuid

        runAsync {
            try {
                val httpClient = HttpClient.newHttpClient()
                val url = URI.create("https://pronoundb.org/api/v1/lookup?platform=minecraft&id=$uuid")
                val request = HttpRequest.newBuilder(url).build()
                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

                val pronouns = Json.decodeFromString<SingleLookupResponse>(response.body()).toEnum()!!
                pronounsCache.put(uuid, pronouns)

                pronounEvents[uuid]?.forEach {
                    it(pronouns)
                }
                pronounEvents.remove(uuid)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                inProgressFetching -= uuid
            }
        }
    }

    fun bulkCachePronouns(uuids: MutableCollection<UUID>) {
        val filtered = uuids.filterNot { isCurrentlyFetching(it) || isPronounCached(it) }
        val chunked = filtered.chunked(50)

        inProgressFetching.addAll(filtered)

        runAsync { runBlocking {
            val httpClient = HttpClient.newHttpClient()
            coroutineScope {
                chunked.map {
                    async {
                        try {
                            val url = URI.create("https://pronoundb.org/api/v1/lookup-bulk?platform=minecraft&ids=${it.joinToString(",")}")
                            val request = HttpRequest.newBuilder(url).build()
                            val response = withContext(Dispatchers.IO) {
                                httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                            }

                            val pronouns = Json.decodeFromString<Map<String, String>>(response.body())
                                .mapKeys { (k, _) -> UUID.fromString(k) }
                                .mapValues { (_, v) -> Pronouns.fromId(v) }
                            pronounsCache.putAll(pronouns)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            inProgressFetching.removeAll(it.toSet())
                        }
                    }
                }.awaitAll()
            }
        }}
    }

    fun listenToPronounGet(uuid: UUID, listener: (Pronouns) -> Unit) {
        pronounEvents.getOrPut(uuid) { mutableListOf() } += listener
    }

    @Serializable
    private data class SingleLookupResponse(val pronouns: String) {
        fun toEnum() = Pronouns.fromId(pronouns)
    }
}
