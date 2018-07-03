package com.github.dxee.dject.grapher;

import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Singleton
public class FileGrapherFilter implements GrapherFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileGrapherFilter.class);

    @Override
    public List<String> packages() {
        return null;
    }

    protected String filePath() {
        return "digraph.dot";
    }

    @Override
    public void dispatch(String dot) {
        toFile(dot);
    }

    /**
     * Write string to file
     *
     * @param dot dot string for write
     */
    public void toFile(String dot) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new File(filePath()), "UTF-8");
            out.write(dot);
        } catch (Exception e) {
            LOGGER.error("Could not save dot file", e);
        } finally {
            try {
                Closeables.close(out, true);
            } catch (IOException e) {
                LOGGER.error("Could not close file", e);
            }
        }
    }
}
