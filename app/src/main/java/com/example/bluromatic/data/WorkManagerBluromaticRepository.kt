package com.example.bluromatic.data

import android.content.Context
import android.net.Uri
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
import com.example.bluromatic.getImageUri
import com.example.bluromatic.workers.BlurWorker
import com.example.bluromatic.workers.CleanupWorker
import com.example.bluromatic.workers.SaveImageToFileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class WorkManagerBluromaticRepository(context: Context) : BluromaticRepository {

    private val imageUri: Uri = context.getImageUri()
    private val workManager = WorkManager.getInstance(context)
    override val outputWorkInfo: Flow<WorkInfo?> = MutableStateFlow(null)

    /**
     * Create the WorkRequests to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image
     */
    override fun applyBlur(blurLevel: Int) {
        // work request cleaning up temp image files
        var continuation = workManager.beginWith(
            OneTimeWorkRequest.Companion.from(CleanupWorker::class.java)
        )
        val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>() // creating a work request to blur the image
        blurBuilder.setInputData(
            createInputDataForWorkRequest(blurLevel, imageUri)
        )

        continuation = continuation.then(blurBuilder.build())
        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>().build()
        continuation = continuation.then(save)

        continuation.enqueue() // starting the above chained work
    }

    /**
     * Cancel any ongoing WorkRequests
     * */
    override fun cancelWork() {}

    /**
     * Creates the input data bundle which includes the blur level to
     * update the amount of blur to be applied and the Uri to operate on
     * @return Data which contains the Image Uri as a String and blur level as an Integer
     */
    private fun createInputDataForWorkRequest(blurLevel: Int, imageUri: Uri): Data {
        val builder = Data.Builder()
        builder.putString(KEY_IMAGE_URI, imageUri.toString()).putInt(KEY_BLUR_LEVEL, blurLevel)
        return builder.build()
    }
}
