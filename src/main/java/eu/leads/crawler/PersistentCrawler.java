package eu.leads.crawler;

import com.googlecode.flaxcrawler.DefaultCrawler;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;
import com.likethecolor.alchemy.api.Client;
import com.likethecolor.alchemy.api.call.SentimentCall;
import com.likethecolor.alchemy.api.call.type.CallTypeUrl;
import com.likethecolor.alchemy.api.entity.Response;
import com.likethecolor.alchemy.api.entity.SentimentAlchemyEntity;
import eu.leads.crawler.utils.Infinispan;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.getProperties;

/**
* // TODO: Document this
*
* @author otrack
* @since 4.0
*/
public class PersistentCrawler extends DefaultCrawler {

    private static ConcurrentMap map;
    private static Client client;

    static{
        if(getProperties().containsKey("sentimentAnalysisKeyFile")){
            String sentimentAnalysisKeyFile = getProperties().getProperty("sentimentAnalysisKeyFile");
            try {
                client = new Client(sentimentAnalysisKeyFile);
            } catch (IOException e) {
                e.printStackTrace();  // TODO: Customise this generated block
            }
        }
        map = Infinispan.getOrCreatePersistentMap();
    }

    private List<String> words;

    public PersistentCrawler(List<String> l){
        words = l;
    }

    private boolean isMatching(Page page){
        if(page.getContent() == null)
            return false;
        for(String w : words){
            if(!page.getContentString().contains(w))
                return false;
        }
        return true;
    }

    @Override
    protected void afterCrawl(CrawlerTask crawlerTask, Page page) {
        super.afterCrawl(crawlerTask, page);

        System.out.println("Crawling "+page.getUrl().toString());

        if ( page == null
             || page.getResponseCode() != HttpURLConnection.HTTP_OK
             || ! isMatching(page))
            return;

        if(client == null)
            return;

        SentimentCall call = new SentimentCall(new CallTypeUrl(page.getUrl().toString()));
        try {
            Response response = client.call(call);
            SentimentAlchemyEntity entity = (SentimentAlchemyEntity) response.iterator().next();
            System.out.println(page.getUrl() + " : " + entity.getType().toString() + " = " + entity.getScore().toString());
            if(entity.getType().equals(SentimentAlchemyEntity.TYPE.POSITIVE))
                map.putIfAbsent(page.getUrl(), response);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
