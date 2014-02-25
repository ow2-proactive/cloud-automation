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

        public Vm getOneReadyVm() {
            Map<String, Vm> vms = getVms(VmStatus.READY);
            for (String vmName: vms.keySet())
                return vms.get(vmName);
            throw new RuntimeException("No ready Vm");
        }

    }

}
