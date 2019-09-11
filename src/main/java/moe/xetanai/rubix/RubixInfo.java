package moe.xetanai.rubix;

public class RubixInfo {

    private static final int VERSION_MAJOR = 0;
    private static final int VERSION_MINOR = 0;
    private static final int VERSION_PATCH = 0;

    public static final long[] OWNER_IDS = {155490847494897664L};
    public static final String RELEASE_NAME = "InDev";
    public static final String VERSION = String.format("%d.%d.%d", VERSION_MAJOR, VERSION_MINOR, VERSION_PATCH, RELEASE_NAME);

    // Links
    public static final String LINK_INVITE = "https://discordapp.com/api/oauth2/authorize?client_id=254746730367680512&scope=bot";
    public static final String LINK_SERVER = "https://discord.gg/x4fPTjt";
    public static final String LINK_GIT = "https://git.xetanai.moe/Xetanai/RubiX";

    @Deprecated
    public static final String LINK_FIELD_TEXT = String.format("[Invite RubiX to your server!](%s)\n" + "[Support server](%s)\n" + "[RubiX source code](%s)", LINK_INVITE, LINK_SERVER, LINK_GIT);
}
