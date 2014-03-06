package uk.ac.cam.pv273.fjava.tick2star;

import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.net.SocketPermission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.PropertyPermission;

public class SafeObjectInputStream extends ObjectInputStream 
{
	SecureClassLoader current = (SecureClassLoader)ClassLoader.getSystemClassLoader();

	public SafeObjectInputStream(InputStream in) throws IOException 
	{
		super(in);
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException 
	{
		try 
		{
			return current.loadClass(desc.getName());
		}
		catch (ClassNotFoundException e) 
		{
			return super.resolveClass(desc);
		}
	}

	public void addClass(final String name, final byte[] defn) 
	{	
		PropertyPermission pPerm = new PropertyPermission("user.home", "read");
		FilePermission fPerm = new FilePermission(System.getProperty("user.home"), "read");
		SocketPermission sPerm = new SocketPermission("www.cam.ac.uk:80", "connect");
		Permissions pColl = new Permissions();
		pColl.add(pPerm);
		pColl.add(fPerm);
		pColl.add(sPerm);
		
		final ProtectionDomain pDomain = new ProtectionDomain(null, pColl);
		
		current = new SecureClassLoader(current) 
		{
			@Override
			protected Class<?> findClass(String className) throws ClassNotFoundException 
			{
				if (className.equals(name)) 
				{	
					Class<?> result = defineClass(name, defn, 0, defn.length, pDomain);
					return result;
				} 
				else 
				{
					throw new ClassNotFoundException();
				}
			}
		};
	}
}
