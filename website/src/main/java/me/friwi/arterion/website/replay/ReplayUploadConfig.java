package me.friwi.arterion.website.replay;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReplayUploadConfig {
    @Value("${replay.dir}")
    public String REPLAY_DIR;
    @Value("${replay.secret}")
    public String REPLAY_SECRET;
}
