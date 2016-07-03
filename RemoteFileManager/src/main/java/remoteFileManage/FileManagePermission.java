package remoteFileManage;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class FileManagePermission {
	
	private static final Logger LOG = Logger.getLogger(FileManagePermission.class);

	static final Map<String, String> UserClass;
	static {
		UserClass = new LinkedHashMap<String, String>();
		UserClass.put(FileManageConstant.BUILTIN_USER, FileManageConstant.PERMISSION_CLASS_OWNER);
		UserClass.put(FileManageConstant.AUTHENTICAED_USERS, FileManageConstant.PERMISSION_CLASS_GROUP);
		UserClass.put(FileManageConstant.EVERYONE, FileManageConstant.PERMISSION_CLASS_OTHER);
	};

	enum READ_ACCESS {
		DEFAULT, READ_NAMED_ATTRS, READ_ACL, READ_ATTRIBUTES, READ_DATA;
	    public Set<AclEntryPermission> returnSetOfAclEntryPermission() {	        
	    	Set<AclEntryPermission> set = new HashSet<AclEntryPermission>();
	    	set.add(AclEntryPermission.READ_NAMED_ATTRS);
	    	set.add(AclEntryPermission.READ_ACL);
	    	set.add(AclEntryPermission.READ_ATTRIBUTES);
	    	set.add(AclEntryPermission.READ_DATA);
	    	return set;
	    }
	};

	enum WRITE_ACCESS {
		DEFAULT, WRITE_NAMED_ATTRS, WRITE_ACL, SYNCHRONIZE, APPEND_DATA, WRITE_DATA, WRITE_OWNER, WRITE_ATTRIBUTES, DELETE, DELETE_CHILD;
		
	    public Set<AclEntryPermission> returnSetOfAclEntryPermission() {
	    	Set<AclEntryPermission> set = new HashSet<AclEntryPermission>();
	    	set.add(AclEntryPermission.WRITE_NAMED_ATTRS);
	    	set.add(AclEntryPermission.WRITE_ACL);
	    	set.add(AclEntryPermission.SYNCHRONIZE);
	    	set.add(AclEntryPermission.APPEND_DATA);
	    	set.add(AclEntryPermission.WRITE_DATA);
	    	set.add(AclEntryPermission.WRITE_OWNER);
	    	set.add(AclEntryPermission.WRITE_ATTRIBUTES);
	    	set.add(AclEntryPermission.DELETE);
	    	set.add(AclEntryPermission.DELETE_CHILD);
	    	return set;
	    }
	};

	enum EXECUTE_ACCESS {
		DEFAULT, EXECUTE;
		
	    public Set<AclEntryPermission> returnSetOfAclEntryPermission() {
	    	Set<AclEntryPermission> set = new HashSet<AclEntryPermission>();
	    	set.add(AclEntryPermission.EXECUTE);
	    	return set;
	    }
	};

	public String convertACLtoPOSIX(List<AclEntry> AclEntries) {

		Map<String, ClassPermissionObject> FilePermissions = new HashMap<>();
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
	
	public void setPermissions(File file, String perms)  throws IOException {
		//Split "---------" into "---", "---", "---"
		String[] ps = perms.split("(?<=\\G...)");
		for(String code : ps) System.out.println("SetPermissions: " + code);
		
		
		Map<String, ClassPermissionObject> fileCurrentPermissions = new HashMap<>();
		AclFileAttributeView fileAttributeView = Files.getFileAttributeView(file.toPath(), AclFileAttributeView.class);
		
		List<AclEntry> aclEntries = fileAttributeView.getAcl();
		
		//Loop the as-is setting
		for (AclEntry entry : aclEntries) {
			//Assumption: In windows, a file belongs to either Builtin User or specific user
			LOG.info("ACL Principal: '" + entry.principal().toString() + "'");
		    LOG.info("ACL Permission: " + entry.permissions());

			//Current User & Bultin User go to the same Class Permission Object
			String entryName = StringUtils.endsWith(entry.principal().toString(), FileManageConstant.CURRENT_USER_SUFFIX) ? FileManageConstant.BUILTIN_USER: entry.principal().toString();
			
			LOG.debug("ACL Entry (Remapped): " + entryName);
			//Find permission values of Owner, Group, Other classes
			if (UserClass.get(entryName) != null)
			{
				if (fileCurrentPermissions.get(UserClass.get(entryName)) == null)
				{
					fileCurrentPermissions.put(UserClass.get(entryName), new ClassPermissionObject(entry));
				} else {
					// Append Info in the same group
					fileCurrentPermissions.put(UserClass.get(entryName), new ClassPermissionObject(fileCurrentPermissions.get(UserClass.get(entryName)), entry));
				}
				
			}
		}
		//Use group permission to represent owner class if owner class is not found
		if (fileCurrentPermissions.get(FileManageConstant.PERMISSION_CLASS_OWNER) == null && fileCurrentPermissions.get(FileManageConstant.PERMISSION_CLASS_GROUP) != null)
		{
			fileCurrentPermissions.put(FileManageConstant.PERMISSION_CLASS_OWNER, fileCurrentPermissions.get(FileManageConstant.PERMISSION_CLASS_GROUP));
		}

		fileCurrentPermissions.forEach((k, v) -> {
			System.out.println("File Current Permissions: " + k + " Principal: " + v.getPrincipalName() + " Permission Class: " + v.toString());
		});	
		
		int count = 0;
		Iterator<Entry<String, String>> it = UserClass.entrySet().iterator();
		while(it.hasNext())	
		{
			LOG.info("To-BE PERMISSION: " + ps[count]);
			Map.Entry<String, String> me = (Entry<String, String>) it.next();
			
			//Initial a default Class Permission Object if not found
			if (fileCurrentPermissions.get(me.getValue()) == null)
			{
				fileCurrentPermissions.put(me.getValue(), new ClassPermissionObject(me.getKey()));
			}
			
			//Initial new Class Permission Object
				try {
					LOG.info("Set new ACL for: " + fileCurrentPermissions.get(me.getValue()).getPrincipalName());
//					System.out.println("Full List After SetACL(): " + setNewACL(aclEntries, fileCurrentPermissions.get(me.getValue()), new ClassPermissionObject(me.getValue() + "_NEW", ps[count])));
					fileAttributeView.setAcl(setNewACL(aclEntries, fileCurrentPermissions.get(me.getValue()), new ClassPermissionObject(me.getValue() + "_NEW", ps[count])));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count++;
		};		
	}
	
	public  List<AclEntry> setNewACL(List<AclEntry> aclEntries, ClassPermissionObject currentPermissionObject, ClassPermissionObject newPermissionObject) throws IOException {
	    //BUILTIN\\Users (Alias) -> [0]"BUILTIN\\Users" [1]"(Alias)"
	    String principalName = StringUtils.split(currentPermissionObject.getPrincipalName(), "(")[0].trim();
	    
	    //RemoveAll since every access are on the same record
	    currentPermissionObject.getListOfAclEntry().stream().filter(entry -> entry !=null).forEach((entry) ->  {
	    		System.out.println("Contains Entry: " + aclEntries.contains(entry));
	    		System.out.println("Remove Entry: " + entry.principal());
	    		System.out.println("Remove Entry: " + entry.permissions());
	    		aclEntries.remove(entry);
	    	}
	    );
	    
		if(newPermissionObject.getCanRead())
		{
			System.out.println("UPDATE READ: " + principalName);
		    System.out.println("UPDATE READ: " + FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(principalName));
			System.out.println("UPDATE READ: " + READ_ACCESS.DEFAULT.returnSetOfAclEntryPermission());
			
			AclEntry.Builder builder = AclEntry.newBuilder();
		    builder.setPrincipal(FileSystems.getDefault()
		    		.getUserPrincipalLookupService().lookupPrincipalByName(principalName));
			builder.setType(AclEntryType.ALLOW);
			builder.setPermissions(READ_ACCESS.DEFAULT.returnSetOfAclEntryPermission());
			
			aclEntries.add(builder.build());
		}
		if(newPermissionObject.getCanWrite())
		{
			System.out.println("UPDATE WRITE: " + principalName);
		    System.out.println("UPDATE WRITE: " + FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(principalName));
			System.out.println("UPDATE WRITE: " + WRITE_ACCESS.DEFAULT.returnSetOfAclEntryPermission());
			
			AclEntry.Builder builder = AclEntry.newBuilder();
		    builder.setPrincipal(FileSystems.getDefault()
		    		.getUserPrincipalLookupService().lookupPrincipalByName(principalName));
			builder.setType(AclEntryType.ALLOW);
			builder.setPermissions(WRITE_ACCESS.DEFAULT.returnSetOfAclEntryPermission());
			aclEntries.add(builder.build());
		}
		if(newPermissionObject.getCanExecute())
		{
			System.out.println("UPDATE EXECUTE: " + principalName);
		    System.out.println("UPDATE EXECUTE: " + FileSystems.getDefault().getUserPrincipalLookupService().lookupPrincipalByName(principalName));
			System.out.println("UPDATE EXECUTE: " + EXECUTE_ACCESS.DEFAULT.returnSetOfAclEntryPermission());
			
			AclEntry.Builder builder = AclEntry.newBuilder();
		    builder.setPrincipal(FileSystems.getDefault()
		    		.getUserPrincipalLookupService().lookupPrincipalByName(principalName));
			builder.setType(AclEntryType.ALLOW);
			builder.setPermissions(EXECUTE_ACCESS.DEFAULT.returnSetOfAclEntryPermission());
			aclEntries.add(builder.build());
		}
		
		for (AclEntry aclEntry : aclEntries) {
	    System.out.println("Final Image PRINCIPAL: " + aclEntry.principal());
	    System.out.println("Final Image PERMISSIONS: " + aclEntry.permissions() + "\n");		
		}
		
		return aclEntries;
	}

	class ClassPermissionObject {

		private final String principalName;
		private final boolean CanRead;
		private final boolean CanWrite;
		private final boolean CanExecute;
		//Entries can have same principal but with other permission 
		private final List<AclEntry> aclEntries; 

		public ClassPermissionObject(String principalName) {
			this.principalName = principalName;
			this.CanRead = false;
			this.CanWrite = false;
			this.CanExecute = false;
			aclEntries = null;
		}
		
		public ClassPermissionObject(AclEntry aclEntry) {
			principalName = aclEntry.principal().toString();
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
			
			this.aclEntries = new LinkedList<AclEntry>();
			this.aclEntries.add(aclEntry);
		}

		public ClassPermissionObject(ClassPermissionObject classPerms, AclEntry aclEntry) {
			principalName = aclEntry.principal().toString();
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
			
			this.aclEntries = classPerms.getListOfAclEntry();
			this.aclEntries.add(aclEntry);
		}
		
		public ClassPermissionObject(String principalName, String perms) {
			this.principalName = principalName;
			this.CanRead = (perms.charAt(0) == FileManageConstant.READ);
			this.CanWrite = (perms.charAt(1) == FileManageConstant.EXECUTE);
			this.CanExecute = (perms.charAt(2) == FileManageConstant.WRITE);
			this.aclEntries = null;
		}

		@Override
		public String toString() {
			String PosixPermission = "";
			PosixPermission += CanRead ? FileManageConstant.READ : FileManageConstant.EMPTY;
			PosixPermission += CanWrite ? FileManageConstant.WRITE : FileManageConstant.EMPTY;
			PosixPermission += CanExecute ? FileManageConstant.EXECUTE : FileManageConstant.EMPTY;
			return PosixPermission;
		}
		
		public String getPrincipalName() {
			return this.principalName;
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
		
		public List<AclEntry> getListOfAclEntry() {
			return aclEntries == null? new LinkedList<>() : aclEntries;
		}
	}
}
