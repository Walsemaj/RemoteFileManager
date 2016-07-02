package remoteFileManage.commands;

import javax.servlet.ServletContext;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface FileCommand {
	Logger LOG = LoggerFactory.getLogger(FileCommand.class);
	
	public JSONObject apply(ServletContext context, String REPOSITORY_BASE_URL, JSONObject params) throws Exception;
}
