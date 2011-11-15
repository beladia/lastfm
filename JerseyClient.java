package lastfm;

import com.sun.jersey.api.client.Client;

public class JerseyClient {
	private static Client client = null;
	
	private JerseyClient() {
		client = new Client();
	}

	public static Client getClient() {
		if (client == null) {
			new JerseyClient();
		}
		return client;
	}

}
