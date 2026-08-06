package com.maszlovicskrisztian.myflix_core.helpers;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MediaTitleParser {
    private static final Pattern YEAR = Pattern.compile("\\b(19|20)\\d{2}\\b");
    private static final Pattern EPISODE = Pattern.compile("(?i)e(\\d{1,3})");
    private static final Pattern SEASON = Pattern.compile("(?i)s(\\d{1,2})");

    public String getTitle(Path relativePath) {
        String folderName = relativePath.getName(relativePath.getNameCount() - 2).toString();

        String titlePart = null;
        Matcher seasonMatcher = SEASON.matcher(folderName);
        if (seasonMatcher.find()) {
            titlePart = folderName.substring(0, seasonMatcher.start());
        } else {
            Matcher yearMatcher = YEAR.matcher(folderName);
            if (yearMatcher.find()) {
                titlePart = folderName.substring(0, yearMatcher.start());
            }
        }

        if (titlePart == null)
            return null;

        return titlePart.replace('.', ' ').trim();
    }

    public Integer getSeason(Path relativePath) {
        String folderName = relativePath.getName(relativePath.getNameCount() - 2).toString();
        Matcher seasonMatcher = SEASON.matcher(folderName);

        if (!seasonMatcher.find()) return null;

        return  Integer.parseInt(seasonMatcher.group(1));
    }

    public Integer getEpisode(Path relativePath) {
        String fileName = relativePath.getFileName().toString();
        Matcher episodeMatcher = EPISODE.matcher(fileName);

        if (!episodeMatcher.find()) return null;

        return  Integer.parseInt(episodeMatcher.group(1));
    }
}
