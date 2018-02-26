import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class App {


    private static final Logger logger = LogManager.getLogger(App.class.getName());
    private static final String LDAP_CONFIG = "src/main/resources/ldapConfig.config";
    private static final String VALUE_CHANGES = "src/main/resources/attrValue.config";

    private static final String WEB_DAV_URL = "webDAVURL";
    private static final String WEB_ACCESS_URL = "webAccessURL";
    private static final String FILE_INFO  = "fileInfo";

    private static final String PRINCIPAL = "principal";
    private static final String CREDENTIALS = "credentials";
    private static final String LDAP_URL = "ldapUrl";
    private static final String SEARCH_BASE = "searchBase";

    private static final String STRID = "strID";
    private static final String DC = "dc";
    private static final String CN = "cn";

    List<HashMap<String, String>> valueChanges = new ArrayList<>();
    List<HashMap<String, String>> ldapConfigs = new ArrayList<>();

    public static void main(String[] args) {
        logger.info("Begin tool update info for LDAP");
//        final String ldapAdServer = "ldap://192.168.1.227:389";
//        final String ldapSearchBase = "o=editor,dc=dubueditor22,dc=com";
//
//        final String ldapUsername = "cn=admin,ou=users,dc=com";
//        final String ldapPassword = "123";

//        Hashtable<String, Object> env = new Hashtable<String, Object>();
//        Object simple = env.put(Context.SECURITY_AUTHENTICATION, "simple");
//        if(StringUtils.isNoneBlank(ldapUsername)) {
//            env.put(Context.SECURITY_PRINCIPAL, ldapUsername);
//        }
//        if(StringUtils.isNoneBlank(ldapPassword)) {
//            env.put(Context.SECURITY_CREDENTIALS, ldapPassword);
//        }
//        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
//        env.put(Context.PROVIDER_URL, ldapAdServer);

        //ensures that objectSID attribute values
        //will be returned as a byte[] instead of a String
//        env.put("java.naming.ldap.attributes.binary", "objectSID");

        // the following is helpful in debugging errors
        //env.put("com.sun.jndi.ldap.trace.ber", System.err);
        App ldap = new App();

        ldap.readConfigFromFile();
        DirContext ctx = null;
        try {
            ctx = ldap.getContext();
        } catch (NamingException e) {
            logger.info("LDAP dir context can't init");
        }

        // load reseller (SuperProvider : in config file)
        try {
            SearchResult result = ldap.searchReseller(ctx);
        } catch (NamingException e) {
            logger.info("Update fails");
        }
//        ldap.modifyAttributesDo(ctx);
//        System.out.println(result.toString());

    }

    private DirContext getContext() throws NamingException {
        Hashtable<String, Object> env = new Hashtable<String, Object>();
        Object simple = env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if(StringUtils.isNoneBlank(ldapConfigs.get(0).get(PRINCIPAL))) {
            env.put(Context.SECURITY_PRINCIPAL, ldapConfigs.get(0).get(PRINCIPAL));
        }
        if(StringUtils.isNoneBlank(ldapConfigs.get(0).get(CREDENTIALS))) {
            env.put(Context.SECURITY_CREDENTIALS, ldapConfigs.get(0).get(CREDENTIALS));
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapConfigs.get(0).get(LDAP_URL));

        //ensures that objectSID attribute values
        //will be returned as a byte[] instead of a String
        env.put("java.naming.ldap.attributes.binary", "objectSID");

        // the following is helpful in debugging errors
        //env.put("com.sun.jndi.ldap.trace.ber", System.err);

        DirContext ctx = new InitialDirContext(env);
        return ctx;
    }

    private void readConfigFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(LDAP_CONFIG))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                String[] ldapConfig = sCurrentLine.split(" ");
                HashMap<String, String> config;
                if (null != ldapConfig && ldapConfig.length == 4) {
                    config = new HashMap<>();
                    config.put(LDAP_URL, ldapConfig[0]);
                    config.put(SEARCH_BASE, ldapConfig[1]);
                    config.put(PRINCIPAL, ldapConfig[2]);
                    config.put(CREDENTIALS, ldapConfig[3]);
                    ldapConfigs.add(config);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // file 2
        try (BufferedReader br = new BufferedReader(new FileReader(VALUE_CHANGES))) {

            String sCurrentLine;

            int line = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                if (line == 0) {
                    line = line + 1;
                    continue;
                }
                String[] ldapValues= sCurrentLine.split(" ");
                HashMap<String, String> value;

                if (ldapValues.length == 3) {
                    value = new HashMap<>();
                        value.put(WEB_DAV_URL, ldapValues[0]);
                        value.put(WEB_ACCESS_URL, ldapValues[1]);
                        value.put(FILE_INFO, ldapValues[2]);
                    valueChanges.add(value);
                }
                line = line + 1;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("Read file complete");
    }

    // Reseller, strID
    private SearchResult searchReseller(DirContext ctx) throws NamingException {
        // # baseObject   : o=editor,998877dc=dubueditor,dc=com
        // # scope        : singleL889evel (1)
        // (strID=*)
//        NamingEnumeration<SearchResult> results = ctx.search("strID=btphong,strID=dubuweb,o=editor,dc=dubueditor,dc=com", "(webDAVURL=*)", getSimpleSearchControls());
//        NamingEnumeration<SearchResult> results = ctx.search("strID=dubuweb2,o=editor,dc=dubueditor,dc=com", "(strID=*)", getSimpleSearchControls());
        NamingEnumeration<SearchResult> results = ctx.search(ldapConfigs.get(0).get(SEARCH_BASE), "(strID=*)", getSimpleSearchControls());

        SearchResult searchResult = null;
        while (results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();
            logger.info(searchResult.getAttributes().get("strID").get());

            logger.info(String.format("Update info for account: %s",searchResult.getAttributes().get("strID") ));

            // search dommain
            // dk: strID = *
            String superProviderBase = String.format("strID=%s,%s", searchResult.getAttributes().get("strID").get(), ldapConfigs.get(0).get(SEARCH_BASE));
            searchDomainBase(ctx, superProviderBase);

            //make sure there is not another item available, there should be only 1 match
            /*if(results.hasMoreElements()) {
                System.err.println("Matched multiple users for the accountName: ");
                return null;
            }*/
        }

        return searchResult;

    }

    // domain
    private void searchDomainBase(DirContext ctx, String domainBase) throws NamingException {
        NamingEnumeration<SearchResult> results = ctx.search(domainBase, "(dc=*)", getSimpleSearchControls());
        SearchResult searchResult = null;
        while (results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();
            // serachResult container domain
            // then update field: WebAcessURL, WebDAVURL
            String searchDomainBase = String.format("dc=%s,%s", searchResult.getAttributes().get("dc").get(),domainBase);
            updateAttributesDomain(ctx, searchDomainBase);
            // search account ddk: cn = *
            searchAccountLdap(ctx, searchDomainBase);
        }
    }

    // Search account LDAP
    private void  searchAccountLdap(DirContext ctx, String accountBase) throws NamingException {
        // search account, cn=*
        NamingEnumeration<SearchResult> results = ctx.search(accountBase, "(cn=*)", getSimpleSearchControls());
        // udpate fileInfo
        SearchResult searchResult = null;
        while (results.hasMoreElements()) {
            searchResult = (SearchResult) results.nextElement();
            String searchAccountBase = String.format("cn=%s,%s", searchResult.getAttributes().get("cn").get(),accountBase);
            // update
            updateAttributesFileinfo(ctx, searchAccountBase);
        }
        // end
    }
    // update fileInfo
    private void updateAttributesFileinfo(DirContext ctx, String accountBase) throws NamingException {
        // Specify the changes to make
        ModificationItem[] mods = new ModificationItem[1];
        // Replace the "mail" attribute with a new value
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(FILE_INFO, valueChanges.get(0).get(FILE_INFO)));
        // Perform the requested modifications on the named object
        // dc=mikorn.com,strID=btphong,strID=dubuweb,o=editor,dc=dubueditor22,dc=com"
        logger.info(String.format("Update fileInfo, Ldap base: %s, value = %s ", accountBase, valueChanges.get(0).get(FILE_INFO)));
        ctx.modifyAttributes(accountBase, mods);
        logger.info("Update fileInfo success");
    }

    // modifier atriture domain
    private void updateAttributesDomain(DirContext ctx,String domainBase) throws NamingException {
        // Specify the changes to make
        ModificationItem[] mods = new ModificationItem[2];
        // Replace the "mail" attribute with a new value
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("webDAVURL", valueChanges.get(0).get(WEB_DAV_URL)));
        mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(WEB_ACCESS_URL, valueChanges.get(0).get(WEB_ACCESS_URL)));
        // Perform the requested modifications on the named object
//        ctx.modifyAttributes("dc=mikorn.com,strID=btphong,strID=dubuweb,o=editor,dc=dubueditor22,dc=com", mods);
        logger.info(String.format("Update webDAVURL,webAccessURL Ldap base: %s, value = %s and %s ", domainBase,
                valueChanges.get(0).get(WEB_DAV_URL) , valueChanges.get(0).get(WEB_ACCESS_URL)));
        ctx.modifyAttributes(domainBase, mods);
        logger.info("Update webDAVURL,webAccessURL success");
    }

    private SearchControls getSimpleSearchControls() {
        SearchControls searchControls = new SearchControls();
       //searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        searchControls.setTimeLimit(30000);
        //String[] attrIDs = {"objectGUID"};
        //searchControls.setReturningAttributes(attrIDs);
        return searchControls;
    }
}
