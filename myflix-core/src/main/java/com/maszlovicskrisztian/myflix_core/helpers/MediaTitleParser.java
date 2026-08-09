package com.maszlovicskrisztian.myflix_core.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MediaTitleParser {
    private static final Pattern YEAR = Pattern.compile("\\b(19|20)\\d{2}\\b");
    private static final Pattern EPISODE = Pattern.compile("(?i)e(\\d{1,3})");
    private static final Pattern SEASON = Pattern.compile("(?i)s(\\d{1,2})");

    public String getTitle(Path relativePath) {
        String folderName = relativePath.getName(relativePath.getNameCount() - 2).toString();

        log.debug("Started title parsing from folder name: {}", folderName);

        String titlePart = null;

        Matcher seasonMatcher = SEASON.matcher(folderName);
        if (seasonMatcher.find()) {
            titlePart = folderName.substring(0, seasonMatcher.start());
        }

        String toBeMatched = titlePart == null ? folderName : titlePart;
        Matcher yearMatcher = YEAR.matcher(toBeMatched);
        if (yearMatcher.find()) {
            titlePart = folderName.substring(0, yearMatcher.start());
        }

        if (titlePart == null) {
            log.warn("Folder name had no match for season nor year, title could not be resolved");
            return null;
        }

        return titlePart.replace('.', ' ').trim();
    }

    public String getYear(Path relativePath) {
        String folderName = relativePath.getName(relativePath.getNameCount() - 2).toString();

        Matcher yearMatcher = YEAR.matcher(folderName);
        if (!yearMatcher.find()) {
            log.warn("Could not determine year from {}", folderName);
            return null;
        }

        log.debug("Resolved {} for year in {}", yearMatcher.group(), folderName);
        return yearMatcher.group();
    }

    public Integer getSeason(Path relativePath) {
        String folderName = relativePath.getName(relativePath.getNameCount() - 2).toString();
        Matcher seasonMatcher = SEASON.matcher(folderName);

        if (!seasonMatcher.find()) {
            log.warn("Could not determine season from {}", folderName);
            return null;
        }

        log.debug("Resolved {} for season in {}", seasonMatcher.group(1), folderName);
        return  Integer.parseInt(seasonMatcher.group(1));
    }

    public Integer getEpisode(Path relativePath) {
        String fileName = relativePath.getFileName().toString();
        Matcher episodeMatcher = EPISODE.matcher(fileName);

        if (!episodeMatcher.find()) {
            log.warn("Could not determine episode from {}", fileName);
            return null;
        }

        log.debug("Resolved {} for episode in {}", episodeMatcher.group(1), fileName);
        return  Integer.parseInt(episodeMatcher.group(1));
    }
}
