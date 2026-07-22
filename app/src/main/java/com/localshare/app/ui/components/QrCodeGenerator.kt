package com.localshare.app.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generates a QR code bitmap from a URL string using ZXing.
 */
object QrCodeGenerator {

    /**
     * Generate a QR code Bitmap with the given content.
     */
    fun generate(
        content: String,
        size: Int = 512,
        foregroundColor: Int = Color.WHITE,
        backgroundColor: Int = Color.TRANSPARENT
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
            }
        }

        return bitmap
    }
}

/**
 * Composable that displays a QR code for the given URL.
 */
@Composable
fun QrCodeImage(
    url: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    foregroundColor: Int = Color.WHITE,
    backgroundColor: Int = Color.TRANSPARENT
) {
    val bitmap = remember(url) {
        QrCodeGenerator.generate(
            content = url,
            size = 512,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor
        )
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR Code for $url",
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
    )
}
