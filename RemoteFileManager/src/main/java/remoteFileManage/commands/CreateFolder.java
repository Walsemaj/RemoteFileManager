package remoteFileManage.commands;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remoteFileManage.FileManageUtil;

public class CreateFolder implements FileCommand {
		
	public JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			String path = params.getString("newPath");

			String[] files = StringUtils.split(path, "/");
			String newFile = files[files.length - 1];
			// Skip the first slash e.g. /File1/File2 -> File1/File2
			String originalFile = StringUtils.substringBefore(path.substring(1), newFile);

			LOG.debug("addFolder path: {} name: {}", originalFile, newFile);
			// File newDir = new File(getServletContext().getRealPath(REPOSITORY_BASE_URL + originalFile), newFile);
			File newDir = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + originalFile, newFile);

			if (!newDir.mkdir()) {
				throw new Exception("Can't create directory: " + newDir.getAbsolutePath());
			}
			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("addFolder", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
