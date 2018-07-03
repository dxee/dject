package com.github.dxee.dject.grapher;

import com.github.dxee.dject.lifecycle.impl.AbstractLifecycleListener;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.grapher.graphviz.GraphvizGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

/**
 * An object that can generate a graph showing a Guice Dependency Injection graph.
 *
 * @see <a href="http://www.graphviz.org/">GraphViz</a>
 * @see <a href="https://github.com/google/guice/wiki/Grapher">Guice Grapher</a>
 */
@Singleton
public class Grapher extends AbstractLifecycleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Grapher.class);
    private final Injector injector;
    private final Set<Key<?>> roots;
    private final GrapherFilter grapherFilter;

    @Inject
    public Grapher(Injector injector, GrapherFilter grapherFilter) {
        this.injector = injector;
        // Scan all the injection bindings to find the root keys
        this.grapherFilter = grapherFilter;

        List<String> packages = grapherFilter.packages();
        if(null != packages && !packages.isEmpty()) {
            this.roots = Sets.newHashSetWithExpectedSize(packages.size());
            for (Key<?> k : injector.getAllBindings().keySet()) {
                Package classPackage = k.getTypeLiteral().getRawType().getPackage();
                if (classPackage == null) {
                    continue;
                }
                String packageName = classPackage.getName();
                for (String p : packages) {
                    if (packageName.startsWith(p)) {
                        roots.add(k);
                        break;
                    }
                }
            }
        } else {
            this.roots = null;
        }
    }

    /**
     * Writes the "Dot" graph to a given file.
     *
     * @param file file to write to
     */
    public void toFile(File file) throws Exception {
        PrintWriter out = new PrintWriter(file, "UTF-8");
        try {
            out.write(graph());
        }
        finally {
            Closeables.close(out, true);
        }
    }

    /**
     * Returns a String containing the "Dot" graph definition.
     *
     * @return the "Dot" graph definition
     */
    public String graph() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(baos);
        Injector localInjector = Guice.createInjector(new GraphvizModule());
        GraphvizGrapher renderer = localInjector.getInstance(GraphvizGrapher.class);
        renderer.setOut(out);
        renderer.setRankdir("TB");
        if (null != roots && !roots.isEmpty()) {
            renderer.graph(injector, roots);
        }
        renderer.graph(injector);
        return baos.toString("UTF-8");
    }

    @Override
    public void onStarted() {
        try {
            grapherFilter.dispatch(graph());
        } catch (Exception e) {
            LOGGER.error("Grapher could not generate dot file", e);
        }
    }
}
