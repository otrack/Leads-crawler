package eu.leads.crawler;

import com.googlecode.flaxcrawler.CrawlerConfiguration;
import com.googlecode.flaxcrawler.CrawlerController;
import com.googlecode.flaxcrawler.CrawlerException;
import com.googlecode.flaxcrawler.download.DefaultDownloader;
import com.googlecode.flaxcrawler.download.DefaultDownloaderController;
import com.googlecode.flaxcrawler.download.DefaultProxyController;
import com.googlecode.flaxcrawler.parse.DefaultParser;
import com.googlecode.flaxcrawler.parse.DefaultParserController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static eu.leads.crawler.utils.Infinispan.getOrCreateQueue;
import static java.lang.System.getProperties;


/**
 * // TODO: Document this
 *
 * @author otrack
 * @since 4.0
 */
public class PersistentCrawl {

    public static void main(String[] args) {

        List<Proxy> proxies = new ArrayList<Proxy>();
        String seed = "http://www.economist.com/";
        ArrayList<String> words = new ArrayList<String>();
        int ncrawlers = 1;

        try{
            Properties properties = getProperties();
            properties.load(PersistentCrawl.class.getClassLoader().getResourceAsStream("config.properties"));
            System.out.println("Found properties file.");
        } catch (IOException e) {
            System.out.println("Found no config.properties file; defaulting.");
        }

        if(getProperties().containsKey("seed")){
            seed = getProperties().getProperty("seed");
        }

        if(getProperties().containsKey("words")){
            for(String w : getProperties().get("words").toString().split(",")){
                System.out.println("Adding word "+w);
                words.add(w);
            }
        }else{
            words.add("Obama");
        }

        if(getProperties().containsKey("ncrawlers")){
            ncrawlers = Integer.valueOf(getProperties().getProperty("ncrawlers"));
            System.out.println("Using "+ncrawlers+" crawler(s)");
        }
        proxies.add(Proxy.NO_PROXY);
        DefaultProxyController proxyController = new DefaultProxyController(proxies);

        DefaultDownloader downloader = new DefaultDownloader();
        downloader.setAllowedContentTypes(new String[]{"text/html", "text/plain"});
        downloader.setMaxContentLength(500000);
        downloader.setTriesCount(3);
        downloader.setProxyController(proxyController);

        DefaultDownloaderController downloaderController = new DefaultDownloaderController();
        downloaderController.setGenericDownloader(downloader);

        DefaultParserController defaultParserController = new DefaultParserController();
        defaultParserController.setGenericParser(DefaultParser.class);

        CrawlerConfiguration configuration = new CrawlerConfiguration();
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_BAD_GATEWAY, 10);
        configuration.setMaxLevel(10);
        configuration.setMaxParallelRequests(5);
        configuration.setPolitenessPeriod(500);

        for (int i = 0; i < ncrawlers; i++) {
            PersistentCrawler crawler = new PersistentCrawler(words);
            crawler.setDownloaderController(downloaderController);
            crawler.setParserController(defaultParserController);
            configuration.addCrawler(crawler);
        }

        CrawlerController crawlerController = new CrawlerController(configuration);
        try {
            crawlerController.setQueue(getOrCreateQueue());
            if(!seed.equals("")) crawlerController.addSeed(new URL(seed));
            crawlerController.start();
            crawlerController.join();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (CrawlerException e) {
            e.printStackTrace();
        }
    }


}
