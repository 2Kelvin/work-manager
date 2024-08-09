package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluromatic.DELAY_TIME_MILLIS
import com.example.bluromatic.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAG = "BlueWorker"

class BlurWorker(ctx: Context, params: WorkerParameters)
    : CoroutineWorker(ctx, params)
{

    override suspend fun doWork(): Result {
        makeStatusNotification(
            applicationContext.resources.getString(R.string.blurring_image),
            applicationContext
        )

        return withContext(Dispatchers.IO) {
            return@withContext try {
                // emulating slower work for delay between notification messages
                delay(DELAY_TIME_MILLIS)

                val picture = BitmapFactory.decodeResource(
                    applicationContext.resources,
                    R.drawable.android_cupcake
                )

                // blurring the image
                val output = blurBitmap(picture, 1)
                // writing the blurred image to a temporary file
                val outputUri = writeBitmapToFile(applicationContext, output)
                // displaying a message to the UI of the blurred image output file
                makeStatusNotification("Output is $outputUri", applicationContext)

                Result.success()
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    applicationContext.resources.getString(R.string.error_applying_blur),
                    throwable
                )

                Result.failure()
            }
        }
    }
}