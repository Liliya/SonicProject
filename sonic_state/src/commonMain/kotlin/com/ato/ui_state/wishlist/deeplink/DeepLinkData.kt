package com.ato.ui_state.wishlist.deeplink

import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class DeepLinkData(
    val pathSegments: List<String>,
    val deeplinkParams: DeeplinkParams
) {
    companion object {
        const val APP: String = "app"
        const val HOST: String = "yalito.me"
        const val SCHEME: String = "wishlist"
        const val HTTPS: String = "https"
        const val FRIENDS: String = "friends"
        const val PERSON: String = "person"
        const val BOARD: String = "board"
        const val WISH: String = "wish"
        const val WISHES: String = "wishes"
        const val ADD_WISH: String = "add_wish"
        const val SETTINGS: String = "settings"
        const val GIFTING: String = "gifting"

        // Вкладка «Дарю» раньше называлась событиями, и ссылки с сегментом
        // `events` уже разошлись по рукам — они всё ещё должны открывать её.
        const val LEGACY_GIFTING: String = "events"

        /**
         * Схема внутренних ссылок — тех, которыми приложение зовёт само себя:
         * переход по уведомлению, [DeeplinkExecutor.openDeeplink].
         *
         * Задаётся сборкой, потому что staging стоит на телефоне рядом с
         * боевым приложением. Пока обе сборки объявляли одну схему, система не
         * могла выбрать между ними: нажатие на уведомление из staging с равным
         * успехом открывало боевое приложение, и наоборот. Схема должна
         * совпадать с той, что объявлена в манифесте, — на Android их задаёт
         * один и тот же тип сборки.
         *
         * По умолчанию — боевая: тот, кто ничего не настраивал (iOS, desktop),
         * работает как работал.
         *
         * Касается только исходящих ссылок, [fromUrl] читает и ту и другую
         * схему.
         */
        var innerScheme: String = SCHEME

        /**
         * Отдаёт ссылку своей схемой, а принимает любую нашу.
         *
         * Строгость нужна только на выходе: там ссылку разбирает система и по
         * ней же выбирает, какому из двух установленных приложений её отдать.
         * На входе выбирать не из чего — ссылка уже пришла сюда, — а прийти
         * она может с канонической схемой: её присылает сервер в пуше
         * (`FRIENDS_DEEPLINK` в functions/notify.mjs), одинаковый для боевого
         * проекта и для staging.
         */
        fun fromUrl(urlString: String): DeepLinkData? {
            return if (
                urlString.startsWith("$HTTPS://$HOST/$APP/") ||
                urlString.startsWith("$SCHEME://$HOST/$APP/") ||
                urlString.startsWith("$innerScheme://$HOST/$APP/")
            ) {
                val url = Url(urlString)
                val pathSegments = url.pathSegments.filter { it.isNotEmpty() }

                DeepLinkData(
                    pathSegments = pathSegments,
                    deeplinkParams = DeeplinkParams.fromUrl(url)
                ).getTail()
            } else {
                // Если это не наша схема, то юзер хочет добавить наше желание по ссылке
                DeeplinkCreator.addWishDeeplink
            }
        }

        fun getAddWishWithDescription(sharedUris: List<String>): DeepLinkData? {
            val addWish = DeeplinkCreator.addWishDeeplink
            return addWish.copy(
                deeplinkParams = addWish.deeplinkParams.copy(
                    // Стояло `joinToString { "/n" }`: каждая ссылка заменялась
                    // на литерал «/n», и в описание попадало «/n, /n» вместо
                    // самих ссылок.
                    wishDescription = sharedUris.joinToString(separator = "\n")
                )
            )
        }

        fun getAddWishWithDescription(sharedDescription: String): DeepLinkData? {
            val addWish = DeeplinkCreator.addWishDeeplink
            return addWish.copy(
                deeplinkParams = addWish.deeplinkParams.copy(
                    wishDescription = sharedDescription
                )
            )
        }

        fun getAddWishWithName(name: String): DeepLinkData? {
            val addWish = DeeplinkCreator.addWishDeeplink
            return addWish.copy(
                deeplinkParams = addWish.deeplinkParams.copy(
                    wishName = name
                )
            )
        }

        fun getAddWishWithLink(url: String): DeepLinkData? {
            val addWish = DeeplinkCreator.addWishDeeplink
            return addWish.copy(
                deeplinkParams = addWish.deeplinkParams.copy(
                    wishUrl = url
                )
            )
        }

        // Придумать решение поизящнее
        private var callback: (() -> Unit)? = null

        fun setOnDestinationReached(onReached: ()->Unit){
            callback = onReached
        }

        fun onDestinationReached() {
            callback?.invoke()
        }
    }

    fun getHead(): String? {
        return if (pathSegments.isNotEmpty()) pathSegments.first() else null
    }

    fun getTail(): DeepLinkData? {
        return if (pathSegments.isNotEmpty()) {
            DeepLinkData(
                pathSegments = pathSegments.drop(1),
                deeplinkParams = deeplinkParams
            )
        } else {
            null
        }
    }

    fun toInnerDeeplink(): String {
        val path = pathSegments.joinToString(separator = "/")
        val query = deeplinkParams.toQueryString()

        return "$innerScheme://$HOST/$APP/$path?$query"
    }

    fun toWebDeeplink(): String {
        val path = pathSegments.joinToString(separator = "/")
        val query = deeplinkParams.toQueryString()

        return "$HTTPS://$HOST/$APP/$path?$query"
    }
}

interface DeeplinkExecutor {
    fun openDeeplink(deepLinkData: DeepLinkData)
}
