String name = '${occi.compute.hostname}'

String ifconfig = "ifconfig eth0".execute().text.trim()
String hostname = "hostname".execute().text.trim()
String nufl = "/tmp/PA-AGENT_URL_$name-0"
String nodeurl = "cat $nufl".execute().text.trim()

println "IFCONFIG COMMAND: '$ifconfig'"
println "HOSTNAME COMMAND: '$hostname'"
println "NODE URL FILENAME:'$nufl'"
println "NODE URL COMMAND: '$nodeurl'"

if (nodeurl == null || nodeurl.isEmpty()) {
    println "RETRYING NODE URL FILENAME:'$nufl'"
    Thread.sleep(20 * 1000)
    nodeurl = "cat $nufl".execute().text.trim()
    println "RESULT: '$nodeurl'"
}


def regex = /(?s).*inet addr:(.*?)Bcast:.*/
def matcher = (ifconfig=~regex)

def ip = "unknownip"
if (matcher.matches()) {
    ip = matcher.group(1).trim()
} else {
    println("Cannot extract IP")
}

def json = new net.minidev.json.JSONObject();
json.put("occi.networkinterface.address", ip)
json.put("occi.compute.hostname", name.trim())
json.put("occi.compute.organization.name", hostname)
json.put("action.state", "done")
json.put("proactive.node.url", nodeurl)
json.put("occi.compute.state", "up")
json.put("occi.paas.application.endpoints", "http://" + ip + ":9200")
result = json.toJSONString()
println result
