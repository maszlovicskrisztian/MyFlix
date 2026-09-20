export type MetadataDetails = {
    relativePath: string;
    mediaId: number;
    mediaType: 'MOVIE' | 'EPISODE' | null;
    tmdbId: number | null;
    title: string | null;
    overview: string | null;
    backdropPath: string | null;
    posterPath: string | null;
    releaseDate: string | null;
    runtimeMinutes: number | null;
    genres: Array<string> | null;
    showId: number | null;
    seasonNumber: number | null;
    episodeNumber: number | null;
}
