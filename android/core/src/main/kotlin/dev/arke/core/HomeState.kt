package dev.arke.core

enum class TravelContext { PLANNING, EXPENSES, SAVINGS, REVIEWS, MEMORIES }
enum class PanelDetent { EXPLORE, BALANCED, DETAILS }

/** Transient home navigation. This is not persisted travel data. */
data class HomeState(
    val context: TravelContext = TravelContext.PLANNING,
    val detent: PanelDetent = PanelDetent.BALANCED,
) {
    fun selectContext(context: TravelContext): HomeState = copy(context = context)
    fun setDetent(detent: PanelDetent): HomeState = copy(detent = detent)
}
