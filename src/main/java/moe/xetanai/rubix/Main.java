package moe.xetanai.rubix;

import moe.xetanai.rubix.database.Database;
import moe.xetanai.rubix.modules.CommandModule;
import moe.xetanai.rubix.modules.WelcomeModule;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class.getName());

    private Database database;
    private static JDA api;

    private Main () {
        logger.info("Starting Rubix v" + RubixInfo.VERSION);

        try {
            logger.info("Loading config.json");
            String confcontents = getConfigFileContents();

            JSONObject config = new JSONObject(new JSONTokener(confcontents));

            // Get objects for our various config types
            JSONObject botCfg = config.getJSONObject("bot");
            this.database = new Database(config.getJSONObject("database"));

            // Everything else checks out. Get ready to authenticate with Discord
            JDABuilder apibuilder = new JDABuilder(AccountType.BOT).setToken(botCfg.getString("token")).setAudioEnabled(false).addEventListener(new WelcomeModule()).addEventListener(new CommandModule(this));
            api = apibuilder.build();
        } catch (IOException | JSONException err) {
            logger.fatal("Failed to load and parse configuration", err);
            System.exit(1);
        } catch (LoginException err) {
            logger.fatal("Failed to login to Discord", err);
            System.exit(1);
        }

        logger.traceExit();
    }

    /**
     * @return config.json contents as a string
     *
     * @throws IOException if there was an IO error while reading it
     */
    private String getConfigFileContents () throws IOException {
        // Open and parse the config file.
        StringBuilder configContents = new StringBuilder();
        Stream<String> stream = Files.lines(Paths.get("config.json"), StandardCharsets.UTF_8);
        stream.forEach(line -> configContents.append(line).append("\n"));
        stream.close();

        return configContents.toString();
    }

    /**
     * @return Database
     */
    public Database getDatabase () {
        return this.database;
    }

    /**
     * @return JDA API root
     */
    public static JDA getApi () {
        return api;
    }

    /**
     * Lights, camera, action!
     */
    public static void main (String[] args) {
        new Main();
    }
}
