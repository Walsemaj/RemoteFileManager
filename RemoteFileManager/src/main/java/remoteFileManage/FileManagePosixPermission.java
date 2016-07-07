package remoteFileManage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

public class FileManagePosixPermission {
	
	private static final Logger LOG = Logger.getLogger(FileManagePosixPermission.class); 
	
	public void setPermissions(File file, String perms)  throws IOException, ClassNotFoundException {
		//Split "---------" into "---", "---", "---"
		String[] psList = perms.split("(?<=\\G...)");
		if(LOG.isDebugEnabled()) for(String code : psList) LOG.debug("SetPermissions: " + code);
		
		Set<PosixFilePermission> posixFilePerms = new HashSet<PosixFilePermission>();
		for(String ps : psList)
		{
			Iterator<String> it = FileManageConstant.PERMISSION_GROUP_LIST.iterator();
			String permsGroup;
			while(it.hasNext())
			{
				permsGroup = it.next();
				if(ps.charAt(0) == FileManageConstant.READ);
					posixFilePerms.add(PosixFilePermission.valueOf(permsGroup + FileManageConstant.POSIX_PERMISSION_READ_SUFFIX));
				if(ps.charAt(1) == FileManageConstant.WRITE);
					posixFilePerms.add(PosixFilePermission.valueOf(permsGroup + FileManageConstant.POSIX_PERMISSION_WRITE_SUFFIX));
				if(ps.charAt(2) == FileManageConstant.EXECUTE);
					posixFilePerms.add(PosixFilePermission.valueOf(permsGroup + FileManageConstant.POSIX_PERMISSION_EXECUTE_SUFFIX));
			}	 
		}		    
		Files.setPosixFilePermissions(file.toPath(), posixFilePerms);
	}
}
