package remoteFileManage.commands;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class Move implements FileCommand {
	
	public JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			JSONArray array = params.getJSONArray("items");

			for (Object temp : array) {
				String path = StringUtils.substringAfter(temp.toString(), "/");

				String newPath = StringUtils.substringAfter(params.getString("newPath"),
						params.getString("newPath").indexOf("//") >= 0 ? "//" : "/");

				String[] files = StringUtils.split(path, "/");
				String filename = files[files.length - 1];

				LOG.debug("copy from: {} to:{}", REPOSITORY_BASE_URL + path, REPOSITORY_BASE_URL + newPath + "/" + filename);

				File srcFile = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), path);
				File destFile = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), newPath + "/" + filename);
				if (srcFile.isFile()) {
					FileUtils.moveFile(srcFile, destFile);
				} else {
					FileUtils.moveDirectory(srcFile, destFile);
				}
			}
			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("copy", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
