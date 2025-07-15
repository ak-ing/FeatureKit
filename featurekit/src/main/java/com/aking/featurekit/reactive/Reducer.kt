package com.aking.featurekit.reactive

/**
 * Interface for an intent that can be dispatched to a reducer.
 * @author Created by Ak on 2024-12-19 23:04.
 */
interface Intent

/**
 * Interface for a reducer that takes an intent and updates the state accordingly.
 */
interface Reducer<I : Intent> {
    /**
     * Reducer function that takes an intent and updates the state accordingly.
     */
    fun reducer(intent: I)
}