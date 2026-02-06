package com.zettl.vocabuhero.ui.navigation

object NavRoutes {
    const val Home = "home"
    const val Practice = "practice"
    const val Edit = "edit/{cardId}"
    const val EditNew = "edit/new"
    const val Import = "import"
    const val Stats = "stats"
    const val Settings = "settings"
    const val DeckList = "decks"
    const val DeckDetail = "deck/{deckId}"

    fun edit(cardId: Long?) = if (cardId == null || cardId == 0L) "edit/new" else "edit/$cardId"
    fun deckDetail(deckId: Long) = "deck/$deckId"
}
