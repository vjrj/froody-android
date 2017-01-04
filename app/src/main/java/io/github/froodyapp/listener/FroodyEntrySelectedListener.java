package io.github.froodyapp.listener;

import io.github.froodyapp.model.FroodyEntryPlus;

/**
 * Item selected listener
 */
public interface FroodyEntrySelectedListener {
    /**
     * A froody item was selected
     *
     * @param entry
     */
    void onFroodyEntrySelected(FroodyEntryPlus entry);
}
