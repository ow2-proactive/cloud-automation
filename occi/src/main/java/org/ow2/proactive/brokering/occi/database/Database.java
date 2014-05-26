package org.ow2.proactive.brokering.occi.database;

import org.ow2.proactive.brokering.occi.Resource;

import java.util.List;

public interface Database {
    public void store(Resource resource);
    public List<Resource> getAllResources();
    public Resource load(String uuid);
    public void delete(String uuid);
    public void drop();
    public void close();
}

