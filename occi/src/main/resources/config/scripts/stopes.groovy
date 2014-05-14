// Stop ElasticSearch
def p = "service elasticsearch stop".execute()
println "stderr: " + p.err.text
println "stdout: " + p.text
println "exitvalue: " + p.exitValue()

def json = new net.minidev.json.JSONObject();
result = json.toJSONString()
println result

