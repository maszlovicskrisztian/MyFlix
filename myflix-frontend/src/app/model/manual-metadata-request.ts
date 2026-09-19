export type ManualMetadataRequest = {
    mediaType: 'MOVIE' | 'EPISODE';
    tmdbId: number | null;
    title: string;
    overview: string | null;
    backdropPath: string | null;
    releaseDate: string | null;
    runtimeMinutes: number | null;
    genres: Array<string>;
    posterPath: string | null;
    showId: number | null;
    seasonNumber: number | null;
    episodeNumber: number | null;
}
