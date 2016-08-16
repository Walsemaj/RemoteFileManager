package remoteFileManage.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;

import org.json.JSONArray;
import org.json.JSONObject;

import remoteFileManage.FileManageConstant;
import remoteFileManage.FileManageUtil;

public class Compress extends FileCommandBase{
	
	protected JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		// "compressedFilename":"temp3","destination":"/temp","action":"compress","items":["/temp/temp2"]
		// "compressedFilename":"total","destination":"/temp/temp2","action":"compress","items":["/temp/temp2/Testing.notesairdropdocument","/temp/temp2/SVN
		// Readme.txt","/temp/temp2/Studies.txt"
		try {
			JSONArray path = params.getJSONArray("items"); // "/public_html/compressed.zip"
			String destination = params.getString("destination").substring(1);
			String compressedFilename = params.getString("compressedFilename");

			LOG.debug("compress path: {} destination: {}", path, destination);

			FileOutputStream dest = new FileOutputStream(
					FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + destination + "/" + compressedFilename + ".zip");
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			for (Object obj : path) {
				String pathName = FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + obj.toString().substring(1);
				LOG.debug("Compress file: " + pathName);
				zipDir(pathName, out);
			}

			out.close();

			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("compress", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
	
	private void zipDir(String dir2zip, ZipOutputStream zos) throws Exception {
		LOG.debug("zipDir: {}", dir2zip);
		File zipFile = new File(dir2zip);
		String[] dirList = zipFile.list();
		byte[] readBuffer = new byte[2156];
		int bytesIn = 0;
		
		// Empty Folder		
		if(dirList.length == 0 && zipFile.isDirectory())
			zos.putNextEntry(new ZipEntry(zipFile.getPath() + "/"));
		
		for (int i = 0; i < dirList.length; i++) {
			File f = new File(zipFile, dirList[i]);
			if (f.isDirectory()) {
				// if the File object is a directory, call this
				// function again to add its content recursively
				String filePath = f.getPath();
				zipDir(filePath, zos);
				continue;
			}
			FileInputStream fis = new FileInputStream(f);
			ZipEntry anEntry = new ZipEntry(f.getPath());
			zos.putNextEntry(anEntry);
			while ((bytesIn = fis.read(readBuffer)) != -1) {
				zos.write(readBuffer, 0, bytesIn);
			}
			fis.close();
		}
	}
}
