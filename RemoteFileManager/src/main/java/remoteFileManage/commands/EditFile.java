package remoteFileManage.commands;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class EditFile implements FileCommand {
	
	public JSONObject apply(ServletContext context, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		// get content
		try {
			String path = params.getString("item");
			LOG.debug("editFile path: {}", path);

//			File srcFile = new File(context.getRealPath(REPOSITORY_BASE_URL), path);
			File srcFile = new File(REPOSITORY_BASE_URL, path);
			String content = FileUtils.readFileToString(srcFile);

			return new JSONObject().put("result", content);
		} catch (Exception e) {
			LOG.error("editFile", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
