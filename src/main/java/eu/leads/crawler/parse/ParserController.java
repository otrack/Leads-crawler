package eu.leads.crawler.parse;

import eu.leads.crawler.model.Page;
import eu.leads.crawler.parse.Parser;

/**
 * Manages parsers. Returns parser for the specified {@link eu.leads.crawler.model.Page}
 * @author ameshkov
 */
public interface ParserController {

    /**
     * Returns {@link eu.leads.crawler.parse.Parser} for the specified {@link eu.leads.crawler.model.Page}
     * @param page
     * @return
     */
    Parser getParser(Page page);
}
