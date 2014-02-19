package eu.leads.crawler;

import eu.leads.crawler.download.DefaultDownloaderController;
import eu.leads.crawler.model.CrawlerTask;
import eu.leads.crawler.model.Page;
import eu.leads.crawler.parse.DefaultParserController;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 *
 * @author ameshkov
 */
public class TestDefaultCrawler {

    @Test
    public void testDefaultCrawler() throws Exception {
        DefaultCrawler crawler = new DefaultCrawler();
        crawler.setDownloaderController(new DefaultDownloaderController());
        crawler.setParserController(new DefaultParserController());

        CrawlerTask crawlerTask = new CrawlerTask("http://www.wikipedia.org/", 0);
        Page page = crawler.crawl(crawlerTask);

        assertNotNull(page);
        assertTrue(page.getLinks().size() > 0);
    }
}
