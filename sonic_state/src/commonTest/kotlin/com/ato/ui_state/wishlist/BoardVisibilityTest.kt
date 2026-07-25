package com.ato.ui_state.wishlist

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardVisibilityTest {

    private val owner = "owner-id"
    private val viewer = "viewer-id"

    private fun board(
        availableForAll: Boolean? = null,
        availableForFollowing: Boolean? = null,
        availableForUserIds: List<String>? = null
    ) = WishlistBoard(
        userDocumentId = owner,
        availableForAll = availableForAll,
        availableForFollowing = availableForFollowing,
        availableForUserIds = availableForUserIds
    )

    @Test
    fun ownerAlwaysSeesTheirOwnBoard() {
        val privateBoard = board(availableForAll = false)

        assertTrue(privateBoard.isVisibleTo(viewerId = owner, viewerFollowsOwner = false))
    }

    @Test
    fun publicBoardIsVisibleToAnyone() {
        assertTrue(board(availableForAll = true).isVisibleTo(viewer, viewerFollowsOwner = false))
    }

    @Test
    fun followersOnlyBoardIsVisibleToAFollower() {
        val followersOnly = board(availableForFollowing = true)

        assertTrue(followersOnly.isVisibleTo(viewer, viewerFollowsOwner = true))
    }

    @Test
    fun followersOnlyBoardIsHiddenFromNonFollowers() {
        val followersOnly = board(availableForFollowing = true)

        assertFalse(followersOnly.isVisibleTo(viewer, viewerFollowsOwner = false))
    }

    @Test
    fun listedBoardIsVisibleOnlyToPeopleOnTheList() {
        val listed = board(availableForUserIds = listOf("someone-else", viewer))
        val listedWithoutViewer = board(availableForUserIds = listOf("someone-else"))

        assertTrue(listed.isVisibleTo(viewer, viewerFollowsOwner = false))
        assertFalse(listedWithoutViewer.isVisibleTo(viewer, viewerFollowsOwner = false))
    }

    @Test
    fun boardWithNoSettingsIsPrivate() {
        // Documents predating the availability fields: hiding is the safe default.
        assertFalse(board().isVisibleTo(viewer, viewerFollowsOwner = true))
    }

    @Test
    fun signedOutViewerOnlySeesPublicBoards() {
        assertTrue(board(availableForAll = true).isVisibleTo(viewerId = null, viewerFollowsOwner = false))
        assertFalse(board(availableForUserIds = listOf(viewer)).isVisibleTo(viewerId = null, viewerFollowsOwner = false))
    }
}
