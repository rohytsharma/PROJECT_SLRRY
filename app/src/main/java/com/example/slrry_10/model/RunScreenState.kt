package com.example.slrry_10.model

enum class RunScreenState {
    READY_TO_START,      // Screen 1: Initial state with start button
    RUNNING,             // Screen 2: Running with pause button
    PAUSED_WITH_OVERLAY, // Screen 3: Paused with distance overlay (map hidden)
    PAUSED_WITH_MAP,     // Screen 4: Paused with map visible
    SUMMARY,             // Screen 5: Run summary with details
    MAPS                 // Screen 6: World / Personal / Friends maps
}

