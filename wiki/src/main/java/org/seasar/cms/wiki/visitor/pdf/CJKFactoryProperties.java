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

import java.awt.Color;
import java.util.StringTokenizer;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.html.simpleparser.ChainedProperties;
import com.lowagie.text.html.simpleparser.FactoryProperties;
import com.lowagie.text.pdf.BaseFont;

/**
 * @author someda
 */
public class CJKFactoryProperties extends FactoryProperties {
	
	public CJKFactoryProperties(){
		super();
	}
	
    public static Chunk createChunk(String text, ChainedProperties props) {
        Chunk ck = new Chunk(text, getFont(props));
        if (props.hasProperty("sub"))
            ck.setTextRise(-6);
        else if (props.hasProperty("sup"))
            ck.setTextRise(6);
        return ck;
    }
    	
    public static Font getFont(ChainedProperties props) {
        String face = props.getProperty("face");
        if (face != null) {
            StringTokenizer tok = new StringTokenizer(face, ",");
            while (tok.hasMoreTokens()) {
                face = tok.nextToken().trim();
                if (FontFactory.isRegistered(face))
                    break;
            }
        }
        int style = 0;
        if (props.hasProperty("i"))
            style |= Font.ITALIC;
        if (props.hasProperty("b"))
            style |= Font.BOLD;
        if (props.hasProperty("u"))
            style |= Font.UNDERLINE;
        String value = props.getProperty("size");
        float size = 12;
        if (value != null)
            size = Float.valueOf(value).floatValue();
        Color color = decodeColor(props.getProperty("color"));
    
        // extended here !!
        String encoding = props.getProperty("cjk_encoding");
        String embededstr = props.getProperty("cjk_embeded");
        if(encoding == null || encoding.equals(""))
        	encoding = BaseFont.WINANSI;
        
        boolean embeded = true;
        if(embededstr != null)
        	embeded = Boolean.valueOf(embededstr).booleanValue();                
        
        return FontFactory.getFont(face, encoding, embeded, size, style, color);
    }

}
