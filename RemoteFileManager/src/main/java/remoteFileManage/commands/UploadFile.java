package remoteFileManage.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remoteFileManage.FileManageUtil;

public class UploadFile {
	
	private static final Logger LOG = LoggerFactory.getLogger(UploadFile.class);
	
	public static JSONObject apply(HttpServletRequest request, ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL) throws ServletException {
		// URL: $config.uploadUrl, Method: POST, Content-Type:
		// multipart/form-data
		// Unlimited file upload, each item will be enumerated as file-1,
		// file-2, etc.
		// [$config.uploadUrl]?destination=/public_html/image.jpg&file-1={..}&file-2={...}
		try {
			String destination = null;
			Map<String, InputStream> files = new HashMap<String, InputStream>();

			List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
			for (FileItem item : items) {
				LOG.debug("UploadFile: " + item.toString());

				if (item.isFormField()) {
					// Process regular form field (input
					// type="text|radio|checkbox|etc", select, etc).
					if ("destination".equals(item.getFieldName())) {
						destination = StringUtils.substringAfter(item.getString(), "/");
					}
				} else {
					// Process form file field (input type="file").
					files.put(item.getName(), item.getInputStream());
				}
			}
			if (files.size() == 0) {
				throw new Exception("file size  = 0");
			} else {
				for (Map.Entry<String, InputStream> fileEntry : files.entrySet()) {
					LOG.debug("Destination: " + REPOSITORY_BASE_URL + destination);
					LOG.debug("File Name: " + fileEntry.getKey());
					// File f = new File(getServletContext().getRealPath(REPOSITORY_BASE_URL + destination), fileEntry.getKey());
					File f = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL) + destination + "/" + fileEntry.getKey());
					if (!write(fileEntry.getValue(), f)) {
						throw new Exception("write FileManageUtil.error");
					}
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException("Cannot parse multipart request: DiskFileItemFactory.parseRequest", e);
		} catch (IOException e) {
			throw new ServletException("Cannot parse multipart request: item.getInputStream", e);
		} catch (Exception e) {
			throw new ServletException("Cannot write file", e);
		}
		return FileManageUtil.success(new JSONObject());
	}

	private static boolean write(InputStream inputStream, File f) {
		boolean ret = false;
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(f);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			ret = true;

		} catch (IOException e) {
			LOG.error("", e);

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					LOG.error("", e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					LOG.error("", e);
				}

			}
		}
		return ret;
	}
}
