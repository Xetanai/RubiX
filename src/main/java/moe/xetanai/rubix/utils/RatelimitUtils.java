package moe.xetanai.rubix.utils;

import moe.xetanai.rubix.entities.Command;
import net.dv8tion.jda.core.entities.User;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public class RatelimitUtils {
	private static final int DEFAULT_RATELIMIT = 0;
	private static Map<User,RatelimitInfo> limits = new HashMap<>();

	public static void setResetTime(User u, Command cmd, OffsetDateTime time) {
		RatelimitInfo rli = limits.getOrDefault(u, new RatelimitInfo());
		rli.setResetTime(cmd, time);
		limits.put(u,rli);
	}

	public static void setResetTime(User u, Command cmd) {
		RatelimitInfo rli = limits.getOrDefault(u, new RatelimitInfo());
		if(DEFAULT_RATELIMIT != 0) {
			rli.setResetTime(cmd, OffsetDateTime.now().plusSeconds(DEFAULT_RATELIMIT));
		}
	}

	public static OffsetDateTime getResetTime(User u, Command cmd) {
		RatelimitInfo rli = limits.getOrDefault(u, new RatelimitInfo());
		return rli.getResetTime(cmd);
	}

	private static class RatelimitInfo {
		private Map<Command, OffsetDateTime> commandUsages = new HashMap<>();

		public RatelimitInfo() {}

		public void setResetTime(Command cmd, OffsetDateTime time) {
			this.commandUsages.put(cmd, time);
		}

		public OffsetDateTime getResetTime(Command cmd) {
			return this.commandUsages.get(cmd);
		}
	}
}
