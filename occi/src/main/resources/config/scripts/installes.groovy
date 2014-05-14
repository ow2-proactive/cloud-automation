import org.apache.commons.io.FileUtils

println "Download ElasticSearch..."
//def p = "curl -O 10.0.0.1:8090/elasticsearch-1.1.1.noarch.rpm".execute()
//def p = "curl -O https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.1.1.noarch.rpm".execute()
def p = "curl -O https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.1.1.deb".execute()

println "stderr: " + p.err.text
println "stdout: " + p.text
p.waitFor()
if (p.exitValue() != 0) throw new RuntimeException("exitValue: " + p.exitValue());

println "Install ElasticSearch..."
p = "dpkg -i elasticsearch-1.1.1.deb".execute()
//p = "rmp -i elasticsearch-1.1.1.noarch.rpm".execute()
println "stderr: " + p.err.text
println "stdout: " + p.text
p.waitFor()
if (p.exitValue() != 0) throw new RuntimeException("exitValue: " + p.exitValue());

println "Install OpenJDK-7..."

p = "apt-get install openjdk-7-jre-headless -y".execute()
println "stderr: " + p.err.text
println "stdout: " + p.text
p.waitFor()
if (p.exitValue() != 0) throw new RuntimeException("exitValue: " + p.exitValue());

def json = new net.minidev.json.JSONObject()
result = json.toJSONString()
println result

