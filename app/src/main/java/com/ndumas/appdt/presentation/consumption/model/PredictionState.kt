package com.ndumas.appdt.presentation.consumption.model

enum class PredictionState {
    POSITIVE, // Verde (Consumo inferiore alla stima)
    NEGATIVE, // Rosso (Consumo superiore alla stima)
    NEUTRAL, // Grigio (Nessuna stima o consumo zero)
}
