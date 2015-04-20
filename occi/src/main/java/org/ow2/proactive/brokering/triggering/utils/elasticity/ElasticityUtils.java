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


package org.ow2.proactive.brokering.triggering.utils.elasticity;
import com.google.gson.Gson;
import org.ow2.proactive.brokering.triggering.utils.MetadataUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ElasticityUtils {

    public static Vms extractVmsData(Map<String, String> args) {
        String metadata = MetadataUtils.extractMetadata(args);
        if (metadata == null || metadata.isEmpty())
            return new Vms();
        else
            return (new Gson()).fromJson(metadata, Vms.class);
    }

    public static void updataVmsData(Map<String, String> args, Vms vms) {
        String json = new Gson().toJson(vms);
        MetadataUtils.writeMetadataToServer(args, json);
    }

    public static enum VmStatus {
        BUILDING, READY, SHUTTINGDOWN, ERROR;
    }

    public static class Vm {
        public Long started;
        public Long expiral;
        public String platform;
        public VmStatus status;
    }

    public static class Vms extends HashMap<String, Vm> {

        public Map<String, Vm> getVms(VmStatus status) {
            Map<String, Vm> ret = new HashMap<String, Vm>();
            for (String vmName: keySet()) {
                Vm vm = get(vmName);
                if (vm.status == status)
                    ret.put(vmName, vm);
            }
            return ret;
        }

        public Map<String, Vm> getVms(VmStatus status, boolean areValid) {
            Map<String, Vm> ret = new HashMap<String, Vm>();
            long now = new Date().getTime();
            for (String vmName: keySet()) {
                Vm vm = get(vmName);
                boolean vmIsValid = now < vm.expiral;
                if (vm.status == status && vmIsValid == areValid)
                    ret.put(vmName, vm);
            }
            return ret;
        }


        public int getNroVmsShuttingDown() {
            return getVms(VmStatus.SHUTTINGDOWN).size();
        }

        public int getNroVmsBuilding() {
            return getVms(VmStatus.BUILDING, true).size();
        }

        public int getNroVmsBuildingExpired() {
            return getVms(VmStatus.BUILDING, false).size();
        }

        public Vm getOneRandomReadyVm() {
            Map<String, Vm> vms = getVms(VmStatus.READY);
            for (String vmName: vms.keySet())
                return vms.get(vmName);
            throw new RuntimeException("Cannot get random VM: empty set");
        }

    }

}
