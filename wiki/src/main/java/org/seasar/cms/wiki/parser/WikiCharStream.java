/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.cms.wiki.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author someda
 */
public class WikiCharStream extends JavaCharStream implements CharStream{	
		
	private static final char LINE_SEPARATOR = '\n';
	
	public WikiCharStream(Reader reader){
		super(getLineBreakAppendReader(reader));
		init();
//		inputStream = new LineBreakAppendReader(reader);
	}
	
	public WikiCharStream(InputStream dstream){
		super(getLineBreakAppendReader(dstream));
		init();
//		inputStream = new LineBreakAppendReader(dstream);
	}
		
	/**
	 * どんなコンテンツでも最初に改行文を入れておく
	 */
	public void init(){
		nextCharBuf[0] = LINE_SEPARATOR;
		maxNextCharInd++;		
	}
	
	private static Reader getLineBreakAppendReader(Reader reader){
		return new LineBreakAppendReader(reader);
	}
	
	private static Reader getLineBreakAppendReader(InputStream stream){
		return new LineBreakAppendReader(stream);
	}
	
	/**
	 * どんなコンテンツでも最後に改行が入るように Reader を拡張
	 */
	private static class LineBreakAppendReader extends Reader{

		private boolean endStream = false;
		
		private Reader reader;
		
		public LineBreakAppendReader(Reader reader){
			super();
			this.reader = reader;
		}
		
		public LineBreakAppendReader(InputStream stream){
			super();
			this.reader = new InputStreamReader(stream);
		}		
			    
	    public int read(char cbuf[], int offset, int length) throws IOException {

			if(endStream){
				return -1;
			}
			int ret = reader.read(cbuf,offset,length);
			if(!endStream && ret == -1){
				cbuf[offset] = '\n';
				ret = 1;
				endStream = true;
			}						
			return ret;	    		
	    }
	    	    
		public boolean ready() throws IOException {
			return reader.ready();
		}
		
		public void close() throws IOException {
			reader.close();			
		}
	}
}
