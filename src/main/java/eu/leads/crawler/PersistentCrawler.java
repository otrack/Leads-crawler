package eu.leads.crawler;

import eu.leads.crawler.model.CrawlerTask;
import eu.leads.crawler.model.Page;
import eu.leads.crawler.utils.Infinispan;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.ConcurrentMap;


/**
* @author P.Sutra
 *
*/
public class PersistentCrawler extends DefaultCrawler {

    private static ConcurrentMap preprocessingMap = Infinispan.getOrCreatePersistentMap("preprocessingMap");

    private static ObjectMapper mapper = new ObjectMapper();

    private static Log log = LogFactory.getLog(PersistentCrawler.class.getName());

    public PersistentCrawler(){
    }

    @Override
    protected void afterCrawl(CrawlerTask crawlerTask, Page page) {
        super.afterCrawl(crawlerTask, page);

        if ( page == null
             || page.getResponseCode() != HttpURLConnection.HTTP_OK
             || page.getContent().isEmpty()){  // this pages violated the crawler constraints (size, etc..).
            return;
        }

        log.info("Crawled: " + page.getUrl().toString());

        try {
            preprocessingMap.putIfAbsent(page.getUrl().toString(), mapper.writeValueAsString(page));
        } catch (IOException e) {
            e.printStackTrace();  // TODO: Customise this generated block
        }

    }

    @Override
    public boolean shouldCrawl(CrawlerTask task, CrawlerTask parent){
        if(preprocessingMap.containsKey(task.getUrl().toString())){
            log.debug("Page already crawled: " + task.getUrl().toString() + " ; thrashing.");
            return false;
        }
        return super.shouldCrawl(task,parent);
        // return task.getDomain() != null && parent.getDomain() != null;
    }

}
