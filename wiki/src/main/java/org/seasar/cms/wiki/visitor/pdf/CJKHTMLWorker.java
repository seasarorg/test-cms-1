/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.cms.wiki.visitor.pdf;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocListener;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.TextElementArray;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.IncCell;
import com.lowagie.text.html.simpleparser.IncTable;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfPTable;


/**
 * Extends HTMLWorker to use CJKFactoryProperties instead of 
 * FactoryProperties while parsing.
 * 
 * @author someda
 */
public class CJKHTMLWorker extends HTMLWorker {

	public CJKHTMLWorker(DocListener doc){
		super(doc);
	}
	
    public static ArrayList parseToList(Reader reader, StyleSheet style) throws IOException {
        CJKHTMLWorker worker = new CJKHTMLWorker(null);
        if (style != null)
            worker.style = style;
        worker.document = worker;
        worker.objectList = new ArrayList();
        worker.parse(reader);
        return worker.objectList;
    }
	
    public void startElement(String tag, HashMap h) {
        if (!tagsSupported.containsKey(tag))
            return;
        style.applyStyle(tag, h);
        String follow = (String)CJKFactoryProperties.followTags.get(tag);
        if (follow != null) {
            HashMap prop = new HashMap();
            prop.put(follow, null);
            cprops.addToChain(follow, prop);
            return;
        }
        if (tag.equals("a")) {
            cprops.addToChain(tag, h);
            if (currentParagraph == null)
                currentParagraph = new Paragraph();
            stack.push(currentParagraph);
            currentParagraph = new Paragraph();
            return;
        }
        if (tag.equals("br")) {
            if (currentParagraph == null)
                currentParagraph = new Paragraph();
            currentParagraph.add(CJKFactoryProperties.createChunk("\n", cprops));
            return;
        }
        if (tag.equals("font") || tag.equals("span")) {
            cprops.addToChain(tag, h);
            return;
        }
        endElement("p");
        if (tag.equals("h1") || tag.equals("h2") || tag.equals("h3") || tag.equals("h4") || tag.equals("h5") || tag.equals("h6")) {
            if (!h.containsKey("size"))
                h.put("size", tag.substring(1));
            cprops.addToChain(tag, h);
            return;
        }
        if (tag.equals("ul")) {
            if (pendingLI)
                endElement("li");
            skipText = true;
            cprops.addToChain(tag, h);
            com.lowagie.text.List list = new com.lowagie.text.List(false, 10);
            list.setListSymbol("\u2022");
            stack.push(list);
            return;
        }
        if (tag.equals("ol")) {
            if (pendingLI)
                endElement("li");
            skipText = true;
            cprops.addToChain(tag, h);
            com.lowagie.text.List list = new com.lowagie.text.List(true, 10);
            stack.push(list);
            return;
        }
        if (tag.equals("li")) {
            if (pendingLI)
                endElement("li");
            skipText = false;
            pendingLI = true;
            cprops.addToChain(tag, h);
            stack.push(new com.lowagie.text.ListItem());
            return;
        }
        if (tag.equals("div") || tag.equals("body")) {
            cprops.addToChain(tag, h);
            return;
        }
        if (tag.equals("pre")) {
            if (!h.containsKey("face")) {
                h.put("face", "Courier");
            }
            cprops.addToChain(tag, h);
            isPRE = true;
            return;
        }
        if (tag.equals("p")) {
            cprops.addToChain(tag, h);
            currentParagraph = CJKFactoryProperties.createParagraph(h);
            return;
        }
        if (tag.equals("tr")) {
            if (pendingTR)
                endElement("tr");
            skipText = true;
            pendingTR = true;
            cprops.addToChain("tr", h);
            return;
        }
        if (tag.equals("td") || tag.equals("th")) {
            if (pendingTD)
                endElement(tag);
            skipText = false;
            pendingTD = true;
            cprops.addToChain("td", h);
            stack.push(new IncCell(tag, cprops));
            return;
        }
        if (tag.equals("table")) {
            cprops.addToChain("table", h);
            IncTable table = new IncTable(h);
            stack.push(table);
            tableState.push(new boolean[]{pendingTR, pendingTD});
            pendingTR = pendingTD = false;
            skipText = true;
            return;
        }
    }
    
