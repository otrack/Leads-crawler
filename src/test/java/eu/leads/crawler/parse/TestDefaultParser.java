package eu.leads.crawler.parse;

import eu.leads.crawler.parse.DefaultParser;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import eu.leads.crawler.download.DefaultDownloader;
import eu.leads.crawler.download.DefaultProxyController;
import eu.leads.crawler.download.DownloadException;
import eu.leads.crawler.model.Page;
import static org.junit.Assert.*;

/**
 *
 * @author ameshkov
 */
public class TestDefaultParser {

    @Test
    @Ignore
    public void testDefaultParser() throws MalformedURLException, DownloadException {
        List<Proxy> proxies = new ArrayList<Proxy>();
        proxies.add(Proxy.NO_PROXY);

        DefaultProxyController proxyController = new DefaultProxyController();
        proxyController.setProxies(proxies);

        DefaultDownloader downloader = new DefaultDownloader();
        downloader.setProxyController(proxyController);
        downloader.setTriesCount(3);

        Page page = downloader.download(new URL("http://vipzone.ws"));
        assertNotNull(page);

        DefaultParser parser = new DefaultParser();
        parser.parse(page);

        assertNotNull(page.getLinks());

        for (URL url : page.getLinks()) {
            System.out.println(url.toString());
        }
    }
}
