package net.ftlines.wicketsource.sourceopener.socket;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import net.ftlines.wicketsource.sourceopener.OpenEvent;

/**
 * Listens for requests to open a file in an eclipse editor, fires the open
 * event (in a separate thread), and responds to the http request with a 200 OK
 * and a very basic response message. Ignores all requests other than file-opens
 * of the appropriate type.
 * 
 * @author Jenny Brown
 * 
 */
public class SourceOpenerHttpd extends NanoHTTPD {
	private Logger log = Logger.getLogger("SourceOpenerHttpd");
	private HttpService httpService;
	private boolean requirePassword;
	private String password;

	public SourceOpenerHttpd(int port, File wwwroot, HttpService httpService) throws IOException {
		super(port, wwwroot);
		this.httpService = httpService;
	}

	public SourceOpenerHttpd(int port, boolean requirePassword, String password, HttpService httpService) throws IOException {
		super(port);
		this.requirePassword = requirePassword;
		this.password = password;
		this.httpService = httpService;
	}
	
	@Override
	public Response serve(String uri, String method, Properties header, Properties params, Properties files)
	{
		log.info("Received request for " + uri);

		try {
			if (this.requirePassword) {
				if (! this.password.equals(params.getProperty("p"))) {
					return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "Bad Request: Not authenticated.");
				}
			}
			OpenEvent event = new OpenEvent(uri, params);
			httpService.onOpenEvent(event);
			if ("y".equals(params.getProperty("jsonp"))) {
				return new Response(HTTP_OK, MIME_JS, "function SourceOpenerEclipseResult() { this.eclipseStatus='OK'; }");
			}
			return new Response(HTTP_OK, MIME_HTML, "<html><body>OK</body></html>");
		} catch (IllegalArgumentException ie) {
			return new Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "Bad Request: Invalid uri.");
		}
	}

	@Override
	public Response serveFile(String uri, Properties header, File homeDir, boolean allowDirectoryListing)
	{
		log.info("Received request for file serve : " + uri);
		return new Response(HTTP_FORBIDDEN, MIME_PLAINTEXT, "FORBIDDEN: No file serving.");
	}

	/**
	 * Overrides the parent class since I want more specific handling for
	 * testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		HttpService httpService = new HttpService();
		try {
			httpService.start();
			System.out.println("Now serving requests. Hit Enter to stop, or terminate the JVM manually.\n");
		} catch (IOException ioe) {
			System.err.println("Couldn't start server: " + ioe);
			System.exit(-1);
		}

		try {
			System.in.read();
			httpService.stop();
		} catch (Throwable t) {
		}

	}

}
