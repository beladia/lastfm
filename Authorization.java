package lastfm;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class Authorization {

    Authorization() {}

	public static String getAuthorization(String key)  {
		Client client = JerseyClient.getClient();
		WebResource webResource = client.resource("http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key="+key);
		System.out.println("http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key="+key);
		//webResource.addFilter(new LoggingFilter());
		String response = webResource.get(String.class);
		//ClientResponse cr = webResource.type("application/json").get(ClientResponse.class);
		String token = response.substring(response.indexOf("<token>")+7, response.indexOf("</token>"));
		return token;
	}

	/*public static void main(String[] args) {
		try {
			System.out.println(Authorization.getAuthorization("e77094ac5bf414726b355017e631d048"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}*/

}
