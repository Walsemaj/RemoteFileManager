package remoteFileManage.commands;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class Delete extends FileCommandBase{

	protected JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			JSONArray array = params.getJSONArray("items");
			for (Object path : array) {
				LOG.debug("delete {}", path.toString());
//				File srcFile = new File(context.getRealPath(REPOSITORY_BASE_URL), path.toString());
				File srcFile = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), path.toString());
				if (!FileUtils.deleteQuietly(srcFile)) {
					throw new Exception("Can't delete: " + srcFile.getAbsolutePath());
				}
			}
			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("delete", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
