package eu.leads.crawler.utils;

import com.googlecode.flaxcrawler.concurrent.Queue;
import org.infinispan.Cache;
import org.infinispan.atomic.AtomicObjectFactory;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static eu.leads.crawler.utils.Digest.digest;
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

    public static synchronized ConcurrentMap getOrCreatePersistentMap(String name) {
        return manager.getCache(name);
    }

    public static Queue getOrCreateQueue(String name){
        initFactory();
        return new InfinispanQueue("queue:"+name);
    }

    public static Set getOrCreateSet(String name){
        initFactory();
        return (Set) factory.getOrCreateInstanceOf(HashSet.class, "set:"+name);
    }

    public static void addListenerForMap(Object listener, ConcurrentMap map){
        ((Cache)map).addListener(listener);
    }

    private static synchronized void initFactory(){
        if(factory == null)
            factory = new AtomicObjectFactory(manager.getCache("objects"));
    }

    /**
     * This class implements a Flaxcrawler queue (com.googlecode.flaxcrawler.concurrent.Queue)
     * on top of inifinispan using the AtomicTypeFactory facility.
     */
    private static class InfinispanQueue implements Queue {

        private LinkedList queue;

        public InfinispanQueue(String name){
            queue = (LinkedList)factory.getOrCreateInstanceOf(LinkedList.class, name,true,null,false);
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
