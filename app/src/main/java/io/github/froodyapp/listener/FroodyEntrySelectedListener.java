package io.github.froodyapp.listener;

import io.github.froodyapp.model.FroodyEntryPlus;

/**
 * FroodyEntry selected listener
 */
public interface FroodyEntrySelectedListener {
    /**
     * A froody entry was selected
     *
     * @param entry The entry
     */
    void onFroodyEntrySelected(FroodyEntryPlus entry);
}
