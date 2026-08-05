package com.ato.ui_state.base.image

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AvatarPresetsTest {

    @Test
    fun presetUrlSurvivesARoundTrip() {
        repeat(AvatarPresets.COUNT) { index ->
            assertEquals(index, AvatarPresets.indexOf(AvatarPresets.url(index)))
        }
    }

    @Test
    fun storageLinksAreNotPresets() {
        val storageLink =
            "https://firebasestorage.googleapis.com/v0/b/wishliststaging.appspot.com/o/users%2Fabc%2Fava.jpg"

        assertNull(AvatarPresets.indexOf(storageLink))
        assertFalse(AvatarPresets.isPreset(storageLink))
    }

    @Test
    fun malformedPresetLinksAreNotPresets() {
        assertNull(AvatarPresets.indexOf(null))
        assertNull(AvatarPresets.indexOf(""))
        assertNull(AvatarPresets.indexOf("avatar://preset/"))
        assertNull(AvatarPresets.indexOf("avatar://preset/two"))
        assertNull(AvatarPresets.indexOf("avatar://preset/3x"))
        assertNull(AvatarPresets.indexOf("preset/3"))
    }

    @Test
    fun presetNumberFromTheFutureFallsBackInsteadOfBreaking() {
        // Клиент, у которого пресетов меньше, чем у того, кто выбирал: лучше
        // нарисовать запасной, чем пустое место.
        val fromNewerBuild = "avatar://preset/${AvatarPresets.COUNT + 4}"

        assertNull(AvatarPresets.indexOf(fromNewerBuild))
        assertNull(AvatarPresets.indexOf("avatar://preset/-1"))
    }

    @Test
    fun fallbackIsStableForTheSameSeed() {
        // Значение зашито намеренно: если хеш когда-нибудь поменяют, у всех
        // пользователей разом сменится картинка, и тест должен об этом сказать.
        assertEquals(
            AvatarPresets.fallbackIndex("C1j1H9fCPC1j4z3I5nN7"),
            AvatarPresets.fallbackIndex("C1j1H9fCPC1j4z3I5nN7")
        )
        assertEquals(6, AvatarPresets.fallbackIndex("C1j1H9fCPC1j4z3I5nN7"))
    }

    @Test
    fun fallbackAlwaysNamesAPresetThatExists() {
        val seeds = List(200) { "user-$it" } + List(50) { "оченьДлинныйНикнейм$it" }

        seeds.forEach { seed ->
            val index = AvatarPresets.fallbackIndex(seed)
            assertTrue(index in 0 until AvatarPresets.COUNT, "seed $seed gave $index")
        }
    }

    @Test
    fun fallbackSpreadsAcrossAllPresets() {
        // Хеш, который сваливает всех в одну картинку, формально проходит
        // проверку выше и при этом бесполезен.
        val used = List(500) { AvatarPresets.fallbackIndex("uid-$it") }.toSet()

        assertEquals(AvatarPresets.COUNT, used.size)
    }

    @Test
    fun emptySeedDoesNotBlowUp() {
        assertEquals(0, AvatarPresets.fallbackIndex(null))
        assertEquals(0, AvatarPresets.fallbackIndex(""))
    }

    @Test
    fun resolvePrefersTheChosenPresetOverTheSeed() {
        val chosen = AvatarPresets.url(2)

        assertEquals(2, AvatarPresets.resolve(chosen, seed = "uid"))
    }

    @Test
    fun resolveFallsBackWhenThereIsNoAvatarAtAll() {
        assertEquals(AvatarPresets.fallbackIndex("uid"), AvatarPresets.resolve(null, seed = "uid"))
        assertEquals(AvatarPresets.fallbackIndex("uid"), AvatarPresets.resolve("", seed = "uid"))
    }

    @Test
    fun resolveSendsRealLinksToTheImageLoader() {
        assertNull(AvatarPresets.resolve("https://example.com/ava.jpg", seed = "uid"))
    }

    @Test
    fun withoutASeedThereIsNoFallback() {
        // Те же компоненты рисуют картинки желаний. Пустое желание должно
        // остаться пустым, а не получить подарочную коробку.
        assertNull(AvatarPresets.resolve(null, seed = null))
        assertNull(AvatarPresets.resolve("", seed = null))
    }

    @Test
    fun aChosenPresetIsDrawnEvenWithoutASeed() {
        assertEquals(4, AvatarPresets.resolve(AvatarPresets.url(4), seed = null))
    }
}
