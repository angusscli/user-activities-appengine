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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@SuppressWarnings("serial")
@WebServlet(name = "demo", value = "/demo")
public class DemoServlet extends HttpServlet {
	private final static Logger log = Logger.getLogger(DemoServlet.class.getName());
	private static String[] links = new String[] {
			"https://www.cnbc.com/id/100003114/device/rss/rss.html"
	};

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		PrintWriter out = resp.getWriter();

		for (String link : links) {
			Document doc = Jsoup.connect(link).get();
			Elements items = doc.select("item");

			for (Element item : items) {
				News news = new News();
				news.setTitle(item.select("title").text());
				news.setDescription(item.select("description").text());
				news.setId(item.select("guid").text());
				news.setType("cnbc");
		        SimpleDateFormat parser = new SimpleDateFormat("EEE, d MMM yyyy HH:mm zzz");
		        Date date;

		        try {
					date = parser.parse(item.select("pubDate").text());

					SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
					String formattedDate = formatter.format(date);
					news.setDate(formattedDate);
				} catch (ParseException e) {
						e.printStackTrace();
				}

		        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

				try {
					NewsPublisher.publish(gson.toJson(news));
				} catch (Exception e) {
					log.severe(e.getMessage());
				}
			}
		}

		out.println("Done");
	}
}
