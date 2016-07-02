package remoteFileManage.commands;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class SaveFile implements FileCommand {
	
	public JSONObject apply(ServletContext context, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		// save content
		try {
			String path = params.getString("item");
			String content = params.getString("content");
			LOG.debug("saveFile path: {} content: isNotBlank {}, size {}", path, StringUtils.isNotBlank(content),
					content != null ? content.length() : 0);

//			File srcFile = new File(context.getRealPath(REPOSITORY_BASE_URL), path);
			File srcFile = new File(REPOSITORY_BASE_URL, path);
			FileUtils.writeStringToFile(srcFile, content);

			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("saveFile", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
