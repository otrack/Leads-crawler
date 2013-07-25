package eu.leads.crawler;

import com.googlecode.flaxcrawler.CrawlerConfiguration;
import com.googlecode.flaxcrawler.CrawlerController;
import com.googlecode.flaxcrawler.concurrent.Queue;
import com.googlecode.flaxcrawler.download.DefaultDownloader;
import com.googlecode.flaxcrawler.download.DefaultDownloaderController;
import com.googlecode.flaxcrawler.download.DefaultProxyController;
import com.googlecode.flaxcrawler.parse.DefaultParser;
import com.googlecode.flaxcrawler.parse.DefaultParserController;
import eu.leads.crawler.utils.Infinispan;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static eu.leads.crawler.utils.Infinispan.getOrCreateQueue;
import static java.lang.System.getProperties;

/**
 *
 * @author P. Sutra
 *
 */
public class PersistentCrawl {

    private static Log log = LogFactory.getLog(PersistentCrawl.class.getName());

    public static void main(String[] args) {

        List<Proxy> proxies = new ArrayList<Proxy>();
        String seed = "http://www.economist.com/";
        ArrayList<String> words = new ArrayList<String>();
        int ncrawlers = 1;

        try{
            Properties properties = getProperties();
            properties.load(PersistentCrawl.class.getClassLoader().getResourceAsStream("config.properties"));
            log.info("Found properties file.");
        } catch (IOException e) {
            log.info("Found no config.properties file; defaulting.");
        }

        if(getProperties().containsKey("seed")){
            seed = getProperties().getProperty("seed");
            log.info("Seed : "+seed);
        }

        if(getProperties().containsKey("words")){
            for(String w : getProperties().get("words").toString().split(",")){
                log.info("Adding word :"+w);
                words.add(w);
            }
        }else{
            words.add("Obama");
        }

        Infinispan.start();

        if(getProperties().containsKey("ncrawlers")){
            ncrawlers = Integer.valueOf(getProperties().getProperty("ncrawlers"));
            log.info("Using "+ncrawlers+" crawler(s)");
        }
        proxies.add(Proxy.NO_PROXY);
        DefaultProxyController proxyController = new DefaultProxyController(proxies);

        DefaultDownloader downloader = new DefaultDownloader();
        downloader.setAllowedContentTypes(new String[]{"text/html", "text/plain"});
        downloader.setMaxContentLength(100000);
        downloader.setTriesCount(3);
        downloader.setProxyController(proxyController);

        DefaultDownloaderController downloaderController = new DefaultDownloaderController();
        downloaderController.setGenericDownloader(downloader);

        DefaultParserController defaultParserController = new DefaultParserController();
        defaultParserController.setGenericParser(DefaultParser.class);

        CrawlerConfiguration configuration = new CrawlerConfiguration();
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_BAD_GATEWAY, 10);
        configuration.setMaxLevel(3);
        configuration.setMaxParallelRequests(5);
        configuration.setPolitenessPeriod(500);

        try {

            PersistentListener listener = new PersistentListener(words);

            for (int i = 0; i < ncrawlers; i++) {
                PersistentCrawler crawler = new PersistentCrawler();
                crawler.setDownloaderController(downloaderController);
                crawler.setParserController(defaultParserController);
                configuration.addCrawler(crawler);
            }

            CrawlerController crawlerController = new CrawlerController(configuration);

            Queue q = getOrCreateQueue("queue");
            log.info(q.size());
            crawlerController.setQueue(q);
            if(!seed.equals("") && q.size()==0 ) crawlerController.addSeed(new URL(seed));
            crawlerController.start();
            crawlerController.join();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Infinispan.stop();

        System.out.println("Terminated.");

        while(true);

    }


}
