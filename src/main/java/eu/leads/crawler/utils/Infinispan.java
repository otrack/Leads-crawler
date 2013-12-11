package eu.leads.crawler.utils;

import eu.leads.crawler.concurrent.Queue;
import org.infinispan.Cache;
import org.infinispan.InvalidCacheUsageException;
import org.infinispan.atomic.AtomicObjectFactory;
import org.infinispan.manager.DefaultCacheManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.getProperties;

/**
 *
 * @author P. Sutra
 *
 */
public class Infinispan {

    private static AtomicObjectFactory factory;
    private static DefaultCacheManager manager;

    public static void start(){
        String infinispanConfig = getProperties().getProperty("infinispanConfigFile");

        if(infinispanConfig != null){
            try {
                manager = new DefaultCacheManager(infinispanConfig);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Incorrect Infinispan configuration file");
            }
        }else{
            manager = new DefaultCacheManager();
        }
        manager.start();

    }

    public static void stop(){
        manager.stop();
    }

    public static synchronized Cache getOrCreatePersistentMap(String name) {
        return manager.getCache(name);
    }

    public static Queue getOrCreateQueue(String name){
        initFactory();
        return new InfinispanQueue("queue:"+name);
    }

    public static Set getOrCreateSet(String name){
        initFactory();
        try {
            return (Set) factory.getInstanceOf(HashSet.class, "set:" + name);
        } catch (InvalidCacheUsageException e) {
        }
        return  null;
    }


    public static void addListenerToMap(Object listener, ConcurrentMap map){
        ((Cache)map).addListener(listener);
    }

    private static synchronized void initFactory(){
        if(factory == null)
            try {
                factory = new AtomicObjectFactory(manager.getCache("objects"));
            } catch (InvalidCacheUsageException e) {
                e.printStackTrace();  // TODO: Customise this generated block
            }
    }

    /**
     * This class implements a Queue on top of inifinispan using the AtomicObjectFactory facility.
     */
    private static class InfinispanQueue implements Queue {

        private LinkedList queue;

        public InfinispanQueue(String name){
            try {
                queue = (LinkedList)factory.getInstanceOf(LinkedList.class, name, true, null, false);
            } catch (InvalidCacheUsageException e) {
                e.printStackTrace();  // TODO: Customise this generated block
            }
        }

        public void add(Object obj) {
            queue.add(obj);
        }

        public void defer(Object obj) {
            queue.add(obj);
        }

        public Object poll() {
            return queue.poll();
        }

        public void dispose() {
        }

        public int size() {
            return queue.size();
        }

        @Override
        public String toString(){
            return queue.toString();
        }
    }

}
