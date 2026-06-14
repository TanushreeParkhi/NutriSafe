package com.priveat.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object TinyIcons {
    val Menu get() = icon("Menu") { moveTo(4f, 6f); lineTo(20f, 6f); moveTo(4f, 12f); lineTo(20f, 12f); moveTo(4f, 18f); lineTo(20f, 18f) }
    val Close get() = icon("Close") { moveTo(6f, 6f); lineTo(18f, 18f); moveTo(18f, 6f); lineTo(6f, 18f) }
    val Person get() = icon("Person") { moveTo(12f, 12f); moveTo(16f, 8f); arcTo(4f, 4f, 0f, true, true, 8f, 8f); arcTo(4f, 4f, 0f, true, true, 16f, 8f); moveTo(5f, 21f); curveTo(6.5f, 16.5f, 17.5f, 16.5f, 19f, 21f) }
    val PersonAdd get() = icon("PersonAdd") { moveTo(12f, 12f); moveTo(16f, 8f); arcTo(4f, 4f, 0f, true, true, 8f, 8f); arcTo(4f, 4f, 0f, true, true, 16f, 8f); moveTo(4f, 21f); curveTo(5.5f, 16.5f, 15f, 16.5f, 16.5f, 21f); moveTo(19f, 10f); lineTo(19f, 16f); moveTo(16f, 13f); lineTo(22f, 13f) }
    val Home get() = icon("Home") { moveTo(3f, 11f); lineTo(12f, 4f); lineTo(21f, 11f); moveTo(5f, 10f); lineTo(5f, 21f); lineTo(19f, 21f); lineTo(19f, 10f); moveTo(10f, 21f); lineTo(10f, 15f); lineTo(14f, 15f); lineTo(14f, 21f) }
    val Settings get() = icon("Settings") { moveTo(12f, 8f); arcTo(4f, 4f, 0f, true, true, 12f, 16f); arcTo(4f, 4f, 0f, true, true, 12f, 8f); moveTo(12f, 2f); lineTo(12f, 5f); moveTo(12f, 19f); lineTo(12f, 22f); moveTo(2f, 12f); lineTo(5f, 12f); moveTo(19f, 12f); lineTo(22f, 12f); moveTo(4.9f, 4.9f); lineTo(7f, 7f); moveTo(17f, 17f); lineTo(19.1f, 19.1f); moveTo(19.1f, 4.9f); lineTo(17f, 7f); moveTo(7f, 17f); lineTo(4.9f, 19.1f) }
    val BarChart get() = icon("BarChart") { moveTo(4f, 20f); lineTo(20f, 20f); moveTo(6f, 17f); lineTo(6f, 11f); moveTo(12f, 17f); lineTo(12f, 7f); moveTo(18f, 17f); lineTo(18f, 4f) }
    val Restaurant get() = icon("Restaurant") { moveTo(7f, 3f); lineTo(7f, 21f); moveTo(4f, 3f); lineTo(4f, 9f); moveTo(10f, 3f); lineTo(10f, 9f); moveTo(4f, 9f); lineTo(10f, 9f); moveTo(17f, 3f); curveTo(21f, 7f, 21f, 13f, 17f, 15f); lineTo(17f, 21f) }
    val AutoAwesome get() = icon("AutoAwesome") { moveTo(12f, 3f); lineTo(14f, 10f); lineTo(21f, 12f); lineTo(14f, 14f); lineTo(12f, 21f); lineTo(10f, 14f); lineTo(3f, 12f); lineTo(10f, 10f); close(); moveTo(18f, 3f); lineTo(19f, 6f); lineTo(22f, 7f); moveTo(6f, 17f); lineTo(5f, 21f); lineTo(2f, 22f) }
    val Security get() = Shield
    val Shield get() = icon("Shield") { moveTo(12f, 3f); lineTo(20f, 6f); lineTo(19f, 13f); curveTo(18f, 17f, 15f, 20f, 12f, 21f); curveTo(9f, 20f, 6f, 17f, 5f, 13f); lineTo(4f, 6f); close(); moveTo(9f, 12f); lineTo(11f, 14f); lineTo(15.5f, 9.5f) }
    val Delete get() = icon("Delete") { moveTo(5f, 7f); lineTo(19f, 7f); moveTo(9f, 7f); lineTo(9f, 4f); lineTo(15f, 4f); lineTo(15f, 7f); moveTo(7f, 7f); lineTo(8f, 21f); lineTo(16f, 21f); lineTo(17f, 7f); moveTo(10f, 11f); lineTo(10f, 17f); moveTo(14f, 11f); lineTo(14f, 17f) }
    val DeleteOutline get() = Delete
    val ArrowForward get() = icon("ArrowForward") { moveTo(4f, 12f); lineTo(19f, 12f); moveTo(13f, 6f); lineTo(19f, 12f); lineTo(13f, 18f) }
    val KeyboardArrowUp get() = icon("KeyboardArrowUp") { moveTo(6f, 15f); lineTo(12f, 9f); lineTo(18f, 15f) }
    val CameraAlt get() = icon("CameraAlt") { moveTo(4f, 8f); lineTo(8f, 8f); lineTo(9.5f, 5f); lineTo(14.5f, 5f); lineTo(16f, 8f); lineTo(20f, 8f); lineTo(20f, 19f); lineTo(4f, 19f); close(); moveTo(12f, 11f); arcTo(3f, 3f, 0f, true, true, 12f, 17f); arcTo(3f, 3f, 0f, true, true, 12f, 11f) }
    val LocalFireDepartment get() = icon("LocalFireDepartment") { moveTo(13f, 3f); curveTo(17f, 7f, 18f, 10f, 15f, 13f); curveTo(18f, 13f, 20f, 16f, 18f, 19f); curveTo(16f, 22f, 8f, 22f, 6f, 18f); curveTo(4f, 14f, 7f, 10f, 10f, 8f); curveTo(10f, 11f, 12f, 12f, 13f, 3f) }
    val Info get() = icon("Info") { moveTo(12f, 3f); arcTo(9f, 9f, 0f, true, true, 12f, 21f); arcTo(9f, 9f, 0f, true, true, 12f, 3f); moveTo(12f, 10f); lineTo(12f, 16f); moveTo(12f, 7f); lineTo(12.1f, 7f) }
    val Lock get() = icon("Lock") { moveTo(7f, 11f); lineTo(17f, 11f); lineTo(17f, 21f); lineTo(7f, 21f); close(); moveTo(9f, 11f); lineTo(9f, 8f); curveTo(9f, 3f, 15f, 3f, 15f, 8f); lineTo(15f, 11f) }
    val UploadFile get() = icon("UploadFile") { moveTo(12f, 17f); lineTo(12f, 5f); moveTo(7f, 10f); lineTo(12f, 5f); lineTo(17f, 10f); moveTo(5f, 19f); lineTo(19f, 19f) }
    val AccessTime get() = icon("AccessTime") { moveTo(12f, 3f); arcTo(9f, 9f, 0f, true, true, 12f, 21f); arcTo(9f, 9f, 0f, true, true, 12f, 3f); moveTo(12f, 7f); lineTo(12f, 12f); lineTo(16f, 14f) }
    val Science get() = icon("Science") { moveTo(9f, 3f); lineTo(15f, 3f); moveTo(10f, 3f); lineTo(10f, 9f); lineTo(5f, 19f); curveTo(4f, 21f, 20f, 21f, 19f, 19f); lineTo(14f, 9f); lineTo(14f, 3f); moveTo(8f, 15f); lineTo(16f, 15f) }
    val CheckCircle get() = icon("CheckCircle") { moveTo(12f, 3f); arcTo(9f, 9f, 0f, true, true, 12f, 21f); arcTo(9f, 9f, 0f, true, true, 12f, 3f); moveTo(8f, 12f); lineTo(11f, 15f); lineTo(16f, 9f) }
    val Phone get() = icon("Phone") { moveTo(7f, 4f); curveTo(5f, 5f, 5f, 8f, 7f, 11f); curveTo(9f, 15f, 12f, 18f, 16f, 19f); curveTo(19f, 20f, 21f, 18f, 20f, 16f); lineTo(17f, 14f); lineTo(14f, 16f); curveTo(11f, 14f, 9f, 12f, 8f, 9f); lineTo(10f, 6f); close() }
    val Image get() = icon("Image") { moveTo(4f, 5f); lineTo(20f, 5f); lineTo(20f, 19f); lineTo(4f, 19f); close(); moveTo(7f, 16f); lineTo(11f, 12f); lineTo(14f, 15f); lineTo(16f, 13f); lineTo(20f, 17f); moveTo(8f, 8f); lineTo(8.1f, 8f) }
    val Send get() = icon("Send") { moveTo(3f, 20f); lineTo(21f, 12f); lineTo(3f, 4f); lineTo(7f, 12f); close(); moveTo(7f, 12f); lineTo(21f, 12f) }
    val LocalDrink get() = icon("LocalDrink") { moveTo(8f, 3f); lineTo(16f, 3f); lineTo(15f, 21f); lineTo(9f, 21f); close(); moveTo(8.5f, 8f); lineTo(15.5f, 8f) }
    val FitnessCenter get() = icon("FitnessCenter") { moveTo(4f, 10f); lineTo(4f, 14f); moveTo(7f, 8f); lineTo(7f, 16f); moveTo(7f, 12f); lineTo(17f, 12f); moveTo(17f, 8f); lineTo(17f, 16f); moveTo(20f, 10f); lineTo(20f, 14f) }
    val Bolt get() = icon("Bolt") { moveTo(13f, 2f); lineTo(5f, 14f); lineTo(12f, 14f); lineTo(11f, 22f); lineTo(19f, 10f); lineTo(12f, 10f); close() }
    val Mail get() = icon("Mail") { moveTo(4f, 6f); lineTo(20f, 6f); lineTo(20f, 18f); lineTo(4f, 18f); close(); moveTo(4f, 7f); lineTo(12f, 13f); lineTo(20f, 7f) }
    val Psychology get() = icon("Psychology") { moveTo(9f, 18f); curveTo(5f, 17f, 5f, 11f, 9f, 10f); curveTo(8f, 6f, 13f, 4f, 15f, 7f); curveTo(20f, 7f, 21f, 14f, 17f, 16f); moveTo(12f, 18f); lineTo(12f, 22f); moveTo(15f, 18f); lineTo(15f, 22f); moveTo(10f, 13f); lineTo(14f, 13f); moveTo(17f, 10f); lineTo(19f, 8f) }
}

private fun icon(name: String, block: PathBuilder.() -> Unit): ImageVector {
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathBuilder = block
        )
    }.build()
}
