package remoteFileManage.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public class Extract implements FileCommand {
		
//	public JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
//		// "item":"/temp/temp2/total.zip","destination":"/temp/temp2","action":"extract","folderName":"total2"
	//{"action":"extract","item":"/WEB-INF/temp/testzip.zip","destination":"/WEB-INF/temp","folderName":"test_extract2"}
//		try {
//			String source = params.getString("item").substring(1);
//			String destination = params.getString("destination"); 
//			String sourceFile = params.getString("folderName"); 
//
//			BufferedOutputStream dest = null;
//			FileInputStream fis = new FileInputStream(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + source);
//			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
//			ZipEntry entry;
//			while ((entry = zis.getNextEntry()) != null) {
//				LOG.debug("Extracting: " + entry);
//				int count;
//				byte data[] = new byte[FileManageConstant.BUFFER];
//				// write the files to the disk
////				FileOutputStream fos = new FileOutputStream(entry.getName()); //TODO Change target directory as per request
//				FileOutputStream fos = new FileOutputStream(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + destination + "/" + sourceFile); //TODO Change target directory as per request
//				dest = new BufferedOutputStream(fos, FileManageConstant.BUFFER);
//				while ((count = zis.read(data, 0, FileManageConstant.BUFFER)) != -1) {
//					dest.write(data, 0, count);
//				}
//				dest.flush();
//				dest.close();
//			}
//			zis.close();
//
//			return FileManageUtil.success(params);
//		} catch (Exception e) {
//			LOG.error("extract", e);
//			return FileManageUtil.error(e.getMessage());
//		}
//	}
	
	public JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL,
			JSONObject params) throws Exception {
		// "item":"/temp/temp2/total.zip","destination":"/temp/temp2","action":"extract","folderName":"total2"
		try {
			String sourceFile = params.getString("item").substring(1);
			String sourceFolder = params.getString("destination");
			String targetFolder = params.getString("folderName");
			
			System.out.println("Source File (item): " + sourceFile);
			System.out.println("Source Folder (destination): " + sourceFolder);
			System.out.println("Target Folder (folderName): " + targetFolder);
			System.out.println("Zip Path: " + FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + sourceFile);

			File newDir = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + sourceFolder + "/", targetFolder);
			if (!newDir.mkdir()) {
				throw new Exception("Can't create directory: " + newDir.getAbsolutePath());
			}
			
			byte[] buf = new byte[1024];
			ZipInputStream zipinputstream = null;
			ZipEntry zipentry;

			zipinputstream = new ZipInputStream(new FileInputStream(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + sourceFile));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				System.out.println("Original Zipentry Name: " + zipentry.getName());
				String[] targetFileName = StringUtils.split(zipentry.getName(), '\\');
				System.out.println("TargetFileName: " + targetFileName[targetFileName.length-1]);
				String entryName = FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + sourceFolder + "/" + targetFolder + "/" + targetFileName[targetFileName.length-1];
//				entryName = entryName.replace('/', File.separatorChar);
//				entryName = entryName.replace('\\', File.separatorChar);
				System.out.println("entryname " + entryName);
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(entryName);
				if (zipentry.isDirectory()) {
					if (!newFile.mkdirs()) {
						break;
					}
					zipentry = zipinputstream.getNextEntry();
					continue;
				}

				fileoutputstream = new FileOutputStream(entryName);

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
