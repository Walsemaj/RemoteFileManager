package remoteFileManage.commands;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class Rename extends FileCommandBase{
	
	protected JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			String path = params.getString("item");
			String newpath = params.getString("newItemPath");

//			File srcFile = new File(context.getRealPath(REPOSITORY_BASE_URL), path);
//			File destFile = new File(context.getRealPath(REPOSITORY_BASE_URL), newpath);
			
			File srcFile = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), path);
			File destFile = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), newpath);			
			if (srcFile.isFile()) {
				FileUtils.moveFile(srcFile, destFile);
			} else {
				FileUtils.moveDirectory(srcFile, destFile);
			}
			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("rename", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
