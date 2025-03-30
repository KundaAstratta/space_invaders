package com.example.space_invaders.levels.elderthingLevel

import android.graphics.*
import com.example.space_invaders.backgrounds.Background.Structure

class ResearchStation(
    centerX: Float,
    centerY: Float,
    val stationSize: Float
) : Structure(centerX, centerY, stationSize) {
    private val buildingPaint = Paint().apply {
        color = Color.rgb(180, 180, 190)
        style = Paint.Style.FILL
    }
    private val windowPaint = Paint().apply {
        color = Color.rgb(100, 150, 200)
        style = Paint.Style.FILL
    }
    private val detailPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }
    private val buildingWidth = stationSize * 1.5f
    private val buildingHeight = stationSize
    private val roofHeight = stationSize * 0.3f

    override fun draw(canvas: Canvas) {
        val left = centerX - buildingWidth / 2
        val top = centerY - buildingHeight / 2
        val right = centerX + buildingWidth / 2
        val bottom = centerY + buildingHeight / 2
        canvas.drawRect(left, top, right, bottom, buildingPaint)
        canvas.drawRect(left, top, right, bottom, detailPaint)
        val roofPath = Path()
        roofPath.moveTo(left - stationSize * 0.1f, top)
        roofPath.lineTo(right + stationSize * 0.1f, top)
        roofPath.lineTo(right, top - roofHeight)
        roofPath.lineTo(left, top - roofHeight)
        roofPath.close()
        canvas.drawPath(roofPath, buildingPaint)
        canvas.drawPath(roofPath, detailPaint)
        val windowSize = stationSize * 0.2f
        val windowRows = 2
        val windowCols = 3
        val windowSpacingX = buildingWidth / (windowCols + 1)
        val windowSpacingY = buildingHeight / (windowRows + 1)
        for (row in 1..windowRows) {
            for (col in 1..windowCols) {
                val windowLeft = left + col * windowSpacingX - windowSize / 2
                val windowTop = top + row * windowSpacingY - windowSize / 2
                canvas.drawRect(
                    windowLeft,
                    windowTop,
                    windowLeft + windowSize,
                    windowTop + windowSize,
                    windowPaint
                )
                canvas.drawRect(
                    windowLeft,
                    windowTop,
                    windowLeft + windowSize,
                    windowTop + windowSize,
                    detailPaint
                )
            }
        }
        val doorWidth = stationSize * 0.3f
        val doorHeight = stationSize * 0.5f
        val doorLeft = centerX - doorWidth / 2
        val doorTop = bottom - doorHeight
        canvas.drawRect(doorLeft, doorTop, doorLeft + doorWidth, bottom, detailPaint)
        val antennaBaseX = centerX
        val antennaBaseY = top - roofHeight
        val antennaHeight = stationSize * 0.6f
        canvas.drawLine(
            antennaBaseX,
            antennaBaseY,
            antennaBaseX,
            antennaBaseY - antennaHeight,
            detailPaint
        )
        canvas.drawLine(
            antennaBaseX,
            antennaBaseY - antennaHeight * 0.3f,
            antennaBaseX + stationSize * 0.2f,
            antennaBaseY - antennaHeight * 0.5f,
            detailPaint
        )
        canvas.drawLine(
            antennaBaseX,
            antennaBaseY - antennaHeight * 0.6f,
            antennaBaseX - stationSize * 0.2f,
            antennaBaseY - antennaHeight * 0.8f,
            detailPaint
        )
    }
}