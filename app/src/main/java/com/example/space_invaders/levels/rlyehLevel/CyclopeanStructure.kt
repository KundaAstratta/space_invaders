package com.example.space_invaders.levels.rlyehLevel

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.space_invaders.backgrounds.Background
import com.example.space_invaders.entities.deepone.DeepOne
import kotlin.random.Random

class CyclopeanStructure(
    centerX: Float,
    centerY: Float,
    val size: Float
) : Background.Structure(centerX, centerY, size / 2) {
    private val path = Path()
    private val color = Color.argb(150, Random.nextInt(100, 200), Random.nextInt(100, 200), Random.nextInt(100, 200))
    private val paint = Paint()

    init {
        path.moveTo(centerX, centerY - size / 2)
        val numPoints = Random.nextInt(5, 8)
        for (i in 1..numPoints) {
            val angle = i * (2 * Math.PI / numPoints)
            val radius = size / 2 * (0.5f + Random.nextFloat() * 0.5f)
            val px = centerX + (radius * Math.cos(angle)).toFloat()
            val py = centerY + (radius * Math.sin(angle)).toFloat()
            path.lineTo(px, py)
        }
        path.close()
    }

    override fun draw(canvas: Canvas) {
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)

        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawPath(path, paint)
    }

    fun intersectsDeepOneVsStruct(entity: DeepOne): Boolean {
        val entityRect = RectF(entity.x, entity.y, entity.x + entity.width, entity.y + entity.height)
        val structureBounds = RectF()
        path.computeBounds(structureBounds, true)
        return RectF.intersects(entityRect, structureBounds)
    }
}