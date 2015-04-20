/*
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 2013-2015 ActiveEon
 * 
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * $$ACTIVEEON_INITIAL_DEV$$
 */


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

