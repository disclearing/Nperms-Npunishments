package us.centile.permissions.util.database;

import com.mongodb.client.*;
import us.centile.permissions.*;
import com.mongodb.*;
import com.mongodb.MongoClient;

import java.util.*;
import java.security.cert.*;
import java.security.*;
import javax.net.ssl.*;

public class PermissionsDatabase
{
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection ranks;
    private MongoCollection profiles;
    
    public PermissionsDatabase(final nPermissions main) {
        if (main.getConfigFile().getBoolean("DATABASE.MONGO.AUTHENTICATION.ENABLED")) {
            this.client = new MongoClient(new ServerAddress(main.getConfigFile().getString("DATABASE.MONGO.HOST"), main.getConfigFile().getInt("DATABASE.MONGO.PORT")), Arrays.asList(MongoCredential.createCredential(main.getConfigFile().getString("DATABASE.MONGO.AUTHENTICATION.USER"), main.getConfigFile().getString("DATABASE.MONGO.AUTHENTICATION.DATABASE"), main.getConfigFile().getString("DATABASE.MONGO.AUTHENTICATION.PASSWORD").toCharArray())));
        }
        else {
            this.client = new MongoClient(new ServerAddress(main.getConfigFile().getString("DATABASE.MONGO.HOST"), main.getConfigFile().getInt("DATABASE.MONGO.PORT")));
        }
        this.database = this.client.getDatabase("permissions");
        this.ranks = this.database.getCollection("ranks");
        this.profiles = this.database.getCollection("profiles");
    }
    
    private SSLSocketFactory validateCert(final String hostIP) {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> hostname.equals(hostIP));
        final TrustManager[] trustAllCerts = { new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                
                @Override
                public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                }
                
                @Override
                public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                }
            } };
        try {
            final SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, null);
            return sc.getSocketFactory();
        }
        catch (GeneralSecurityException ex) {
            return null;
        }
    }
    
    public MongoClient getClient() {
        return this.client;
    }
    
    public MongoDatabase getDatabase() {
        return this.database;
    }
    
    public MongoCollection getRanks() {
        return this.ranks;
    }
    
    public MongoCollection getProfiles() {
        return this.profiles;
    }
}
