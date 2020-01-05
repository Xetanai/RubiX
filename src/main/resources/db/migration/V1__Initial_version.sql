-- Create guilds table. Initially only a mapping of IDs to prefixes.
CREATE TABLE guilds (
    discordid VARCHAR(20) NOT NULL,
    prefix VARCHAR(5) NULL,
    PRIMARY KEY (discordid)
);
