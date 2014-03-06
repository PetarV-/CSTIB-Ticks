package uk.ac.cam.cl.fjava.messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;

public class DynamicObjectInputStream extends ObjectInputStream {

	private HashMap<String, byte[]> hMap = new HashMap<String, byte[]>();
	
	private ClassLoader current = new ClassLoader(ClassLoader.getSystemClassLoader())
	{
		@Override
		protected Class<?> findClass(String className) throws ClassNotFoundException 
		{
			if (hMap.containsKey(className)) 
			{
				byte[] defn = hMap.get(className);
				Class<?> result = defineClass(className, defn, 0, defn.length);
				return result;
			} 
			else 
			{
				throw new ClassNotFoundException();
			}
		}
	};

	public DynamicObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
			ClassNotFoundException {
		try {
			return current.loadClass(desc.getName());
		}
		catch (ClassNotFoundException e) {
			return super.resolveClass(desc);
		}
	}

	public void addClass(final String name, final byte[] defn) {
		hMap.put(name, defn);
	}

}
