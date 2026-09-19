/**
 * What the backend already knows about a media item, in the shape the metadata editor
 * edits. `mediaType` is null while the item is still unknown — nothing has been
 * enriched yet — so the editor falls back to its own default in that case.
 */
export type MetadataDetails = {
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
