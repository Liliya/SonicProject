package com.ato.helpers

import com.ato.helpers.links.LinkRefusal
import com.ato.helpers.links.LinkSafety
import com.ato.helpers.links.LinkWarning
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Проверки для [LinkSafety].
 *
 * Половина файла — про строки, которые никто не набирает руками: `javascript:`,
 * `ozon.ru@evil.com`, кириллическая «о» в латинском слове. Это и есть то, ради
 * чего разбор написан, и единственное место, где такие строки можно прогнать
 * через него, не заводя желание с подделкой в базе.
 */
class LinkSafetyTest {

    // ---- то, что должно открываться ---------------------------------------

    @Test
    fun keepsAnOrdinaryHttpsLink() {
        val link = LinkSafety.check("https://ozon.ru/product/123?from=search")

        assertEquals("https://ozon.ru/product/123?from=search", link.url)
        assertEquals("ozon.ru", link.host)
        assertEquals("/product/123?from=search", link.tail)
        assertTrue(link.warnings.isEmpty())
    }

    @Test
    fun addsHttpsWhenTheSchemeIsMissing() {
        val link = LinkSafety.check("ozon.ru/product/123")

        assertEquals("https://ozon.ru/product/123", link.url)
        assertEquals("ozon.ru", link.host)
    }

    @Test
    fun readsAPortAsAPortAndNotAsAScheme() {
        val link = LinkSafety.check("shop.example.com:8080/item")

        assertEquals("https://shop.example.com:8080/item", link.url)
        assertEquals("shop.example.com", link.host)
    }

    @Test
    fun lowercasesTheSchemeSoStoredLinksHaveOneShape() {
        assertEquals("https://ozon.ru/x", LinkSafety.check("HTTPS://ozon.ru/x").url)
        assertEquals("https://ozon.ru", LinkSafety.check("https:/ozon.ru").url)
    }

    @Test
    fun dropsWwwFromWhatIsShownButNotFromWhatIsOpened() {
        val link = LinkSafety.check("https://www.ozon.ru/")

        assertEquals("ozon.ru", link.host)
        assertEquals("https://www.ozon.ru/", link.url)
    }

    @Test
    fun aLinkToTheFrontPageHasNoTail() {
        assertNull(LinkSafety.check("https://ozon.ru").tail)
        assertNull(LinkSafety.check("https://ozon.ru/").tail)
    }

    @Test
    fun cyrillicDomainsAreOrdinaryDomains() {
        val link = LinkSafety.check("https://подарки.рф/каталог")

        assertTrue(link.isOpenable)
        assertFalse(link.asksFirst, "целиком кириллическое имя — обычный домен, а не подделка")
    }

    // ---- то, что открываться не должно ------------------------------------

    @Test
    fun refusesSchemesThatDoNotOpenAPage() {
        val refused = listOf(
            "javascript:alert(document.cookie)",
            "data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==",
            "file:///etc/passwd",
            "intent://scan/#Intent;scheme=zxing;package=com.google.zxing;end",
            "content://com.android.contacts/contacts",
            "market://details?id=com.example",
            "tel:+79001234567",
        )

        refused.forEach { raw ->
            val link = LinkSafety.check(raw)
            assertNull(link.url, "$raw не должна открываться")
            assertEquals(LinkRefusal.ForeignScheme, link.refusal, raw)
        }
    }

    @Test
    fun caseDoesNotSmuggleAScheme() {
        assertEquals(LinkRefusal.ForeignScheme, LinkSafety.check("JavaScript:alert(1)").refusal)
    }

    @Test
    fun refusesWhatIsNotAnAddressAtAll() {
        listOf("", "   ", "спросить у мамы", "озон", "ozon ru", "??").forEach { raw ->
            assertEquals(LinkRefusal.NotAnAddress, LinkSafety.check(raw).refusal, "«$raw»")
        }
    }

    @Test
    fun refusesSomethingLongerThanALink() {
        val long = "https://ozon.ru/" + "a".repeat(LinkSafety.MAX_LINK_LENGTH)

        assertEquals(LinkRefusal.TooLong, LinkSafety.check(long).refusal)
    }

    @Test
    fun whatCannotBeOpenedIsStillShownAsTyped() {
        // Стереть набранное — это потерять то, что человек имел в виду. Пусть
        // висит текстом, но не ссылкой.
        assertEquals("спросить у мамы", LinkSafety.check("спросить у мамы").host)
    }

    // ---- то, что открывается только с вопросом -----------------------------

    @Test
    fun theRealHostIsWhatComesAfterTheAt() {
        val link = LinkSafety.check("https://ozon.ru@evil.example/deal")

        assertEquals("evil.example", link.host)
        assertContains(link.warnings, LinkWarning.HiddenPrefix)
        assertTrue(link.asksFirst)
        // Открывается всё же исходный адрес: переписать его — значит открыть
        // не то, на что нажали.
        assertEquals("https://ozon.ru@evil.example/deal", link.url)
    }

    @Test
    fun mixedAlphabetsInOneWordAreASpoof() {
        // Первая «о» — кириллическая.
        val link = LinkSafety.check("https://оzon.ru")

        assertContains(link.warnings, LinkWarning.DisguisedName)
        assertTrue(link.asksFirst)
    }

    @Test
    fun punycodeAndBareAddressesAskFirst() {
        assertContains(
            LinkSafety.check("https://xn--80ak6aa92e.com").warnings,
            LinkWarning.DisguisedName,
        )
        assertContains(
            LinkSafety.check("http://185.199.108.153/deal").warnings,
            LinkWarning.DisguisedName,
        )
    }

    @Test
    fun httpIsMarkedButDoesNotAskFirst() {
        val link = LinkSafety.check("http://oldshop.ru/item")

        assertContains(link.warnings, LinkWarning.NotEncrypted)
        assertFalse(link.asksFirst, "незашифрованных магазинов много, вопрос на каждый перестают читать")
    }

    // ---- невидимое ---------------------------------------------------------

    @Test
    fun stripsCharactersThatChangeHowTheAddressReads() {
        // U+202E разворачивает показ строки, U+200B рвёт имя там, где глаз
        // склеивает: и то и другое — способ показать одно, а открыть другое.
        val link = LinkSafety.check("https://oz​on‮.ru/x")

        assertEquals("ozon.ru", link.host)
        assertEquals("https://ozon.ru/x", link.url)
    }

    @Test
    fun stripsNewlinesAndTabsInsideTheAddress() {
        assertEquals("https://ozon.ru/x", LinkSafety.check(" https://ozon\t.ru\n/x ").url)
    }

    // ---- то, что уходит в базу ---------------------------------------------

    @Test
    fun normalizeForStorageKeepsOnlyWhatCanBeOpened() {
        assertEquals("https://ozon.ru/x", LinkSafety.normalizeForStorage("ozon.ru/x"))
        assertNull(LinkSafety.normalizeForStorage("javascript:alert(1)"))
        assertNull(LinkSafety.normalizeForStorage("спросить у мамы"))
    }

    @Test
    fun everythingStoredStartsWithASchemeTheRulesAllow() {
        // Тот же инвариант проверяют `firestore.rules` и `functions/links.mjs`:
        // если он держится здесь, там нечего вычищать.
        listOf("ozon.ru", "HTTP://shop.ru:8080/a?b#c", "https://ozon.ru@evil.example")
            .mapNotNull(LinkSafety::normalizeForStorage)
            .forEach { stored ->
                assertTrue(
                    stored.startsWith("http://") || stored.startsWith("https://"),
                    stored,
                )
            }
    }
}
