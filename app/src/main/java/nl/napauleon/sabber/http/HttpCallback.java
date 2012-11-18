package nl.napauleon.sabber.http;

public interface HttpCallback {
	
	void handleResponse(String response);
	
	void handleTimeout();
	
	void handleError(String error);
}
