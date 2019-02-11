package us.centile.permissions.jedis;

import us.centile.permissions.*;
import redis.clients.jedis.*;

public class JedisPublisher
{
    private nPermissions main;
    
    public JedisPublisher(final nPermissions main) {
        this.main = main;
    }
    
    public void write(final String message) {
        Jedis jedis = null;
        try {
            jedis = this.main.getPool().getResource();
            if (this.main.getConfigFile().getBoolean("DATABASE.REDIS.AUTHENTICATION.ENABLED")) {
                jedis.auth(this.main.getConfigFile().getString("DATABASE.REDIS.AUTHENTICATION.PASSWORD"));
            }
            jedis.publish("permissions", message);
        }
        finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
