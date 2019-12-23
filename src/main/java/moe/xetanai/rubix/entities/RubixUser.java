package moe.xetanai.rubix.entities;

import moe.xetanai.rubix.Main;
import moe.xetanai.rubix.utils.FilterUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class RubixUser {
    private final Logger logger = LogManager.getLogger("RubiX User-"+this.id);

    private long id;

    public RubixUser(long id) {
        this.id = id;
    }

    public RubixUser(User user) {
        this(user.getIdLong());
    }

    private User getUser() {
        return Main.getApi().getUserById(this.id);
    }

    /* FILTER METHODS */

    public String getName() {
        User u = getUser();
        return FilterUtils.filter(u.getName());
    }

    public String getAsTag() {
        User u = getUser();
        return FilterUtils.filter(u.getAsTag());
    }

    public List<RubixGuild> getMutualGuilds() {
        List<RubixGuild> guilds = new ArrayList<>();
        User u = getUser();

        // convert all guilds to our format
        for(Guild g : u.getMutualGuilds()) {
            guilds.add(new RubixGuild(g));
        }

        return guilds;
    }

    /* PROXY METHODS. ADDED AS NEEDED. METHODS NOT USED WILL NOT BE PROXIED */

    /* CONVENIENCE METHODS */

}