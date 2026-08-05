package com.maszlovicskrisztian.myflix_core.helpers;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MediaPathResolver {

    @Value("${MEDIA_PATH}")
    private String mediaPath;
}
