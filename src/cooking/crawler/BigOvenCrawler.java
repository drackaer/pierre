package cooking.crawler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class BigOvenCrawler {
	public String searchForRecipes(String keywords) {
		String endpoint = "http://api.bigoven.com/recipes";
		String name = "title_kw";
		String value = keywords;
		
		String nameValuePair = name + "=" + value;
		
		String results = sendGetRequest(endpoint, nameValuePair);
		
		System.out.println(results);
		
		return results;
	}
	
	/**
	 * Sends an HTTP GET request to a url
	 *
	 * @param endpoint - The URL of the server. (Example: " http://www.yahoo.com/search")
	 * @param requestParameters - all the request parameters (Example: "param1=val1&param2=val2"). Note: This method will add the question mark (?) to the request - DO NOT add it yourself
	 * @return - The response from the end point
	 */
	public static String sendGetRequest(String endpoint, String requestParameters)
	{
		String result = null;
		if (endpoint.startsWith("http://"))
		{
			// Send a GET request to the servlet
			try
			{
				// Construct data
				//StringBuffer data = new StringBuffer();
				// Send data
				String urlStr = endpoint;
				if (requestParameters != null && requestParameters.length () > 0)
				{
					urlStr += "?" + requestParameters;
				}
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection ();
				conn.setRequestProperty("Accept", "application/json");
				conn.setDoOutput(true);
				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = rd.readLine()) != null)
				{
					sb.append(line);
				}
				rd.close();
				result = sb.toString();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return result;
	}	
}
