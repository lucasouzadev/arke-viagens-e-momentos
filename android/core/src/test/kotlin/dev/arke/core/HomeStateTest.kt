package dev.arke.core

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeStateTest {
    @Test fun `home begins in planning with balanced panel`() {
        assertEquals(HomeState(TravelContext.PLANNING, PanelDetent.BALANCED), HomeState())
    }

    @Test fun `all contexts preserve the panel position`() {
        for (detent in PanelDetent.entries) {
            val initial = HomeState(detent = detent)
            for (context in TravelContext.entries) {
                assertEquals(HomeState(context, detent), initial.selectContext(context))
            }
        }
    }

    @Test fun `all panel positions preserve the active context`() {
        for (context in TravelContext.entries) {
            val initial = HomeState(context = context)
            for (detent in PanelDetent.entries) {
                assertEquals(HomeState(context, detent), initial.setDetent(detent))
            }
        }
    }
}
