package remoteFileManage;

import java.nio.file.attribute.AclEntry;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class FileManagePermission {
	
	private static final Logger LOG = Logger.getLogger(FileManagePermission.class);

	static final HashMap<String, String> UserClass;
	static {
		UserClass = new HashMap<String, String>();
		UserClass.put(FileManageConstant.BUILTIN_USER, FileManageConstant.PERMISSION_CLASS_OWNER);
		UserClass.put(FileManageConstant.AUTHENTICAED_USERS, FileManageConstant.PERMISSION_CLASS_GROUP);
		UserClass.put(FileManageConstant.EVERYONE, FileManageConstant.PERMISSION_CLASS_OTHER);
	};

	enum READ_ACCESS {
		READ_NAMED_ATTRS, READ_ACL, READ_ATTRIBUTES, READ_DATA
	};

	enum WRITE_ACCESS {
		WRITE_NAMED_ATTRS, WRITE_ACL, SYNCHRONIZE, APPEND_DATA, WRITE_DATA, WRITE_OWNER, WRITE_ATTRIBUTES, DELETE, DELETE_CHILD
	};

	enum EXECUTE_ACCESS {
		EXECUTE
	};

	public String convertACLtoPOSIX(List<AclEntry> AclEntries) {

		HashMap<String, ClassPermissionObject> FilePermissions = new HashMap<>();
		String permission = "";

		for (AclEntry entry : AclEntries) {
			LOG.debug("ACL Entry: '" + entry.principal().toString() + "'");

			//Current User & Bultin User go to the same Class Permission Object
			String entryName = StringUtils.endsWith(entry.principal().toString(), FileManageConstant.CURRENT_USER_SUFFIX) ? FileManageConstant.BUILTIN_USER: entry.principal().toString();
			
			LOG.debug("ACL Entry (Remapped): " + entryName);
			//Find permission values of Owner, Group, Other classes
			if (UserClass.get(entryName) != null)
			{
				if (FilePermissions.get(UserClass.get(entryName)) == null)
				{
					FilePermissions.put(UserClass.get(entryName), new ClassPermissionObject(entry));
				} else {
					// Append Info in the same group
					FilePermissions.put(UserClass.get(entryName), new ClassPermissionObject(FilePermissions.get(UserClass.get(entryName)), entry));
				}
			}
		}

		//Use group permission to represent owner class if owner class is not found
		if (FilePermissions.get(FileManageConstant.PERMISSION_CLASS_OWNER) == null && FilePermissions.get(FileManageConstant.PERMISSION_CLASS_GROUP) != null)
		{
			FilePermissions.put(FileManageConstant.PERMISSION_CLASS_OWNER, FilePermissions.get(FileManageConstant.PERMISSION_CLASS_GROUP));
		}
		
		permission += FilePermissions.get(FileManageConstant.PERMISSION_CLASS_OWNER)!=null? FilePermissions.get(FileManageConstant.PERMISSION_CLASS_OWNER).toString(): FileManageConstant.EMPTY_PERMISSION_CLASS;
		permission += FilePermissions.get(FileManageConstant.PERMISSION_CLASS_GROUP)!=null? FilePermissions.get(FileManageConstant.PERMISSION_CLASS_GROUP).toString(): FileManageConstant.EMPTY_PERMISSION_CLASS;
		permission += FilePermissions.get(FileManageConstant.PERMISSION_CLASS_OTHER)!=null? FilePermissions.get(FileManageConstant.PERMISSION_CLASS_OTHER).toString(): FileManageConstant.EMPTY_PERMISSION_CLASS;
		
		return permission;
	}

	class ClassPermissionObject {

		final boolean CanRead;
		final boolean CanWrite;
		final boolean CanExecute;

		public ClassPermissionObject(AclEntry aclEntry) {
			CanRead = aclEntry.permissions().stream().filter(AclEntryPermissions -> {
				try {
					LOG.debug("CanRead: " + AclEntryPermissions.toString());
					READ_ACCESS.valueOf(AclEntryPermissions.toString());
					return true;
				} catch (Exception e) {
					return false;
				}
			}).findAny().orElse(null) != null;

			CanWrite = aclEntry.permissions().stream().filter(AclEntryPermissions -> {
				try {
					LOG.debug("CanWrite: " + AclEntryPermissions.toString());
					WRITE_ACCESS.valueOf(AclEntryPermissions.toString());
					return true;
				} catch (Exception e) {
					return false;
				}
			}).findAny().orElse(null) != null;

			CanExecute = aclEntry.permissions().stream().filter(AclEntryPermissions -> {
				try {
					LOG.debug("CanExecute: " + AclEntryPermissions.toString());
					EXECUTE_ACCESS.valueOf(AclEntryPermissions.toString());
					return true;
				} catch (Exception e) {
					return false;
				}
			}).findAny().orElse(null) != null;
		}

		public ClassPermissionObject(ClassPermissionObject classPerms, AclEntry aclEntry) {

			if (!classPerms.getCanRead()) {
				this.CanRead = aclEntry.permissions().stream().filter(AclEntryPermissions -> {
					try {
						LOG.debug("CanRead: " + AclEntryPermissions.toString());
						READ_ACCESS.valueOf(AclEntryPermissions.toString());
						return true;
					} catch (Exception e) {
						return false;
					}
				}).findAny().orElse(null) == null;
			} else {
				this.CanRead = classPerms.getCanRead();
			}

			if (!classPerms.getCanWrite()) {
				this.CanWrite = aclEntry.permissions().stream().filter(AclEntryPermissions -> {
					try {
						LOG.debug("CanRead: " + AclEntryPermissions.toString());
						WRITE_ACCESS.valueOf(AclEntryPermissions.toString());
						return true;
					} catch (Exception e) {
						return false;
					}
				}).findAny().orElse(null) == null;
			} else {
				this.CanWrite = classPerms.getCanWrite();
			}

			if (!classPerms.getCanExecute()) {
				this.CanExecute = aclEntry.permissions().stream().filter(AclEntryPermissions -> {
					try {
						LOG.debug("CanRead: " + AclEntryPermissions.toString());
						EXECUTE_ACCESS.valueOf(AclEntryPermissions.toString());
						return true;
					} catch (Exception e) {
						return false;
					}
				}).findAny().orElse(null) == null;
			} else {
				this.CanExecute = classPerms.getCanExecute();
			}
		}

		@Override
		public String toString() {
			String PosixPermission = "";
			PosixPermission += CanRead ? FileManageConstant.READ : FileManageConstant.EMPTY;
			PosixPermission += CanWrite ? FileManageConstant.WRITE : FileManageConstant.EMPTY;
			PosixPermission += CanExecute ? FileManageConstant.EXECUTE : FileManageConstant.EMPTY;
			return PosixPermission;
		}

		public boolean getCanRead() {
			return this.CanRead;
		}

		public boolean getCanWrite() {
			return this.CanWrite;
		}

		public boolean getCanExecute() {
			return this.CanExecute;
		}
	}
}
