/*
 * Copyright 2016 Google Inc.
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

package com.example.appengine.memcache;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(name = "login",
    urlPatterns = "/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    String path = req.getRequestURI();
    if (path.startsWith("/favicon.ico")) {
      return; // ignore the request for favicon.ico
    }
	byte[] successKey = "test_success".getBytes();

    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

    byte[] currentKey = "test_current".getBytes();
    byte[] totalKey = "test_total".getBytes();

    Long current = syncCache.increment(currentKey, 1L, 0L);
    Long total = syncCache.increment(totalKey, 1L, 0L);
    
    Long success = syncCache.increment(successKey, 0L, 0L);

	double ratio = ((double)success / (double)total)*100;
	

	// double ratio = 0;
	double start = 4;
	double end = 6;
	double random = new Random().nextDouble();
	double result = start + (random * (end - start));
	
	if (total==1) {
		result = 0;
	}

	String message = "{\"type\":\"total\",\"currentuser\":\"" + current + "\",\"total\":\"" + total
			+ "\",\"successrate\":\"" + round(ratio,1) + "\",\"engagement\":\"" + round(result, 1) + "\"}";
	
	
	try {
		UserPublisher.publish(message);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	resp.getWriter().print(message);

  }
	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
}
// [END example]
