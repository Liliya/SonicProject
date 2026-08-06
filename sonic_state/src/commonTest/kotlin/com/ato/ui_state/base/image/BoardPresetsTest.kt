package com.ato.ui_state.base.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardPresetsTest {

    @Test
    fun presetValueSurvivesARoundTrip() {
        repeat(BoardPresets.COUNT) { index ->
            assertEquals(index, BoardPresets.indexOf(BoardPresets.value(index)))
        }
    }

    @Test
    fun anEmojiIsNotAPreset() {
        // Поле доски раньше хранило эмодзи и продолжает встречаться со старым
        // значением. Оно не должно читаться как выбор пресета.
        assertNull(BoardPresets.indexOf("❤️"))
        assertFalse(BoardPresets.isPreset("🎁"))
    }

    @Test
    fun malformedPresetValuesAreNotPresets() {
        assertNull(BoardPresets.indexOf(null))
        assertNull(BoardPresets.indexOf(""))
        assertNull(BoardPresets.indexOf("board://preset/"))
        assertNull(BoardPresets.indexOf("board://preset/two"))
        assertNull(BoardPresets.indexOf("board://preset/3x"))
        assertNull(BoardPresets.indexOf("preset/3"))
        // Чужая схема: аватарки нумеруются отдельно.
        assertNull(BoardPresets.indexOf("avatar://preset/3"))
    }

    @Test
    fun presetNumberFromTheFutureFallsBackInsteadOfBreaking() {
        val fromNewerClient = "board://preset/${BoardPresets.COUNT + 5}"

        assertNull(BoardPresets.indexOf(fromNewerClient))
        assertEquals(
            BoardPresets.fallbackIndex("board-1"),
            BoardPresets.resolve(fromNewerClient, "board-1")
        )
    }

    @Test
    fun anUploadedPictureIsNotDrawnAsAPreset() {
        // Своя картинка доски лежит в том же поле. Если бы её приняли за
        // «не пресет», доска нарисовала бы запасной предмет поверх
        // загруженной фотографии.
        val url = "https://firebasestorage.googleapis.com/v0/b/x/o/boards%2F1%2Fpicture.jpg"

        assertTrue(BoardPresets.isUploaded(url))
        assertNull(BoardPresets.resolve(url, seed = "board-1"))
    }

    @Test
    fun anEmojiIsNotAnUploadedPicture() {
        // Иначе старое эмодзи уехало бы в загрузчик картинок как адрес.
        assertFalse(BoardPresets.isUploaded("🎁"))
        assertFalse(BoardPresets.isUploaded(null))
        assertFalse(BoardPresets.isUploaded("board://preset/3"))

        assertEquals(
            BoardPresets.fallbackIndex("board-1"),
            BoardPresets.resolve("🎁", seed = "board-1")
        )
    }

    @Test
    fun theFallbackIsStableForOneBoard() {
        val first = BoardPresets.fallbackIndex("aY7kQ2")
        repeat(5) {
            assertEquals(first, BoardPresets.fallbackIndex("aY7kQ2"))
        }
    }

    @Test
    fun theFallbackStaysInsideTheRange() {
        val seeds = listOf("", "a", "board", "aY7kQ2", "и русские буквы", "0123456789abcdef")
        seeds.forEach { seed ->
            val index = BoardPresets.fallbackIndex(seed)
            assertTrue(index in 0 until BoardPresets.COUNT, "seed=$seed gave $index")
        }
    }

    @Test
    fun differentBoardsDoNotAllGetTheSamePicture() {
        // Не про равномерность, а про то, что сид вообще участвует: если бы
        // хеш игнорировал вход, весь список досок был бы одной картинкой.
        val indices = (1..40).map { BoardPresets.fallbackIndex("board-$it") }.toSet()
        assertTrue(indices.size > 1, "все доски получили один пресет: $indices")
    }

    @Test
    fun anExplicitChoiceWinsOverTheFallback() {
        val chosen = BoardPresets.value(3)
        assertEquals(3, BoardPresets.resolve(chosen, seed = "board-1"))
    }
}
