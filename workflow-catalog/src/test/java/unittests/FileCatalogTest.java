package unittests;

import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.workflowcatalog.Catalog;
import org.ow2.proactive.workflowcatalog.FileCatalog;

import java.io.File;

public class FileCatalogTest {

    private static final long REFRESH_PERIOD_MS = 20;
    private static Catalog catalog;
    private static File catalogPath = Files.createTempDir();

    @BeforeClass
    public static void beforeAll() throws Exception {
        catalog = new FileCatalog(catalogPath, REFRESH_PERIOD_MS);
        waitForUpdate();
    }

    @Test
    public void list_workflows_test() throws Exception {
        Assert.assertEquals(0, catalog.getWorkflows().size());
        insertNewWorkflow(catalogPath, "workflow1.xml");
        waitForUpdate();
        Assert.assertEquals(1, catalog.getWorkflows().size());
        insertNewWorkflow(catalogPath, "workflow2.xml");
        waitForUpdate();
        Assert.assertEquals(2, catalog.getWorkflows().size());
        removeOneWorkflow(catalogPath, "workflow1.xml");
        waitForUpdate();
        Assert.assertEquals(1, catalog.getWorkflows().size());
        removeOneWorkflow(catalogPath, "workflow2.xml");
        waitForUpdate();
        Assert.assertEquals(0, catalog.getWorkflows().size());
    }

    @Test
    public void list_workflows_subdirectories_test() throws Exception {
        String SEP = File.separator;
        Assert.assertEquals(0, catalog.getWorkflows().size());
        insertNewWorkflow(catalogPath, "d1" + SEP + "workflow4.xml");
        insertNewWorkflow(catalogPath, "d1" + SEP + "d2" + SEP + "workflow5.xml");
        waitForUpdate();
        Assert.assertEquals(2, catalog.getWorkflows().size());
        removeOneWorkflow(catalogPath, "d1" + SEP + "workflow4.xml");
        removeOneWorkflow(catalogPath, "d1" + SEP + "d2" + SEP + "workflow5.xml");
        waitForUpdate();
        Assert.assertEquals(0, catalog.getWorkflows().size());
    }

    @Test
    public void list_workflows_with_bad_files_test() throws Exception {
        Assert.assertEquals(0, catalog.getWorkflows().size());
        insertNewWorkflow(catalogPath, "workflow6.xml");
        insertNewWorkflow(catalogPath, "workflow7");
        waitForUpdate();
        Assert.assertEquals(1, catalog.getWorkflows().size());
        removeOneWorkflow(catalogPath, "workflow6.xml");
        removeOneWorkflow(catalogPath, "workflow7");
        waitForUpdate();
        Assert.assertEquals(0, catalog.getWorkflows().size());
    }


    private static void insertNewWorkflow(File catalogPath, String name) throws Exception {
        File f = new File(catalogPath, name);
        f.getParentFile().mkdirs();
        Files.touch(f);
    }

    private static void removeOneWorkflow(File catalogPath, String name) throws Exception {
        File f = new File(catalogPath, name);
        f.delete();
    }

    private static void waitForUpdate() {
        catalog.forceUpdate();
    }

}

