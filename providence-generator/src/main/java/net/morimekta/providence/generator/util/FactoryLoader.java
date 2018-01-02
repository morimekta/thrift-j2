package net.morimekta.providence.generator.util;

import net.morimekta.providence.generator.GeneratorFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

public class FactoryLoader {
    public FactoryLoader() {}

    public List<GeneratorFactory> getFactories(File path) {
        try {
            List<File> jars = findJarFiles(path);
            List<GeneratorFactory> factories = new ArrayList<>();
            for (File jar : jars) {
                URLClassLoader classLoader = getClassLoader(jar);
                factories.add(getFactory(classLoader));
            }
            return factories;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public GeneratorFactory getFactory(File file) {
        try {
            return getFactory(getClassLoader(file));
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private List<File> findJarFiles(File path) {
        List<File> out = new ArrayList<>();
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".jar")) {
                    out.add(file);
                }
            }
        }
        return out;
    }

    private URLClassLoader getClassLoader(File file) throws MalformedURLException {
        return URLClassLoader.newInstance(new URL[]{file.toURI().toURL()}, ClassLoader.getSystemClassLoader());
    }

    private GeneratorFactory getFactory(URLClassLoader classLoader)
            throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        URL url = classLoader.findResource("META-INF/MANIFEST.MF");
        Manifest manifest = new Manifest(url.openStream());
        String factoryClass = manifest.getMainAttributes().getValue("Providence-Generator-Factory");
        if (factoryClass == null) {
            throw new IllegalStateException("No Providence-Generator-Factory in " + Arrays.toString(classLoader.getURLs()));
        }
        Class klass = classLoader.loadClass(factoryClass);
        if (!GeneratorFactory.class.isAssignableFrom(klass)) {
            throw new IllegalStateException("Registered factory " + klass.getName() + " is not a GeneratorFactory.");
        }
        return (GeneratorFactory) klass.newInstance();
    }
}