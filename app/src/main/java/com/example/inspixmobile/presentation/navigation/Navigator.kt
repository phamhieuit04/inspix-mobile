package com.example.inspixmobile.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.example.inspixmobile.presentation.state.NavigationState

class Navigator(val state: NavigationState) {
    fun switchTab(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        }
    }

    fun push(route: NavKey) {
        state.backStacks[state.topLevelRoute]?.add(route)
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }

    fun replaceAll(route: NavKey) {
        state.topLevelRoute = route

        state.backStacks.forEach { (topLevel, stack) ->
            stack.clear()
            stack.add(topLevel)
        }
    }
}
