package com.example.space_invaders

import com.example.space_invaders.entities.Bullet
import com.example.space_invaders.entities.Player
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
    fun testBulletMoveReducesYCoordinate() {
        val bullet = Bullet(0f, 100f)
        val initialY = bullet.y

        bullet.move()

        assertTrue(bullet.y < initialY)
    }

}