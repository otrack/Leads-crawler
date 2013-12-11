package eu.leads.crawler.parse;

import eu.leads.crawler.model.Page;
import eu.leads.crawler.parse.ParserCallback;
import it.unimi.dsi.parser.callback.TextExtractor;

/**
 * {@link eu.leads.crawler.parse.ParserCallback} for extracting page title and text
 * @author ameshkov
 */
public class TextExtractorCallback extends TextExtractor implements ParserCallback {

    public void startPage(Page page) {
        // Doing nothing
    }

    public void endPage(Page page) {
        page.setTitle(this.title.toString());
    }
}
