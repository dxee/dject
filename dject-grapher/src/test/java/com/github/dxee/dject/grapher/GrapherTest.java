package com.github.dxee.dject.grapher;

import com.github.dxee.dject.Dject;
import com.github.dxee.dject.DjectBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GrapherTest {
    @Test
    public void testGenerateFullGraph() {
        final AtomicBoolean injectCalled = new AtomicBoolean(false);
        final AtomicBoolean afterInjectorCalled = new AtomicBoolean(false);

        try(Dject injector = DjectBuilder
                .fromModules(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(F1.class);
                        install(new AbstractModule() {
                            @Override
                            protected void configure() {
                                bind(F2.class);
                            }
                        });
                    }
                }, new GrapherModule()).createInjector(Stage.PRODUCTION)) {
            File f = new File("digraph.dot");
            Assert.assertTrue(f.exists());
            f.delete();
        }
    }


    @Test
    public void testSpecifyPackageGraph() {
        final AtomicBoolean injectCalled = new AtomicBoolean(false);
        final AtomicBoolean afterInjectorCalled = new AtomicBoolean(false);

        try(Dject injector = DjectBuilder
                .fromModules(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(F1.class);
                        install(new AbstractModule() {
                            @Override
                            protected void configure() {
                                bind(F2.class);
                            }
                        });
                        bind(GrapherFilter.class).toInstance(new FileGrapherFilter() {
                            @Override
                            public List<String> packages() {
                                List<String> packages = new ArrayList<>();
                                packages.add("org.dxee.dject.grapher");
                                return packages;
                            }

                            @Override
                            public String filePath() {
                                return "/tmp/digraph.dot";
                            }
                        });
                    }
                }, new GrapherModule()).createInjector(Stage.PRODUCTION)) {
            File f = new File("/tmp/digraph.dot");
            Assert.assertTrue(f.exists());
            f.delete();
        }
    }

    @Singleton
    private static class F1 {

    }

    @Singleton
    private static class F2 {
        private F1 f1;

        @Inject
        public F2(F1 f1) {
            this.f1 = f1;
        }
    }
}
