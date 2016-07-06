package remoteFileManage.commands;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class Copy implements FileCommand {

	public JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			JSONArray array = params.getJSONArray("items");
			// Scenario #1 copy a file into a directory
			// items /temp/test2.txt
			// newPath //temp/temp3
			// singleFilename test2.txt

			// Scenario #2 copy a file into current directory
			// items /temp/test2.txt
			// newPath /temp
			// singleFilename test3.txt

			// Scenario #3 copy several files
			// items ["/temp/temp3/test2.txt","/temp/temp3/test3.txt"]
			// newPath //temp/temp3/subfolder
			for (Object temp : array) {
				String path = StringUtils.substringAfter(temp.toString(), "/");
				String filename;

				try {
					filename = params.getString("singleFilename");
				} catch (Exception e) {
					String[] files = StringUtils.split(path, "/");
					filename = files[files.length - 1];
				}

				String newPath = StringUtils.substringAfter(params.getString("newPath"),
						params.getString("newPath").indexOf("//") >= 0 ? "//" : "/");
				LOG.debug("copy from: {} to: {}", REPOSITORY_BASE_URL + path, REPOSITORY_BASE_URL + newPath + "/" + filename);

				// File srcFile = new File(getServletContext().getRealPath(REPOSITORY_BASE_URL), path);
				// File destFile = new File(getServletContext().getRealPath(REPOSITORY_BASE_URL), newPath + "/" + params.getString("singleFilename"));
				File srcFile = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), path);
				File destFile = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), newPath + "/" + filename);
				if (srcFile.isFile()) {
					FileUtils.copyFile(srcFile, destFile);
				} else {
					FileUtils.copyDirectory(srcFile, destFile);
				}
			}
			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("copy", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
