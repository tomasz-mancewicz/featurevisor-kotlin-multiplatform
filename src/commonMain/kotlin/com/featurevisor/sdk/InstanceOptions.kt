package com.featurevisor.sdk

import com.featurevisor.types.DatafileContent
import com.featurevisor.types.InitialFeatures
import com.featurevisor.types.StickyFeatures
import kotlinx.coroutines.CoroutineScope

typealias Listener = (Array<out Any>) -> Unit

data class InstanceOptions(
    val bucketKeySeparator: String = ".",
    val configureBucketKey: ConfigureBucketKey? = null,
    val configureBucketValue: ConfigureBucketValue? = null,
    val initialFeatures: InitialFeatures? = null,
    val interceptContext: InterceptContext? = null,
    val logger: Logger? = null,
    val onRefresh: Listener? = null,
    val onActivation: Listener? = null,
    val stickyFeatures: StickyFeatures? = null,
)
