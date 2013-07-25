package eu.leads.crawler;

import eu.leads.crawler.utils.Infinispan;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.getProperties;

/**
 *
 * @author P. Sutra
 *
 */
public class InstantaneousCrawl {

    public static void main(String[] args) {

        try{
            Properties properties = getProperties();
            properties.load(InstantaneousCrawl.class.getClassLoader().getResourceAsStream("config.properties"));
            System.out.println("Found properties file.");
        } catch (IOException e) {
            System.out.println("Found no config.properties file; defaulting.");
        }

        Infinispan.start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }

        ConcurrentMap<String,CrawlResult> postrocessingMap = Infinispan.getOrCreatePersistentMap("postprocessingMap");

        TreeMap<CrawlResult,String> sortedResults = new TreeMap(Collections.reverseOrder());
        for(String url : postrocessingMap.keySet()){
            sortedResults.put(postrocessingMap.get(url),url);
        }

        for(CrawlResult result : sortedResults.keySet()){
            System.out.println(result+" "+sortedResults.get(result));
        }

        Infinispan.stop();

        System.exit(0);

    }

}
