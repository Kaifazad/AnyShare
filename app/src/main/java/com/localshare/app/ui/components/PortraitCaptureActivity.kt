package com.localshare.app.ui.components

import com.journeyapps.barcodescanner.CaptureActivity

/**
 * Custom CaptureActivity to force the QR scanner to open in portrait mode.
 * ZXing's default CaptureActivity is locked to landscape in its manifest.
 */
class PortraitCaptureActivity : CaptureActivity()
