package remoteFileManage;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.BooleanUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remoteFileManage.commands.FileCommandFactory;
import remoteFileManage.commands.UploadFile;

/**
 * This servlet serve angular-filemanager call<br>
 * It's here for example purpose, to use it you have to put it in your java web
 * project<br>
 * Put in web.xml the servlet mapping
 * <br>
 * During initialization this servlet load some config properties from a file
 * called angular-filemanager.properties in your classes folder. You can set
 * repository.base.url <br>
 * Default values are : repository.base.url = "" 
 * <br>
 * 
 * @author Paolo Biavati https://github.com/paolobiavati
 * @author James Law https://github.com/Walsemaj
 */
public class RemoteFileManageServlet extends HttpServlet {

	private static final Logger LOG = LoggerFactory.getLogger(RemoteFileManageServlet.class);

	private static final long serialVersionUID = -8453502699403909016L;

	private String REPOSITORY_BASE_URL = "";
	
	private FileCommandFactory factory;

	@Override
	public void init() throws ServletException {
		super.init();

		// load from properties file REPOSITORY_BASE_URL and DATE_FORMAT, use default if missing
		InputStream propertiesFile = null;
		factory = FileCommandFactory.init();
		if(LOG.isDebugEnabled()) factory.listCommands();
		try {
			propertiesFile = getClass().getClassLoader().getResourceAsStream("angular-filemanager.properties");
			if (propertiesFile != null) {
				Properties prop = new Properties();
				// load a properties file from class path, inside static method
				prop.load(propertiesFile);
				REPOSITORY_BASE_URL = prop.getProperty("repository.base.url", REPOSITORY_BASE_URL);
				if (!"".equals(REPOSITORY_BASE_URL)
						&& !new File(getServletContext().getRealPath(REPOSITORY_BASE_URL)).isDirectory()) {
					throw new ServletException("invalid repository.base.url");
				}
				LOG.info("REPOSITORY_BASE_URL: " + REPOSITORY_BASE_URL);
			}
		} catch (Throwable t) {
		} finally {
			if (propertiesFile != null) {
				try {
					propertiesFile.close();
				} catch (IOException e) {
					LOG.error("initialization FileManageUtil.error: {}", e.getMessage(), e);
					throw new ServletException(e);
				}
			}
		}
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Catch download requests or view item
		LOG.debug(FileManageUtil.printHeaderParams(request));
		
		String action = request.getParameter("action");
		if(Action.download.name().equals(action))
		{
			boolean preview = BooleanUtils.toBoolean(request.getParameter("preview"));
			String path = request.getParameter("path");
	
//			File file = new File(getServletContext().getRealPath(REPOSITORY_BASE_URL), path);
			File file = new File(REPOSITORY_BASE_URL, path);
	
			if (!file.isFile()) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource Not Found");
				return;
			}
	
			// response.setHeader("Content-Type", getServletContext().getMimeType(imageName));
			response.setHeader("Content-Type", getServletContext().getMimeType(file.getName()));
			response.setHeader("Content-Length", String.valueOf(file.length()));
			response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
	
			FileInputStream input = null;
			BufferedOutputStream output = null;
			try {
	
				input = new FileInputStream(file);
				output = new BufferedOutputStream(response.getOutputStream());
				byte[] buffer = new byte[8192];
				for (int length = 0; (length = input.read(buffer)) > 0;) {
					output.write(buffer, 0, length);
				}
			} catch (Throwable t) {
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException logOrIgnore) {
					}
				}
				if (input != null) {
					try {
						input.close();
					} catch (IOException logOrIgnore) {
					}
				}
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			LOG.debug(FileManageUtil.printHeaderParams(request));
			// if request contains multipart-form-data
			if (ServletFileUpload.isMultipartContent(request)) {
				JSONObject responseJsonObject = UploadFile.apply(request, REPOSITORY_BASE_URL);

				response.setContentType("application/json");
				PrintWriter out = response.getWriter();
				out.print(responseJsonObject);
				out.flush();
			}
			// all other post request has jspn params in body
			else {
				fileOperation(request, response);
			}
		} catch (Throwable t) {
			FileManageUtil.seterror(t, response);
		}

	}

	private void fileOperation(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		JSONObject responseJsonObject = null;
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = request.getReader();
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
			LOG.info(sb.toString());
			JSONObject params = new JSONObject(sb.toString());

			Action action = Action.valueOf(params.getString("action")); //Throws IlllegalArgulmentException if not found
			
			responseJsonObject = factory.executeFileCommand(action, this.getServletContext(), REPOSITORY_BASE_URL, params);
			
			if (responseJsonObject == null) {
				responseJsonObject = FileManageUtil.error("generic FileManageUtil.error : responseJsonObject is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
			responseJsonObject = FileManageUtil.error(e.getMessage());
		}
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(responseJsonObject);
		out.flush();
	}
}
