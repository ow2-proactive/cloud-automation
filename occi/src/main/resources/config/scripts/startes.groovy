import org.apache.commons.io.FileUtils

def elasticSearchMasterIp = '${paas.elasticsearch.master.ip}'
def elasticSearchNodeName = '${occi.compute.hostname}'

def SEP = File.separator
def configFilePath = SEP + "etc" + SEP + "elasticsearch" + SEP + "elasticsearch.yml"

println "Create/modify config file..."
def masterConfigLines = new StringBuilder()
masterConfigLines.append("node.name: ")
masterConfigLines.append(elasticSearchNodeName)
masterConfigLines.append("\n")
masterConfigLines.append("discovery.zen.ping.multicast.enabled: false")
masterConfigLines.append("\n")
masterConfigLines.append("discovery.zen.ping.unicast.hosts: [\"")
masterConfigLines.append(elasticSearchMasterIp)
masterConfigLines.append("\"]")
masterConfigLines.append("\n")

println "Writing to file: $configFilePath the following:"
println "---"
println "$masterConfigLines"
println "---"

def configFile = new File(configFilePath)

if (configFile.exists())
    FileUtils.writeStringToFile(configFile, masterConfigLines.toString());
else
    throw new RuntimeException("Config file not found: $configFilePath")

println "Start ElasticSearch..."

def p = "service elasticsearch start".execute()
println "stderr: " + p.err.text
println "stdout: " + p.text
p.waitFor()
if (p.exitValue() != 0) throw new RuntimeException("exitValue: " + p.exitValue());

Thread.sleep(1000 * 15)

println "Check status..."

p = "service elasticsearch status".execute()
println "stderr: " + p.err.text

def output1 = p.text
println "stdout: " + output1
p.waitFor()
if (p.exitValue() != 0 || output1.contains("not")) throw new RuntimeException("exitValue: " + p.exitValue());

def json = new net.minidev.json.JSONObject()
json.put("occi.paas.state", "up")
result = json.toJSONString()
println result

