package com.pedram.net;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.PriorityBlockingQueue;

public class SelectorPool {
    private final PriorityBlockingQueue<SelectorWithChannelCount> selectors;

    private final HashMap<Selector, SelectorWithChannelCount> selectorCorresponder;

    public SelectorPool(int numberOfSelectors) throws IOException {
        this.selectors = new PriorityBlockingQueue<>();
        this.selectorCorresponder = new HashMap<>();
        for (int i = 0; i < numberOfSelectors; i++) {
            SelectorWithChannelCount selectorWithCount = new SelectorWithChannelCount(Selector.open());
            selectors.add(selectorWithCount);
            selectorCorresponder.put(selectorWithCount.getSelector(), selectorWithCount);
        }


    }

    public Selector getNextSelector() {
        return this.selectors.peek().getSelector();
    }

    public SelectorWithChannelCount[] getSelectors() throws ClassCastException {
        return this.selectors.toArray(new SelectorWithChannelCount[0]);
    }

    public void updateSelectorState(Selector selector) throws Exception {
        SelectorWithChannelCount selectorWithCount = selectorCorresponder.get(selector);
        if (selectors.remove(selectorWithCount))
            selectors.add(selectorWithCount);
        else
            throw new Exception("Selector not Found");
    }

    static public class SelectorWithChannelCount implements Comparable<SelectorWithChannelCount> {
        private final Selector selector;


        public SelectorWithChannelCount(Selector selector) throws NullPointerException {
            if (selector == null)
                throw new NullPointerException("The Selector passed to the constructor is null");
            this.selector = selector;
        }


        @Override
        public int compareTo(SelectorWithChannelCount other) {
            return Integer.compare(this.selector.keys().size(), other.selector.keys().size());
        }

        @Override
        public synchronized boolean equals(Object o) {
            if (o instanceof SelectorWithChannelCount) {
                return ((SelectorWithChannelCount) o).selector == selector;
            } else if (o instanceof Selector) {
                return this.selector == o;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(selector);
        }

        public @NotNull Selector getSelector() {
            return selector;
        }
    }
}
