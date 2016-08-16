package remoteFileManage.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class Extract extends FileCommandBase{
			
	protected JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL,
			JSONObject params) throws Exception {
		// "item":"/temp/temp2/total.zip","destination":"/temp/temp2","action":"extract","folderName":"total2"
		try {
			String sourceFileName = params.getString("item").substring(1); //WEB-INF/{source.folder}/{file}.zip
			String sourceFolderName = params.getString("destination"); //WEB-INF/{source.folder}
			String targetFolderName = params.getString("folderName"); //{target.folder}
			String basePath = FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL);			

			LOG.debug("sourceFileName: " + sourceFileName);			
			LOG.debug("sourceFolderName: " + (CONTEXT_GET_REAL_PATH? sourceFolderName: sourceFolderName.substring(1)));
			LOG.debug("targetFolderName: " + targetFolderName);
			LOG.debug("basepath: " + basePath);
			
			File targetFolder = new File(basePath + (CONTEXT_GET_REAL_PATH? sourceFolderName: sourceFolderName.substring(1)) + "/", targetFolderName);
			
			if(!targetFolder.exists()) {
				if (!targetFolder.mkdir()) {
					throw new Exception("Can't create directory: " + targetFolder.getAbsolutePath());
				}
			}
			
			extractDir(basePath, sourceFileName, targetFolder.getAbsolutePath());
			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("extract", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
	
	private void extractDir(String basePath, String sourceFileName, String targetFolderName) throws Exception {
		
		LOG.info("Base Path: {}", basePath);
		LOG.info("Source File Name: {}", sourceFileName);
		LOG.info("Target Folder Name: {}", targetFolderName);
		
		byte[] buf = new byte[1024];
		ZipInputStream zipinputstream = null;
		ZipEntry zipentry;

		zipinputstream = new ZipInputStream(new FileInputStream(basePath + sourceFileName));

		zipentry = zipinputstream.getNextEntry();
		while (zipentry != null) {
			// for each entry to be extracted
			LOG.debug("Original Zipentry Name: " + zipentry.getName());
			String[] originalFileNameWithPath = StringUtils.split(zipentry.getName(), (SystemUtils.IS_OS_LINUX? '/': '\\'));
			createTargetSubFolders(originalFileNameWithPath, targetFolderName);
			
			int n;
			FileOutputStream fileoutputstream;
			File newFile = new File(new File(targetFolderName), zipentry.getName());
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
	}
	
	private void createTargetSubFolders(String[] subFolders, String targetFolderName) throws Exception {
		String baseFolder = targetFolderName + "/";
		int count = 0;
		for(String subFolderName: subFolders) {
			LOG.debug("Base Folder: " + baseFolder);
			LOG.debug("Create Sub Folder: " + subFolderName);
			if(count == (subFolders.length-1)) break; //Skip the target zip entry
			File subFolder = new File(baseFolder + subFolderName);
			if(!subFolder.exists()) {
				if (!subFolder.mkdir()) {
					throw new Exception("Can't create directory: " + subFolder.getAbsolutePath());
				}
			}
			baseFolder = baseFolder + subFolderName + "/";
			count++;
		}
	}
}
