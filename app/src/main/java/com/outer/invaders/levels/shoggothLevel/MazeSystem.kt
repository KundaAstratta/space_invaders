package com.outer.invaders.levels.shoggothLevel

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import kotlin.random.Random

class MazeSystem(
    private val screenWidth: Float,
    private val screenHeight: Float,
    private val cellSize: Float = 140f // Taille d'une cellule du labyrinthe
) {
    // Représente un mur du labyrinthe
    data class Wall(val rect: RectF) {
        fun intersects(x: Float, y: Float, radius: Float): Boolean {
            val closestX = x.coerceIn(rect.left, rect.right)
            val closestY = y.coerceIn(rect.top, rect.bottom)
            val distanceX = x - closestX
            val distanceY = y - closestY
            return (distanceX * distanceX + distanceY * distanceY) <= radius * radius
        }
    }

    private val walls = mutableListOf<Wall>()
    private val corridors = mutableListOf<RectF>()
    private var cols: Int = (screenWidth / cellSize).toInt()
    private var rows: Int = (screenHeight / cellSize).toInt()
    private val grid = Array(rows) { BooleanArray(cols) { false } } // false = mur, true = corridor

    // Calcul du décalage pour centrer le labyrinthe
    private val mazeWidth = cols * cellSize
    private val mazeHeight = rows * cellSize
    private val offsetX = (screenWidth - mazeWidth) / 2
    private val offsetY = (screenHeight - mazeHeight) / 2

    init {
        generateMaze()
    }

    private fun generateMaze() {
        walls.clear()
        corridors.clear()

        // Initialiser la grille avec des murs partout
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                grid[i][j] = false
            }
        }

        // Générer le labyrinthe avec l'algorithme de Prim
        val start = Pair(1, 1)
        grid[start.first][start.second] = true
        val frontiers = mutableListOf<Pair<Int, Int>>()
        addFrontiers(start, frontiers)

        while (frontiers.isNotEmpty()) {
            val current = frontiers.removeAt(Random.nextInt(frontiers.size))
            val neighbors = getNeighbors(current.first, current.second)
                .filter { grid[it.first][it.second] }

            if (neighbors.isNotEmpty()) {
                val neighbor = neighbors.random()
                grid[current.first][current.second] = true
                grid[(current.first + neighbor.first) / 2][(current.second + neighbor.second) / 2] = true
                addFrontiers(current, frontiers)
            }
        }

        // Créer les murs physiques avec le décalage pour centrer
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (!grid[i][j]) {
                    walls.add(
                        Wall(
                            RectF(
                                j * cellSize + offsetX,
                                i * cellSize + offsetY,
                                (j + 1) * cellSize + offsetX,
                                (i + 1) * cellSize + offsetY
                            )
                        )
                    )
                } else {
                    corridors.add(
                        RectF(
                            j * cellSize + offsetX,
                            i * cellSize + offsetY,
                            (j + 1) * cellSize + offsetX,
                            (i + 1) * cellSize + offsetY
                        )
                    )
                }
            }
        }
    }



    private fun addFrontiers(cell: Pair<Int, Int>, frontiers: MutableList<Pair<Int, Int>>) {
        val directions = listOf(-2 to 0, 2 to 0, 0 to -2, 0 to 2)
        for ((dx, dy) in directions) {
            val newRow = cell.first + dx
            val newCol = cell.second + dy
            if (newRow in 1 until rows - 1 && newCol in 1 until cols - 1 && !grid[newRow][newCol]) {
                frontiers.add(Pair(newRow, newCol))
            }
        }
    }

    private fun getNeighbors(row: Int, col: Int): List<Pair<Int, Int>> {
        val neighbors = mutableListOf<Pair<Int, Int>>()
        val directions = listOf(-2 to 0, 2 to 0, 0 to -2, 0 to 2)

        for ((dx, dy) in directions) {
            val newRow = row + dx
            val newCol = col + dy
            if (newRow in 0 until rows && newCol in 0 until cols) {
                neighbors.add(Pair(newRow, newCol))
            }
        }
        return neighbors
    }

    fun getRandomCorridorPosition(): Pair<Float, Float> {
        val corridor = corridors.random()
        return Pair(
            corridor.left + (corridor.right - corridor.left) / 2,
            corridor.top + (corridor.bottom - corridor.top) / 2
        )
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        walls.forEach { wall ->
            canvas.drawRect(wall.rect, paint)
        }

    }


    fun checkCollision(x: Float, y: Float, radius: Float): Boolean {
        return walls.any { it.intersects(x, y, radius) }
    }

    fun checkBulletCollision(x: Float, y: Float): Boolean {
        return walls.any { wall ->
            x >= wall.rect.left && x <= wall.rect.right &&
                    y >= wall.rect.top && y <= wall.rect.bottom
        }
    }

    fun getValidMovement(
        currentX: Float,
        currentY: Float,
        targetX: Float,
        targetY: Float,
        radius: Float
    ): Pair<Float, Float> {
        // Si pas de collision, autoriser le mouvement
        if (!checkCollision(targetX, targetY, radius)) {
            return Pair(targetX, targetY)
        }

        // Essayer de se déplacer horizontalement
        if (!checkCollision(targetX, currentY, radius)) {
            return Pair(targetX, currentY)
        }

        // Essayer de se déplacer verticalement
        if (!checkCollision(currentX, targetY, radius)) {
            return Pair(currentX, targetY)
        }

        // Si aucun mouvement n'est possible, rester sur place
        return Pair(currentX, currentY)
    }
}