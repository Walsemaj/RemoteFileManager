package remoteFileManage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public class FileManageUtil {
	
	public static void seterror(Throwable t, HttpServletResponse response) throws IOException {
		try {
			// { "result": { "FileManageUtil.success": false, "FileManageUtil.error": "message" } }
			JSONObject responseJsonObject = FileManageUtil.error(t.getMessage());
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			out.print(responseJsonObject);
			out.flush();
		} catch (Throwable x) {
			response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR, x.getMessage());
		}
	}
	
	public static JSONObject error(String msg) throws ServletException {
		try {
			// { "result": { "success": false, "error": "msg" } }
			JSONObject result = new JSONObject();
			result.put("success", false);
			result.put("error", msg);
			return new JSONObject().put("result", result);
		} catch (JSONException e) {
			throw new ServletException(e);
		}
	}

	public static JSONObject success(JSONObject params) throws ServletException {
		try {
			// { "result": { "success": true, "error": null } }
			JSONObject result = new JSONObject();
			result.put("success", true);
			result.put("error", (Object) null);
			return new JSONObject().put("result", result);
		} catch (JSONException e) {
			throw new ServletException(e);
		}
	}
	
	public static String printHeaderParams(HttpServletRequest request) throws IOException {
		String result = "\n";
		
		Enumeration headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
		  String headerName = (String)headerNames.nextElement();
		  result += "\tHeader Name - " + headerName + ", Value - " + request.getHeader(headerName);
		  result += "\n";
		}
		
		Enumeration params = request.getParameterNames(); 
		while(params.hasMoreElements()){
		 String paramName = (String)params.nextElement();
		 result += "\tParameter Name - "+paramName+", Value - "+request.getParameter(paramName);
		 result += "\n";
		}
		
		return result;
	}
}
