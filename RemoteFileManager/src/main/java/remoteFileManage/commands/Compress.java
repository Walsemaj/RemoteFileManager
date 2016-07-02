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

public class Compress implements FileCommand {
	
	public JSONObject apply(ServletContext context, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		// "compressedFilename":"temp3","destination":"/temp","action":"compress","items":["/temp/temp2"]
		// "compressedFilename":"total","destination":"/temp/temp2","action":"compress","items":["/temp/temp2/Testing.notesairdropdocument","/temp/temp2/SVN
		// Readme.txt","/temp/temp2/Studies.txt"
		try {
			JSONArray path = params.getJSONArray("items"); // "/public_html/compressed.zip"
			String destination = params.getString("destination").substring(1);
			String compressedFilename = params.getString("compressedFilename");

			LOG.debug("compress path: {} destination: {}", path, destination);

			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(
					REPOSITORY_BASE_URL + destination + "/" + compressedFilename + ".zip");
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			byte data[] = new byte[FileManageConstant.BUFFER];
			// get a list of files from current directory

			for (Object obj : path) {
				File f = new File(REPOSITORY_BASE_URL + obj.toString().substring(1));
				LOG.debug("Compress file: " + REPOSITORY_BASE_URL + obj.toString().substring(1));
				if (f.isFile()) {
					LOG.debug("Adding: " + f.getName());
					FileInputStream fi = new FileInputStream(f);
					origin = new BufferedInputStream(fi, FileManageConstant.BUFFER);
					ZipEntry entry = new ZipEntry(f.toString());
					out.putNextEntry(entry);
					int count;
					while ((count = origin.read(data, 0, FileManageConstant.BUFFER)) != -1) {
						out.write(data, 0, count);
					}
					origin.close();
				} else {
					File[] files = f.listFiles();

					for (File file : files) {
						LOG.debug("Adding: " + file.getName());
						FileInputStream fi = new FileInputStream(file);
						origin = new BufferedInputStream(fi, FileManageConstant.BUFFER);
						ZipEntry entry = new ZipEntry(file.toString());
						out.putNextEntry(entry);
						int count;
						while ((count = origin.read(data, 0, FileManageConstant.BUFFER)) != -1) {
							out.write(data, 0, count);
						}
						origin.close();
					}
				}
			}
			out.close();

			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("compress", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
