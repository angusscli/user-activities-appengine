/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloud.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.cloud.sample.bean.News;
import com.google.api.client.util.Data;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;


@SuppressWarnings("serial")
@WebServlet(name = "twitterStream", value = "/twitterStream")
public class TwitterServlet extends HttpServlet {
	private final static Logger log = Logger.getLogger(TwitterServlet.class.getName());


	FilterQuery filtered = new FilterQuery();

	String keywords[] ={ "#AlphaStock","#FC4","#FC5"};
	
	ConfigurationBuilder cb = new ConfigurationBuilder();
	
    
	TwitterStreamFactory tf = null;
	TwitterStream twitterStream = null;
	
	@Override
	public void init() {
		
		  cb = new ConfigurationBuilder();


		    cb.setDebugEnabled(true)
		      .setOAuthConsumerKey("")
		      .setOAuthConsumerSecret("")
		      .setOAuthAccessToken("")
		      .setOAuthAccessTokenSecret("");
	        
	       tf = new TwitterStreamFactory(cb.build());
	       
//	   	   filtered.track(keywords);
//		   filtered.language("en");
		
	       System.out.println("Servlet Started");
	        	
	}
	
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	
		try {
			
				String action = (req.getParameter("action")!=null?req.getParameter("action"):"");
				String updateKeywords[] = (req.getParameterValues("keywords")!=null?req.getParameterValues("keywords"):null);
		
				switch (action)
		        {
					case "stop" :
						log.info("Servlet action: "+ action);
						shutdown();
						break;
					case "update" :
						log.info("Servlet action: "+ action);
										
								if (updateKeywords!=null && updateKeywords.length>0 )
								{
									shutdown();
									keywords = updateKeywords;
									log.info("Update:" + Arrays.toString(keywords));
									startup();
								}	
						break;
					case "show" :
						log.info("Current Keywords:" + Arrays.toString(keywords));
						break;
					default:
						log.info("Servlet action: "+ action);
						startup();
						break;
		        }
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    				
				
	}
	
	private void shutdown() throws Exception {
		twitterStream.cleanUp();
		twitterStream.shutdown();
		twitterStream = null;
		
	}
	
	private void startup() throws Exception {
		
		if (twitterStream == null)
		{			
			filtered.track(keywords);
			filtered.language("en");
			
			twitterStream = tf.getInstance();
			
			twitterStream.addListener(listener);
			
			if (keywords.length==0) {
				twitterStream.sample();
			} else { 
			    twitterStream.filter(filtered);
			}
			
			System.out.println("Twitter Stream Connected with Filters:" + Arrays.toString(keywords));
		}
		
	}
	
	StatusListener listener = new StatusListener(){
		
		News news = new News();
        public void onStatus(Status status) {
            //System.out.println(status.getUser().getName() + " : " + status.getText());
            
            
            
            news.setTitle(status.getUser().getName()+": "+ status.getText());
			news.setDescription(status.getUser().getName()+": "+ status.getText());
		
			log.info("Status" + status.getUser().getName()+ ":" + status.getText());
			log.info("Description" + status.getUser().getName()+":"+ status.getText());
			
			news.setId(Long.toString(status.getId()));
			news.setType("twitter");
			
	        SimpleDateFormat parser = new SimpleDateFormat("EEE, d MMM yyyy HH:mm zzz");
	        Date date;
	
	        //date = parser.parse(status.getCreatedAt());
			date = status.getCreatedAt();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			String formattedDate = formatter.format(date);
			news.setDate(formattedDate);
	
	        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
			try {
				NewsPublisher.publish(gson.toJson(news));
				//System.out.println(gson.toJson(news).toString());
			} catch (Exception e) {
				log.severe(e.getMessage());
			}
    }
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

    public void onException(Exception ex){
        ex.printStackTrace();
    }
    
	@Override
	public void onScrubGeo(long arg0, long arg1) {
		// TODO Auto-generated method stub
		  System.out.println("Got scrub_geo event userId:" + arg0 + " upToStatusId:" + arg1);
	}
	@Override
	public void onStallWarning(StallWarning arg0) {
		// TODO Auto-generated method stub
		  System.out.println("Got stall warning:" + arg0);
		
	}
};
}
