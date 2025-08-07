package com.featurevisor.sdk.mocks

import com.featurevisor.sdk.DatafileFetchHandler
import com.featurevisor.types.DatafileContent
import com.featurevisor.utils.OpenForMokkery

@OpenForMokkery
class DatafileFetchHandlerTest : DatafileFetchHandler {
    override fun invoke(param: String): Result<DatafileContent> {
        println("TestDatafileFetchHandler.invoke called with: $param")
        return Result.failure(Throwable())
    }
}