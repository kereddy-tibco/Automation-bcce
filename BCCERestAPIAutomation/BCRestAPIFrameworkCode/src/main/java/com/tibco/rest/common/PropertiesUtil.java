package com.tibco.rest.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertiesUtil extends PropertiesConfiguration {
	public String resourcePath;
	public static PropertiesUtil prop;

	public PropertiesConfiguration props;
	private static InheritableThreadLocal<PropertiesUtil> instance = new InheritableThreadLocal<PropertiesUtil>() {

		protected PropertiesUtil childValue(PropertiesUtil parentValue) {
			return new PropertiesUtil(parentValue);
		};

		protected PropertiesUtil initialValue() {
			PropertiesUtil prop = new PropertiesUtil(System.getProperty("resources.dir", "resources"));

			try {
				addBundle(prop, new File("resources").getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return prop;
		};
	};

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public PropertiesUtil(PropertiesUtil props) {
		this();
		append(props);
	}

	public PropertiesUtil() {
		super();
		resourcePath = "./resources";
	}

	public PropertiesUtil(String... file) {
		this();
		load(file);
	}

	public PropertiesUtil(String file) {
		this();
		File dirList = new File(file);
		if (dirList.isDirectory()) {
			for (File f : dirList.listFiles())
				try {
					if(!f.isDirectory()) {
						load(f);
					}
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
		}
	}

	public boolean load(String... files) {
		boolean r = true;
		for (String file : files) {
			try {
				super.load(new FileInputStream(file));
			} catch (ConfigurationException e) {
				r = false;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return r;
	}

	public static PropertiesUtil getBundle() {
		return PropertiesUtil.instance.get();
	}

	public Object getPropertyValue(String key) {
		try {
			Object obj = this.getProperty(key);
			if (obj instanceof String) {
				return obj.toString();
			} else {
				return obj;
			}
		} catch (Exception e) {
			return null;
		}
	}

	public void setProperty(String key, String value) {
		super.setProperty(key, value);
	}

	public static void addBundle(PropertiesUtil p, String fileOrDir) {
		String localResources = p.getString("local.resources", "resources");
		File resourceFile = new File(fileOrDir);

		if (!localResources.equalsIgnoreCase(resourceFile.getAbsolutePath())) {
			p.addProperty("local.reasources", resourceFile.getAbsolutePath());
			if (resourceFile.exists()) {
				if (resourceFile.isDirectory()) {
					File[] propFiles = resourceFile.listFiles();

					for (File f : propFiles) {
						if(f.getAbsolutePath().contains("properties")) {
							PropertiesUtil p1 = new PropertiesUtil();
							try {
								p1.load(f);
							} catch (ConfigurationException e) {
								e.printStackTrace();
							}
							p.copy(p1);
						}
					}
				} else {
					try {
						p.load(fileOrDir);
					} catch (Exception e) {
						System.err.println("Unable to load " + resourceFile.getAbsolutePath() + "!");
					}
				}

				// add/override system properties
				Iterator<Object> iterator = System.getProperties().keySet().iterator();

				while (iterator.hasNext()) {
					Object key = iterator.next();
					p.setProperty(String.valueOf(key), System.getProperties().getProperty(String.valueOf(key)));
				}
			} else {
				System.err.println(resourceFile.getAbsolutePath() + " not exist!");
			}
		}
	}
}
