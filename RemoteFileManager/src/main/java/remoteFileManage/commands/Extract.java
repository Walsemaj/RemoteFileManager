package remoteFileManage.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class Extract implements FileCommand {
			
	public JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL,
			JSONObject params) throws Exception {
		try {
			String sourceFileName = params.getString("item").substring(1); //WEB-INF/{source.folder}/{file}.zip
			String sourceFolderName = params.getString("destination"); //WEB-INF/{source.folder}
			String targetFolderName = params.getString("folderName"); //{target.folder}
			String basePath = FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL);			

			LOG.debug("sourceFileName: {}", sourceFolderName);			
			LOG.debug("sourceFolderName: {}", (CONTEXT_GET_REAL_PATH? sourceFolderName: sourceFolderName.substring(1)));
			LOG.debug("targetFolderName: {}", targetFolderName);
			LOG.debug("basepath: {}", basePath);
			
			File targetFolder = new File(basePath + (CONTEXT_GET_REAL_PATH? sourceFolderName: sourceFolderName.substring(1)) + "/", targetFolderName);
			
			if(!targetFolder.exists()) {
				if (!targetFolder.mkdir()) {
					throw new Exception("Can't create directory: " + targetFolder.getAbsolutePath());
				}
			}
			
			byte[] buf = new byte[1024];
			ZipInputStream zipinputstream = null;
			ZipEntry zipentry;

			zipinputstream = new ZipInputStream(new FileInputStream(basePath + sourceFileName));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				LOG.debug("Original Zipentry Name: {}", zipentry.getName());
				String[] originalFileNameWithPath = StringUtils.split(zipentry.getName(), '\\');
				originalFileNameWithPath = StringUtils.split(originalFileNameWithPath[originalFileNameWithPath.length-1], '/'); //For Linux
				String targetFileName = originalFileNameWithPath[originalFileNameWithPath.length-1]; //File might be zipped with absolute path
				LOG.debug("New Zipentry Name: {}", targetFileName);
				
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(targetFolder, targetFileName);
				if (zipentry.isDirectory()) {
					if (!newFile.mkdirs()) {
						break;
					}
					zipentry = zipinputstream.getNextEntry();
					continue;
				}
				fileoutputstream = new FileOutputStream(newFile);
				while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
					fileoutputstream.write(buf, 0, n);
				}
				fileoutputstream.close();
				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();
			} // while

			zipinputstream.close();

			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("extract", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
