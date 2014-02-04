package unittests;

import com.google.common.io.Files;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.proactive.workflowcatalog.Catalog;

import java.io.File;

public class CatalogTest {

    private static final long REFRESH_PERIOD_MS = 20;
    private static Catalog catalog;
    private static File catalogPath = Files.createTempDir();

    @BeforeClass
    public static void beforeAll() throws Exception {
        catalog = new Catalog(catalogPath, REFRESH_PERIOD_MS);
        waitForUpdate();
    }

    @Test
    public void listWorkflows_Test() throws Exception {

        Assert.assertTrue(catalog.getWorkflows().size() == 0);

        insertNewWorkflow(catalogPath, "workflow1.xml");
        insertNewWorkflow(catalogPath, "workflow2.xml");
        waitForUpdate();

        Assert.assertTrue(catalog.getWorkflows().size() == 2);

        insertNewWorkflow(catalogPath, "workflow3.xml");
        waitForUpdate();

        Assert.assertTrue(catalog.getWorkflows().size() == 3);

        removeOneWorkflow(catalogPath, "workflow1.xml");
        removeOneWorkflow(catalogPath, "workflow2.xml");
        removeOneWorkflow(catalogPath, "workflow3.xml");
        waitForUpdate();

        Assert.assertTrue(catalog.getWorkflows().size() == 0);

    }

    private static void insertNewWorkflow(File catalogPath, String name) throws Exception {
        File f = new File(catalogPath, name);
        Files.touch(f);
    }

    private static void removeOneWorkflow(File catalogPath, String name) throws Exception {
        File f = new File(catalogPath, name);
        f.delete();
    }

    private static void waitForUpdate() {
        try {
            Thread.sleep(REFRESH_PERIOD_MS * 10);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

}

