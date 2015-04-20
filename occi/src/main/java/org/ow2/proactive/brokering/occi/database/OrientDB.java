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


package org.ow2.proactive.brokering.occi.database;

import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.tx.OTransaction;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.apache.log4j.Logger;
import org.ow2.proactive.brokering.occi.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OrientDB implements Database {

    private static String DB_DEFAULT_USERNAME = "admin";
    private static String DB_DEFAULT_PASSWORD = "admin";

    private static Logger logger = Logger.getLogger(OrientDB.class);

    private OObjectDatabaseTx db;

    public OrientDB (String name) {

        logger.info("Setting up DB");

        db = new OObjectDatabaseTx(generateDatabaseUrl(name));

        this.initialize();

        logger.info("DB correctly started");

    }

    @Override
    public void store(Resource resource) {
        try {
            logger.info("Resource to store in DB : " + resource.getUuid());
            Resource oldResource = loadAttached(resource.getUuid());
            Resource newResource = resource;
            updateResource(oldResource, newResource);
        } catch (Throwable e) {
            logger.error("Resource storage to DB failed", e);
        }
    }

    private void updateResource(Resource oldResource, Resource newResource) {
        try {
            db.begin(OTransaction.TXTYPE.NOTX); // TODO Switch to a valid transaction
                                                // mode for newer versions of OrientDB
            db.save(newResource);
            if (oldResource != null)
                db.delete(oldResource);
            db.commit();
        } catch (Exception e) {
            db.rollback();
            throw new RuntimeException("Could not safely store resource", e);
        }
    }

    @Override
    public void delete(String uuid) {
        try {
            Resource res = loadAttached(uuid);
            if (res != null)
                db.delete(res);
        } catch (Throwable e) {
            logger.error("Resource deletion from DB failed", e);
        }
    }

    @Override
    public Resource load(String uuid) {
        return db.detach(loadAttached(uuid), true);
    }

    private Resource loadAttached(String uuid) {
        OSQLSynchQuery<Resource> q =
                new OSQLSynchQuery<Resource>("select * from Resource where uuid = '" + uuid + "'");
        List<Resource> list = db.query(q);

        if (list.size() == 0) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            logger.warn("Repeated uuid: " + uuid);
            return list.get(0);
        }
    }

    @Override
    public List<Resource> getAllResources() {
        logger.info("Load all resources");
        List<Resource> list = new ArrayList<Resource>();
        for (Resource r : db.browseClass(Resource.class)) {
            list.add((Resource)db.detach(r, true));
        }
        return list;
    }

    @Override
    public void drop() {
        db.drop();
    }


    @Override
    public void close() {
        db.close();
    }

    private static String generateDatabaseUrl(String name) {
        String tmp = System.getProperty("java.io.tmpdir");
        String fs = File.separator;
        return "local:" + tmp + fs + name;
    }

    private void initialize() {
        boolean existedBefore;

        if (!db.exists()) {
            existedBefore = false;
            db.create();
        } else {
            existedBefore = true;
        }

        if (db.isClosed())
            db.open(DB_DEFAULT_USERNAME, DB_DEFAULT_PASSWORD);

        db.getEntityManager().registerEntityClass(Resource.class);

        if (!existedBefore) {
            Resource init = new Resource();
            init = db.save(init);
            db.delete(init);
        }
    }


}

