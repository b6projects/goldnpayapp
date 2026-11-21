package com.gps.os.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;

/**
 * Cleans the body HTML written by Core before it goes into a template.
 * Removes script, form, embed-type tags, inline event handlers and javascript: URLs.
 */
@Component
public class HtmlSanitizer {

    private static final String BLOCKED_TAGS =
            "script, form, iframe, object, embed, applet, base, meta, link, style, noscript, input, button, textarea, select";

    public String clean(String html) {
        Document doc = Jsoup.parseBodyFragment(html == null ? "" : html);
        doc.outputSettings().prettyPrint(false);
        doc.select(BLOCKED_TAGS).remove();
        for (Element el : doc.getAllElements()) {
            for (Attribute a : el.attributes().asList()) {
                String key = a.getKey().toLowerCase();
                String val = a.getValue().replaceAll("[\\s\\x00-\\x1f]", "").toLowerCase();
                boolean urlAttr = key.equals("href") || key.equals("src") || key.equals("action")
                        || key.equals("formaction") || key.equals("xlink:href") || key.equals("data");
                if (key.startsWith("on")
                        || (urlAttr && (val.startsWith("javascript:") || val.startsWith("vbscript:") || val.startsWith("data:text")))) {
                    el.removeAttr(a.getKey());
                }
            }
        }
        return doc.body().html();
    }

    /** Plain-text alternative for the multipart/alternative part. */
    public String toText(String html) {
        return Jsoup.parse(html == null ? "" : html, "", Parser.htmlParser()).text();
    }
}
