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
		// "item":"/temp/temp2/total.zip","destination":"/temp/temp2","action":"extract","folderName":"total2"
		try {
			String sourceFileName = params.getString("item").substring(1); //WEB-INF/{source.folder}/{file}.zip
			String sourceFolderName = params.getString("destination"); //WEB-INF/{source.folder}
			String targetFolderName = params.getString("folderName"); //{target.folder}
			String basePath = FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL);
			
			System.out.println("Source File (item): " + sourceFileName);
			System.out.println("Source Folder (destination): " + sourceFolderName);
			System.out.println("Target Folder (folderName): " + targetFolderName);
			System.out.println("Zip File Path: " + basePath + sourceFileName); //{web.project.root}/WEB-INF/{source.folder}

			File targetFolder = new File(basePath + (CONTEXT_GET_REAL_PATH? sourceFolderName: sourceFolderName.substring(1)) + "/", targetFolderName);
			
			if(!targetFolder.exists()) {
				if (!targetFolder.mkdir()) {
					throw new Exception("Can't create directory: " + targetFolder.getAbsolutePath());
				}
//				targetFolder.setReadable(true);
//				targetFolder.setWritable(true);
//				
//				String[] data = {targetFolder.getAbsolutePath()};
//				JSONArray ja = new JSONArray().put(data);
//				JSONObject items = new JSONObject()
//						.put("perms", "rwxrwxrwx").put("permsCode", "777").put("recursive", "false").put("items", ja);
//				
//				new ChangePermission().apply(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL, items);
			}
			
			byte[] buf = new byte[1024];
			ZipInputStream zipinputstream = null;
			ZipEntry zipentry;

			zipinputstream = new ZipInputStream(new FileInputStream(basePath + sourceFileName));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				LOG.debug("Original Zipentry Name: " + zipentry.getName());
				String[] originalFileNameWithPath = StringUtils.split(zipentry.getName(), '\\');
				String targetFileName = originalFileNameWithPath[originalFileNameWithPath.length-1]; //File might be zipped with absolute path
				
				String targetFileNameWithTargetDestination = basePath + sourceFolderName + "/" + targetFolder + "/" + targetFileName;
				
				targetFileNameWithTargetDestination = targetFileNameWithTargetDestination.replace('/', File.separatorChar);
				targetFileNameWithTargetDestination = targetFileNameWithTargetDestination.replace('\\', File.separatorChar);				
				LOG.debug("Target File Name With Target Destination: " + targetFileNameWithTargetDestination);
				
				originalFileNameWithPath = StringUtils.split(targetFileNameWithTargetDestination, '\\');
				targetFileNameWithTargetDestination = originalFileNameWithPath[originalFileNameWithPath.length-1];
				LOG.info("Extracting file to: " + targetFileNameWithTargetDestination);
				
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(targetFolder, targetFileNameWithTargetDestination);
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
