/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.automation.extensions.adminclients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.configurations.AutomationConfiguration;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;

import javax.xml.xpath.XPathExpressionException;
import java.rmi.RemoteException;

public class AuthenticationAdminClient {
    private static final Log log = LogFactory.getLog(AuthenticationAdminClient.class);
    private AuthenticationAdminStub authenticationAdminStub;

    public AuthenticationAdminClient(String backendUrl) throws AxisFault {
        String serviceName = "AuthenticationAdmin";
        String endPoint = backendUrl + serviceName;
        if (log.isDebugEnabled()) {
            log.debug("EndPoint" + endPoint);
        }
        try {
            authenticationAdminStub = new AuthenticationAdminStub(endPoint);
        } catch (AxisFault axisFault) {
            log.info("authenticationAdminStub initialization fails");
            throw new AxisFault("authenticationAdminStub initialization fails");
        }
    }

    /**
     * Logs to the given server with the backend and return the session cookie
     *
     * @param userName User intended ti login
     * @param password Password of the user
     * @param host     Host name
     * @return Session cookie of the login session
     * @throws org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException
     * @throws java.rmi.RemoteException
     */
    public String login(String domain, String userName, String password, String host)
            throws LoginAuthenticationExceptionException, RemoteException, XPathExpressionException {
        Boolean loginStatus;
        ServiceContext serviceContext;
        String sessionCookie;
        if (!domain.equals(AutomationConfiguration.getConfigurationValue(ContextXpathConstants.SUPER_TENANT_DOMAIN))) {
            userName += "@" + domain;
        }
        loginStatus = authenticationAdminStub.login(userName, password, host);


        if (!loginStatus) {
            throw new LoginAuthenticationExceptionException("Login Unsuccessful. " +
                    "Return false as a login status by Server");
        }
        log.info("Login Successful");
        serviceContext = authenticationAdminStub._getServiceClient()
                .getLastOperationContext().getServiceContext();
        sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        if (log.isDebugEnabled()) {
            log.debug("SessionCookie :" + sessionCookie);
        }
        return sessionCookie;
    }

    public Boolean unsuccessfulLogin(String userName, String password, String backEndURL)
            throws LoginAuthenticationExceptionException, RemoteException {
        return authenticationAdminStub.login(userName, password, backEndURL);
    }

    public void logOut() throws LogoutAuthenticationExceptionException, RemoteException {
        authenticationAdminStub.logout();
        log.info("log out");
    }

    public Stub getAuthenticationAdminStub() {
        return authenticationAdminStub;
    }
}