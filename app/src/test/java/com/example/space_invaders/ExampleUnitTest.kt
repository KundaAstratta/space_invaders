package com.example.space_invaders

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPlayerHitReducesLives() {
        val player = Player(0f, 0f, 100f)
        val initialLives = player.lives

        player.hit()

        assertEquals(initialLives - 1, player.lives)
    }

    @Test
    fun testPlayerHitReturnsTrueWhenNoLivesLeft() {
        val player = Player(0f, 0f, 100f)
        player.lives = 1

        val isDead = player.hit()

        assertTrue(isDead)
    }

    @Test
    fun testPlayerIntersectsWithCosmicHorror() {
        val player = Player(50f, 50f, 50f)
        val cosmicHorror = CosmicHorror(40f, 40f, 30f, 20f, 1)

        val intersects = player.intersects(cosmicHorror)

        assertTrue(intersects)
    }
    @Test
    fun testCosmicHorrorHitReducesHitsToDestroy() {
        val cosmicHorror = CosmicHorror(0f, 0f, 100f, 50f, 1)
        val initialHits = cosmicHorror.hitsToDestroy

        cosmicHorror.hit()

        assertEquals(initialHits - 1, cosmicHorror.hitsToDestroy)
    }

    @Test
    fun testCosmicHorrorHitReturnsTrueWhenDestroyed() {
        val cosmicHorror = CosmicHorror(0f, 0f, 100f, 50f, 1)
        cosmicHorror.hitsToDestroy = 1

        val isDestroyed = cosmicHorror.hit()

        assertTrue(isDestroyed)
    }

    @Test
    fun testCosmicHorrorMoveStaysOnScreen() {
        val cosmicHorror = CosmicHorror(20f, 20f, 100f, 50f, 1)
        val screenWidth = 500f
        val screenHeight = 800f

        cosmicHorror.move(screenWidth, screenHeight, 1f)

        assertTrue(cosmicHorror.x in 0f..screenWidth)
        assertTrue(cosmicHorror.y in 0f..screenHeight)
    }

    @Test
    fun testBulletMoveReducesYCoordinate() {
        val bullet = Bullet(0f, 100f)
        val initialY = bullet.y

        bullet.move()

        assertTrue(bullet.y < initialY)
    }

    @Test
    fun testBulletIntersectsWithCosmicHorror() {
        val bullet = Bullet(50f, 50f)
        val cosmicHorror = CosmicHorror(40f, 40f, 30f, 20f, 1)

        val intersects = bullet.intersects(cosmicHorror)

        assertTrue(intersects)
    }


}