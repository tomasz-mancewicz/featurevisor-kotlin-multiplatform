package com.featurevisor.sdk

import com.featurevisor.types.*
import com.featurevisor.types.EventName.*
import kotlinx.serialization.json.Json

typealias ConfigureBucketKey = (Feature, Context, BucketKey) -> BucketKey
typealias ConfigureBucketValue = (Feature, Context, BucketValue) -> BucketValue
typealias InterceptContext = (Context) -> Context

class FeaturevisorInstance private constructor(
    datafile: DatafileContent?,
    options: InstanceOptions,
) {
    companion object {
        fun createInstance(
            datafile: DatafileContent? = null,
            options: InstanceOptions = InstanceOptions()
        ): FeaturevisorInstance {
            return FeaturevisorInstance(datafile, options)
        }

        var companionLogger: Logger? = null
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Simple event emitter for refresh notifications
    internal val emitter: Emitter = Emitter()

    // Configuration
    internal val logger = options.logger?.also { companionLogger = it }
    internal val initialFeatures = options.initialFeatures
    internal val interceptContext = options.interceptContext
    internal var stickyFeatures = options.stickyFeatures
    internal var bucketKeySeparator = options.bucketKeySeparator
    internal var configureBucketKey = options.configureBucketKey
    internal var configureBucketValue = options.configureBucketValue

    // The main datafile reader
    internal var datafileReader: DatafileReader

    init {
        logger?.debug("Creating FeaturevisorInstance")

        // Set up event listeners if provided
        options.onRefresh?.let { emitter.addListener(REFRESH, it) }
        options.onActivation?.let { emitter.addListener(ACTIVATION, it) }

        // Initialize with provided datafile or empty one
        val initialDatafile = datafile ?: createEmptyDatafile()
        datafileReader = DatafileReader(initialDatafile)

        logger?.debug("FeaturevisorInstance created with ${initialDatafile.features.size} features")
    }

    /**
     * Update the datafile and notify listeners
     */
    fun setDatafile(datafileContent: DatafileContent) {
        val oldRevision = datafileReader.getRevision()
        datafileReader = DatafileReader(datafileContent)

        logger?.debug("Datafile updated", mapOf(
            "oldRevision" to oldRevision,
            "newRevision" to datafileContent.revision,
            "features" to datafileContent.features.size
        ))

        // Emit refresh event
        emitter.emit(REFRESH, datafileContent)
    }

    /**
     * Update datafile from JSON string
     */
    fun setDatafile(datafileJSON: String) {
        try {
            val datafileContent = json.decodeFromString<DatafileContent>(datafileJSON)

            setDatafile(datafileContent)
        } catch (e: Exception) {
            logger?.error("Could not parse datafile JSON", mapOf("error" to e))
            throw e
        }
    }

    // Configuration methods
    fun setLogLevel(level: Logger.LogLevel) {
        logger?.setLevel(level)
    }

    fun setStickyFeatures(stickyFeatures: StickyFeatures?) {
        this.stickyFeatures = stickyFeatures
    }

    // Simple getters
    fun getRevision(): String = datafileReader.getRevision()

    // Clean shutdown
    fun shutdown() {
        emitter.removeAllListeners()
        logger?.debug("FeaturevisorInstance shut down")
    }

    private fun createEmptyDatafile() = DatafileContent(
        schemaVersion = "1",
        revision = "empty",
        attributes = emptyList(),
        segments = emptyList(),
        features = emptyList(),
    )
}