package org.ow2.proactive.workflowcatalog.security;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.shiro.config.Ini;
import org.apache.shiro.realm.text.IniRealm;

public class LoginConfigurationIniRealm extends IniRealm {

    private static final String KEY_VAL_SEPARATOR = ":";
    private String loginFilePath;
    private String groupsFilePath;
    private Ini ini;

    public LoginConfigurationIniRealm() throws IOException {
        super();
    }

    public Ini.Section getUsersSection() {
        return ini.getSection(IniRealm.USERS_SECTION_NAME);
    }

    public void setGroupsFilePath(String groupsFilePath) throws IOException {
        this.groupsFilePath = groupsFilePath;
        update();
    }

    public void setLoginFilePath(String loginFilePath) throws IOException {
        this.loginFilePath = loginFilePath;
        update();
    }

    private void update() throws IOException {
        if (loginFilePath != null && groupsFilePath != null) {
            this.ini = generateIni(loginFilePath, groupsFilePath);
            setIni(this.ini);
            init();
        }
    }

    private Ini generateIni(String loginFilePath, String groupsFilePath) throws IOException {
        Ini ini = new Ini();
        Ini.Section section = ini.addSection(USERS_SECTION_NAME);

        addFileDefinedUsers(section, loginFilePath, groupsFilePath);

        return ini;
    }

    private void addFileDefinedUsers(
            Ini.Section section, String loginFilePath, String groupsFilePath) throws IOException {
        Map<String, UserInfo> userinfo = generateUserInfo(loginFilePath, groupsFilePath);
        for (String name: userinfo.keySet())
            section.put(name, generateSecondArgument(userinfo.get(name)));

        if (section.isEmpty())
            throw new RuntimeException("Shiro configuration needs at least one user to proceed");

    }

    private Map<String, UserInfo> generateUserInfo(
            String loginFilePath, String groupsFilePath) throws IOException {
        Map<String, String> login = readAsMap(loginFilePath);
        List<UserGroupLink> groups = readAsUserGroupLinkList(groupsFilePath);

        Map<String, UserInfo> userinfo = new HashMap<String, UserInfo>();
        for (String user: login.keySet()) {
            UserInfo uinfo = new UserInfo(user);
            uinfo.setPassword(login.get(user));
            for (UserGroupLink link: groups) {
                if (link.getUsername().equals(user)) {
                    uinfo.addGroup(link.getGroup());
                }
            }
            userinfo.put(user, uinfo);
        }

        return userinfo;
    }

    private Map<String, String> readAsMap(String filePath) throws IOException {
        Map<String, String> map = new HashMap<String, String>();

        List<String> lines = getAsLines(filePath);
        for (String line: lines) {
            String[] lineSplit = line.split(KEY_VAL_SEPARATOR);
            if (lineSplit.length == 2) {
                map.put(lineSplit[0], lineSplit[1]);
            }
        }

        return map;
    }

    private List<UserGroupLink> readAsUserGroupLinkList(String filePath) throws IOException {
        List<UserGroupLink> list = new ArrayList<UserGroupLink>();

        List<String> lines = getAsLines(filePath);
        for (String line: lines) {
            String[] lineSplit = line.split(KEY_VAL_SEPARATOR);
            if (lineSplit.length == 2) {
                list.add(new UserGroupLink(lineSplit[0], lineSplit[1]));
            }
        }

        return list;
    }

    private List<String> getAsLines(String filePath) throws IOException {
        List<String> lines = new ArrayList<String>();

        if (filePath == null)
            return lines;

        InputStream is = getAsInputStream(filePath);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null)
            lines.add(line);
        br.close();

        return lines;
    }

    private InputStream getAsInputStream(String resource) throws FileNotFoundException {
        File f = new File(resource).getAbsoluteFile();

        if (f.exists())
            return new FileInputStream(f);

        InputStream isRes = this.getClass().getResourceAsStream(resource);
        if (isRes != null)
            return isRes;

        String msg = "Shiro: file '" + f.getAbsolutePath() +
                "' or resource '" + resource + "' not found";

        throw new FileNotFoundException(msg);
    }

    private String generateSecondArgument(UserInfo userInfo) {

        StringBuilder builder = new StringBuilder();

        builder.append(userInfo.getPassword());
        builder.append(",");

        for (String group: userInfo.getGroups()) {
            builder.append(group);
            builder.append(",");
        }

        builder.setLength(builder.length() -1 );
        return builder.toString();

    }

    class UserGroupLink {

        private String group;
        private String username;

        public UserGroupLink(String username, String group) {
            this.username = username;
            this.group = group;
        }

        public String getUsername() {
            return username;
        }

        public String getGroup() {
            return group;
        }

    }

    class UserInfo {

        private String username;
        private String password;
        private List<String> groups;

        public UserInfo(String username) {
            this.username = username;
            this.groups = new ArrayList<String>();
        }

        public String getUsername() {
            return username;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public void addGroup(String group) {
            groups.add(group);
        }

        public List<String> getGroups() {
            return groups;
        }

        public String toString() {
            return String.format("u'%s' p'%s' g'%s'", username, password, groups);
        }

    }

}
