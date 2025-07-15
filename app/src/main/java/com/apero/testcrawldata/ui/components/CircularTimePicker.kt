package com.apero.testcrawldata.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun CircularTimePicker(
    selectedMinutes: Int,
    onTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxMinutes: Int = 60,
    minMinutes: Int = 1
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .size(280.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val angle = atan2(
                            change.position.y - center.y,
                            change.position.x - center.x
                        )
                        
                        // Convert angle to minutes (0-59)
                        var degrees = Math.toDegrees(angle.toDouble()).toFloat()
                        if (degrees < 0) degrees += 360f
                        
                        // Adjust so 12 o'clock is 0 degrees
                        degrees = (degrees + 90f) % 360f
                        
                        val minutes = ((degrees / 360f) * maxMinutes).toInt()
                        val clampedMinutes = minutes.coerceIn(minMinutes, maxMinutes)
                        
                        onTimeSelected(clampedMinutes)
                    }
                }
        ) {
            drawCircularTimePicker(
                selectedMinutes = selectedMinutes,
                maxMinutes = maxMinutes,
                circleColor = Color.Gray,
                selectedColor = Color.Blue,
                handleColor = Color.White
            )
        }
        
        // Time display in center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = selectedMinutes.toString(),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (selectedMinutes == 1) "phút" else "phút",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

private fun DrawScope.drawCircularTimePicker(
    selectedMinutes: Int,
    maxMinutes: Int,
    circleColor: Color,
    selectedColor: Color,
    handleColor: Color
) {
    val radius = size.minDimension / 2f - 20.dp.toPx()
    val center = size.center
    val strokeWidth = 8.dp.toPx()
    
    // Draw background circle
    drawCircle(
        color = circleColor.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
    )
    
    // Draw minute markers
    for (i in 0 until maxMinutes step 5) {
        val angle = (i.toFloat() / maxMinutes) * 360f - 90f
        val startRadius = radius - 15.dp.toPx()
        val endRadius = radius + 5.dp.toPx()
        
        val startX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * startRadius
        val startY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * startRadius
        val endX = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * endRadius
        val endY = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * endRadius
        
        drawLine(
            color = circleColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 2.dp.toPx()
        )
    }
    
    // Draw selected arc
    val sweepAngle = (selectedMinutes.toFloat() / maxMinutes) * 360f
    drawArc(
        color = selectedColor,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(width = strokeWidth),
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )
    
    // Draw handle
    val handleAngle = (selectedMinutes.toFloat() / maxMinutes) * 360f - 90f
    val handleX = center.x + cos(Math.toRadians(handleAngle.toDouble())).toFloat() * radius
    val handleY = center.y + sin(Math.toRadians(handleAngle.toDouble())).toFloat() * radius
    
    // Handle shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = 12.dp.toPx(),
        center = Offset(handleX + 2.dp.toPx(), handleY + 2.dp.toPx())
    )
    
    // Handle
    drawCircle(
        color = selectedColor,
        radius = 12.dp.toPx(),
        center = Offset(handleX, handleY)
    )
    
    // Handle inner circle
    drawCircle(
        color = handleColor,
        radius = 8.dp.toPx(),
        center = Offset(handleX, handleY)
    )
} 