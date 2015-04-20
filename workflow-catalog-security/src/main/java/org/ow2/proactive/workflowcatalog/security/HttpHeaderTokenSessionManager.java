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


package org.ow2.proactive.workflowcatalog.security;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.session.mgt.SimpleSessionFactory;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;


public class HttpHeaderTokenSessionManager extends DefaultWebSessionManager {

    public static final String TOKEN_KEY = "sessionid";

    public HttpHeaderTokenSessionManager() {
        setSessionFactory(new CopyAttributesSessionFactory());
        SessionDAO dao = getSessionDAO();
        if (dao instanceof AbstractSessionDAO) {
            ((AbstractSessionDAO) dao).setSessionIdGenerator(new CopyTokenFromContext());
        }
    }

    @Override
    public Serializable getSessionId(SessionKey sessionKey) {
        ServletRequest request = WebUtils.getRequest(sessionKey);
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String tokenKeyHeader = httpRequest.getHeader(TOKEN_KEY);
            if (tokenKeyHeader == null) {
                return sessionKey.getSessionId();
            }
            return tokenKeyHeader;
        } else {
            return sessionKey.getSessionId();
        }
    }


    private static class CopyAttributesSessionFactory extends SimpleSessionFactory {
        @Override
        public Session createSession(SessionContext initData) {
            Session session = super.createSession(initData);
            for (Map.Entry<String, Object> initDataEntry : initData.entrySet()) {
                session.setAttribute(initDataEntry.getKey(), initDataEntry.getValue());
            }
            return session;
        }
    }

    private static class CopyTokenFromContext implements org.apache.shiro.session.mgt.eis.SessionIdGenerator {
        @Override
        public Serializable generateId(Session session) {
            Object token = session.getAttribute(TOKEN_KEY);
            if (token == null) {
                return "unvalid" + UUID.randomUUID().toString();
            } else {
                return (Serializable) token;
            }
        }
    }
}
