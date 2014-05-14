
println "Current category ID: ${occi.core.id}"

// Stop RMNode
def p = "/root/stop-node.sh".execute()
println "stderr: " + p.err.text
println "stdout: " + p.text
println "exitvalue: " + p.exitValue()

def json = new net.minidev.json.JSONObject();
result = json.toJSONString()
println result

