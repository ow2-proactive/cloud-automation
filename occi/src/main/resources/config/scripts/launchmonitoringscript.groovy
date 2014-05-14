def content = []

println "Creating monitoring script..."

content << '#!/bin/python'
content << ''
content << 'import os, sys, json, time;'
content << 'from subprocess import Popen, PIPE;'
content << ''
content << 'while True:'
content << '        command1 = ["curl", "-s", "http://localhost:9200/_nodes"]'
content << '        command2 = ["curl", "-s", "http://localhost:9200/_status"]'
content << '        filename1 = "/tmp/pflags/esnodes"'
content << '        filename2 = "/tmp/pflags/esstatus"'
content << '        interval = 5'
content << ''
content << '        process1 = Popen(command1, stdout=PIPE);'
content << '        (stdout1, stderr1) = process1.communicate();'
content << '        exitcode1 = process1.wait();'
content << ''
content << '        process2 = Popen(command2, stdout=PIPE);'
content << '        (stdout2, stderr2) = process2.communicate();'
content << '        exitcode2 = process2.wait();'
content << ''
content << '        try:'
content << '            obj = json.loads(stdout1);'
content << '            for nameid in obj["nodes"]:'
content << '                hname = obj["nodes"][nameid]["name"];'
content << '                obj["nodes"][nameid] = {};'
content << '                obj["nodes"][nameid]["name"] = hname;'
content << ''
content << '            content1 = json.dumps(obj)'
content << ''
content << '        except: '
content << '            content1 = ""'
content << '            pass'
content << ''
content << '        try:'
content << '            obj = json.loads(stdout2)'
content << '            for nameid in obj["indices"]:'
content << '                obj["indices"][nameid] = {};'
content << '                obj["indices"][nameid]["name"] = nameid;'
content << ''
content << '            content2 = json.dumps(obj)'
content << ''
content << '        except: '
content << '            content2 = ""'
content << '            pass'
content << ''
content << '        if not os.path.exists(os.path.dirname(filename1)):'
content << '            os.makedirs(os.path.dirname(filename1))'
content << ''
content << '        print "Writing :" + content1 + " to: " + filename1'
content << '        print "Writing :" + content2 + " to: " + filename2'
content << ''
content << '        f = open(filename1, "w")'
content << '        f.write(content1)'
content << ''
content << '        g = open(filename2, "w")'
content << '        g.write(content2)'
content << ''
content << '        time.sleep(interval)'
content << ''

new File("/tmp/monitoring.py").withWriter { out -> content.each { out.println it } }

content = []

println "Creating monitoring service script..."

content << '#!/bin/bash'
content << ''
content << 'WORK_DIR="/tmp/"'
content << 'DAEMON="/usr/bin/python"'
content << 'ARGS="/tmp/monitoring.py"'
content << 'PIDFILE="/tmp/monitoring.pid"'
content << 'USER="root"'
content << ''
content << 'case "$1" in'
content << '  start)'
content << '    echo "Starting server"'
content << '    mkdir -p "$WORK_DIR"'
content << '    /sbin/start-stop-daemon --start --pidfile $PIDFILE --user $USER --group $USER -b --make-pidfile --chuid $USER --exec $DAEMON $ARGS'
content << '    ;;'
content << '  stop)'
content << '    echo "Stopping server"'
content << '    /sbin/start-stop-daemon --stop --pidfile $PIDFILE --verbose'
content << '    ;;'
content << '  *)'
content << '    echo "Usage: /etc/init.d/$USER {start|stop}"'
content << '    exit 1'
content << '    ;;'
content << 'esac'
content << ''
content << 'exit 0'

new File("/etc/init.d/monitoring").withWriter { out -> content.each { out.println it } }

def executeit(cmd) {
    def p = cmd.execute()

    println "command: " + cmd
    println "stderr: " + p.err.text
    println "stdout: " + p.text
    p.waitFor()
    if (p.exitValue() != 0) throw new RuntimeException("cmd " + cmd + " returned " + p.exitValue());
}

println "Creating service..."

executeit(["chmod", "+x", "/etc/init.d/monitoring"])
executeit(["service", "monitoring", "start"])

def json = new net.minidev.json.JSONObject();
//json.put("occi.networkinterface.address", ip.trim())
result = json.toJSONString()
println result

