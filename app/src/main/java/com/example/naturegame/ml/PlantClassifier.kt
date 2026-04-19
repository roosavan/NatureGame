package com.example.naturegame.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ML Kit -pohjainen luonnon tunnistaja (on-device Image Labeling).
 * Tunnistaa kasvit, eläimet, linnut, hyönteiset ja sienet.
 */
class PlantClassifier {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.4f)
            .build()
    )

    /**
     * Laajennettu avainsanalista kattamaan eri luontokategoriat.
     */
    private val natureKeywords = setOf(
        // Kasvit
        "plant", "flower", "tree", "shrub", "leaf", "fern", "moss", "grass", "herb", "bush", "flora",
        // Eläimet ja nisäkkäät
        "animal", "mammal", "wildlife", "fauna", "deer", "squirrel", "fox", "rabbit", "hare", "moose", "bear",
        // Linnut
        "bird", "avian", "owl", "eagle", "swan", "duck", "goose", "sparrow", "woodpecker",
        // Hyönteiset ja niveljalkaiset
        "insect", "bug", "butterfly", "moth", "beetle", "bee", "wasp", "ant", "spider", "dragonfly",
        // Sienet
        "mushroom", "fungus", "fungi", "toadstool",
        // Yleiset luontosanat
        "nature", "forest", "woodland", "wilderness", "outdoors"
    )

    suspend fun classify(imageUri: Uri, context: Context): ClassificationResult {
        return suspendCancellableCoroutine { continuation ->
            try {
                val inputImage = InputImage.fromFilePath(context, imageUri)

                labeler.process(inputImage)
                    .addOnSuccessListener { labels ->
                        // Suodatetaan luontoon liittyvät merkinnät
                        val natureLabels = labels.filter { label ->
                            natureKeywords.any { keyword ->
                                label.text.contains(keyword, ignoreCase = true)
                            }
                        }

                        val result = if (natureLabels.isNotEmpty()) {
                            val best = natureLabels.maxByOrNull { it.confidence }!!
                            ClassificationResult.Success(
                                label = translateLabel(best.text), // Käännetään yleisimmät suomeksi
                                confidence = best.confidence,
                                allLabels = labels.take(5)
                            )
                        } else {
                            ClassificationResult.NotNature(
                                allLabels = labels.take(3)
                            )
                        }
                        continuation.resume(result)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Yksinkertainen kääntäjä yleisimmille ML Kitin englanninkielisille termeille.
     */
    private fun translateLabel(label: String): String {
        return when (label.lowercase()) {
            "plant" -> "Kasvi"
            "flower" -> "Kukka"
            "tree" -> "Puu"
            "bird" -> "Lintu"
            "animal" -> "Eläin"
            "mammal" -> "Nisäkäs"
            "insect" -> "Hyönteinen"
            "butterfly" -> "Perhonen"
            "mushroom" -> "Sieni"
            "fungus" -> "Sieni"
            "leaf" -> "Lehti"
            "squirrel" -> "Orava"
            "forest" -> "Metsä"
            else -> label.replaceFirstChar { it.uppercase() }
        }
    }

    fun close() {
        labeler.close()
    }
}

sealed class ClassificationResult {
    data class Success(
        val label: String,
        val confidence: Float,
        val allLabels: List<ImageLabel>
    ) : ClassificationResult()

    data class NotNature(
        val allLabels: List<ImageLabel>
    ) : ClassificationResult()

    data class Error(val message: String) : ClassificationResult()
}
