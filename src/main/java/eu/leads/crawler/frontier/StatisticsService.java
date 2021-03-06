package eu.leads.crawler.frontier;

import eu.leads.crawler.frontier.DomainStatistics;
import eu.leads.crawler.model.CrawlerTask;
import eu.leads.crawler.model.Page;

/**
 * Service for saving crawler statistics. Also saves parsed documents urls.
 * @author ameshkov
 */
public interface StatisticsService {

    /**
     * Checks if specified url is already crawled
     * @param url
     */
    boolean isCrawled(String url);

    /**
     * Method called when {@code task} has been schedulled
     * @param task
     */
    void afterScheduling(CrawlerTask task);

    /**
     * Method called after (@code task} has been downloaded
     * @param task
     * @param page {@link eu.leads.crawler.model.Page} downloaded
     */
    void afterDownloading(CrawlerTask task, Page page);

    /**
     * Method called after {@link eu.leads.crawler.model.Page} has been parsed
     * @param task
     * @param page
     */
    void afterParsing(CrawlerTask task, Page page);

    /**
     * Returns specified domain statistics
     * @param domainName
     * @return
     */
    DomainStatistics getDomainStatistics(String domainName);

    /**
     * Returns count of scheduled tasks
     * @return
     */
    long getScheduled();

    /**
     * Returns count of downloaded pages
     * @return
     */
    long getDownloaded();

    /**
     * Returns count of parsed pages
     * @return
     */
    long getParsed();

    /**
     * Returns count of errors
     * @return
     */
    long getErrors();

    /**
     * Disposes statistics service
     */
    void dispose();
}
