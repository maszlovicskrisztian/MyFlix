package com.maszlovicskrisztian.myflix_core.helpers;

import com.maszlovicskrisztian.myflix_core.dtos.ParsedMediaTitle;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class MediaTitleParser {
    private static final Pattern YEAR = Pattern.compile("\\b(19|20)\\d{2}\\b");
    private static final Pattern EPISODE = Pattern.compile("(?i)s(\\d{1,2})e(\\d{1,3})");
    private static final Set<String> NOISE = Set.of(
            "1080p", "720p", "2160p", "4k", "bluray", "webrip", "web-dl", "webdl",
            "hdtv", "x264", "x265", "hevc", "aac", "dts", "ac3", "extended", "remastered"
    );

    public ParsedMediaTitle parseByFilename(String relativePath) {
        String fileName = Paths.get(relativePath).getFileName().toString();
        return parse(fileName);
    }

    public ParsedMediaTitle parseByFolder(String relativePath) {
        Path path = Paths.get(relativePath);
        String folderName = path.getName(path.getNameCount() - 2).toString();
        return parse(folderName);
    }

    private ParsedMediaTitle parse(String value) {
        String base = value.replaceAll("\\.[^.]+$", "");
        String normalized = base.replaceAll("[._]", " ");

        Matcher epMatcher = EPISODE.matcher(normalized);
        boolean isEpisode = epMatcher.find();
        String titlePart = isEpisode ? normalized.substring(0, epMatcher.start()) : normalized;

        Matcher yearMatcher = YEAR.matcher(titlePart);
        Integer year = null;
        if (yearMatcher.find()) {
            year = Integer.parseInt(yearMatcher.group());
            titlePart = titlePart.substring(0, yearMatcher.start());
        }

        String cleanTitle = Arrays.stream(titlePart.split(" "))
                .filter(w -> !w.isBlank() && !NOISE.contains(w.toLowerCase()))
                .collect(Collectors.joining(" "))
                .trim();

        return new ParsedMediaTitle(
                cleanTitle, year, isEpisode,
                isEpisode ? Integer.parseInt(epMatcher.group(1)) : null,
                isEpisode ? Integer.parseInt(epMatcher.group(2)) : null
        );
    }
}
