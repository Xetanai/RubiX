package moe.xetanai.rubix;

import moe.xetanai.rubix.database.Database;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
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

	Database database;
	JDA api;

	public Main() {
		logger.info("Starting Rubix v"+ RubixInfo.VERSION);

		try {
			logger.info("Loading config.json");
			String confcontents = getConfigFileContents();

			JSONObject config = new JSONObject(new JSONTokener(confcontents));

			JSONObject botCfg = config.getJSONObject("bot");
			this.database = new Database(config.getJSONObject("database"));

			JDABuilder apibuilder = new JDABuilder(AccountType.BOT)
					.setToken(botCfg.getString("token"))
					.setAudioEnabled(false);

			this.api = apibuilder.build();
		} catch (IOException | JSONException err) {
			logger.fatal("Failed to load and parse configuration", err);
			System.exit(1);
		} catch (LoginException err) {
			logger.fatal("Failed to login to Discord", err);
			System.exit(1);
		}

		logger.traceExit();
	}

	private String getConfigFileContents() throws IOException {
		// Open and parse the config file.
		StringBuilder configContents = new StringBuilder();
		Stream<String> stream = Files.lines(Paths.get("config.json"), StandardCharsets.UTF_8);
		stream.forEach(line -> configContents.append(line).append("\n"));
		stream.close();

		return configContents.toString();
	}

	public static void main(String[] args) {
		new Main();
	}
}
