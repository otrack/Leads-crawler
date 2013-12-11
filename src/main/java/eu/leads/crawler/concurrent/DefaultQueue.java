package eu.leads.crawler.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Default queue implementation
 * @author ameshkov
 */
public class DefaultQueue implements Queue {

    private ConcurrentLinkedQueue innerList = new ConcurrentLinkedQueue();

    public void add(Object obj) {
        innerList.add(obj);
    }

    public void defer(Object obj) {
        add(obj);
    }

    public Object poll() {
        return innerList.poll();
    }

    public void dispose() {
        // Do nothing
    }

    public int size() {
        return innerList.size();
    }
}
