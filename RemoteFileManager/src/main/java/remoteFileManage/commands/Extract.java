package remoteFileManage.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletContext;

import org.json.JSONObject;

import remoteFileManage.FileManageConstant;
import remoteFileManage.FileManageUtil;

public class Extract implements FileCommand {
		
	public JSONObject apply(ServletContext context, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		// "item":"/temp/temp2/total.zip","destination":"/temp/temp2","action":"extract","folderName":"total2"
		try {
			String source = params.getString("item").substring(1);
			String destination = params.getString("destination"); 
			String sourceFile = params.getString("folderName"); 

			BufferedOutputStream dest = null;
			FileInputStream fis = new FileInputStream(REPOSITORY_BASE_URL + source);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				LOG.debug("Extracting: " + entry);
				int count;
				byte data[] = new byte[FileManageConstant.BUFFER];
				// write the files to the disk
				FileOutputStream fos = new FileOutputStream(entry.getName()); //TODO Change target directory as per request
				dest = new BufferedOutputStream(fos, FileManageConstant.BUFFER);
				while ((count = zis.read(data, 0, FileManageConstant.BUFFER)) != -1) {
					dest.write(data, 0, count);
				}
				dest.flush();
				dest.close();
			}
			zis.close();

			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("extract", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
}
