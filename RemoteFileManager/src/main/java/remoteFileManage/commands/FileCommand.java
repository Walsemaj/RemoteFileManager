package remoteFileManage.commands;

import javax.servlet.ServletContext;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface FileCommand {
	Logger LOG = LoggerFactory.getLogger(FileCommand.class);
	
	public JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception;
}