    public void endElement(String tag) {
        if (!tagsSupported.containsKey(tag))
            return;
        try {
            String follow = (String)CJKFactoryProperties.followTags.get(tag);
            if (follow != null) {
                cprops.removeChain(follow);
                return;
            }
            if (tag.equals("font") || tag.equals("span")) {
                cprops.removeChain(tag);
                return;
            }
            if (tag.equals("a")) {
                String href = cprops.getProperty("href");
                if (currentParagraph == null)
                    currentParagraph = new Paragraph();
                if (href != null) {
                    ArrayList chunks = currentParagraph.getChunks();
                    for (int k = 0; k < chunks.size(); ++k) {
                        Chunk ck = (Chunk)chunks.get(k);
                        ck.setAnchor(href);
                    }
                }
                Paragraph tmp = (Paragraph)stack.pop();
                Phrase tmp2 = new Phrase();
                tmp2.add(currentParagraph);
                tmp.add(tmp2);
                currentParagraph = tmp;
                cprops.removeChain("a");
                return;
            }
            if (tag.equals("br")) {
                return;
            }
            if (currentParagraph != null) {
                if (stack.empty())
                    document.add(currentParagraph);
                else {
                    Object obj = stack.pop();;
                    if (obj instanceof TextElementArray) {
                        TextElementArray current = (TextElementArray)obj;
                        current.add(currentParagraph);
                    }
                    stack.push(obj);
                }
            }
            currentParagraph = null;
            if (tag.equals("ul") || tag.equals("ol")) {
                if (pendingLI)
                    endElement("li");
                skipText = false;
                cprops.removeChain(tag);
                if (stack.empty())
                    return;
                Object obj = stack.pop();
                if (!(obj instanceof com.lowagie.text.List)) {
                    stack.push(obj);
                    return;
                }
                if (stack.empty())
                    document.add((Element)obj);
                else
                    ((TextElementArray)stack.peek()).add(obj);
                return;
            }
            if (tag.equals("li")) {
                pendingLI = false;
                skipText = true;
                cprops.removeChain(tag);
                if (stack.empty())
                    return;
                Object obj = stack.pop();
                if (!(obj instanceof ListItem)) {
                    stack.push(obj);
                    return;
                }
                if (stack.empty()) {
                    document.add((Element)obj);
                    return;
                }
                Object list = stack.pop();
                if (!(list instanceof com.lowagie.text.List)) {
                    stack.push(list);
                    return;
                }
                ListItem item = (ListItem)obj;
                ((com.lowagie.text.List)list).add(item);
                ArrayList cks = item.getChunks();
                if (cks.size() > 0)
                    item.listSymbol().setFont(((Chunk)cks.get(0)).font());
                stack.push(list);
                return;
            }
            if (tag.equals("div") || tag.equals("body")) {
                cprops.removeChain(tag);
                return;
            }
            if (tag.equals("pre")) {
                cprops.removeChain(tag);
                isPRE = false;
                return;
            }
            if (tag.equals("p")) {
                cprops.removeChain(tag);
                pendingP = false;
                return;
            }
            if (tag.equals("h1") || tag.equals("h2") || tag.equals("h3") || tag.equals("h4") || tag.equals("h5") || tag.equals("h6")) {
                cprops.removeChain(tag);
                return;
            }
            if (tag.equals("table")) {
                if (pendingTR)
                    endElement("tr");
                cprops.removeChain("table");
                IncTable table = (IncTable) stack.pop();
                PdfPTable tb = table.buildTable();
                tb.setSplitRows(true);
                if (stack.empty())
                    document.add(tb);
                else
                    ((TextElementArray)stack.peek()).add(tb);
                boolean state[] = (boolean[])tableState.pop();
                pendingTR = state[0];
                pendingTD = state[1];
                skipText = false;
                return;
            }
            if (tag.equals("tr")) {
                if (pendingTD)
                    endElement("td");
                pendingTR = false;
                cprops.removeChain("tr");
                ArrayList cells = new ArrayList();
                IncTable table = null;
                while (true) {
                    Object obj = stack.pop();
                    if (obj instanceof IncCell) {
                        cells.add(((IncCell)obj).getCell());
                    }
                    if (obj instanceof IncTable) {
                        table = (IncTable)obj;
                        break;
                    }
                }
                table.addCols(cells);
                table.endRow();
                stack.push(table);
                skipText = true;
                return;
            }
            if (tag.equals("td") || tag.equals("th")) {
                pendingTD = false;
                cprops.removeChain("td");
                skipText = true;
                return;
            }
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    public void text(String str) {
        if (skipText)
            return;
        String content = str;
        if (isPRE) {
            if (currentParagraph == null)
                currentParagraph = new Paragraph();
            currentParagraph.add(CJKFactoryProperties.createChunk(content, cprops));
            return;
        }
        if (content.trim().length() == 0 && content.indexOf(' ') < 0) {
            return;
        }
        
        StringBuffer buf = new StringBuffer();
        int len = content.length();
        char character;
        boolean newline = false;
        for (int i = 0; i < len; i++) {
            switch(character = content.charAt(i)) {
                case ' ':
                    if (!newline) {
                        buf.append(character);
                    }
                    break;
                case '\n':
                    if (i > 0) {
                        newline = true;
                        buf.append(' ');
                    }
                    break;
                case '\r':
                    break;
                case '\t':
                    break;
                    default:
                        newline = false;
                        buf.append(character);
            }
        }
        if (currentParagraph == null)
            currentParagraph = CJKFactoryProperties.createParagraph(cprops);
        currentParagraph.add(CJKFactoryProperties.createChunk(buf.toString(), cprops));
    }
	
}
