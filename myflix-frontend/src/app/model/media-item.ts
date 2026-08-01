export type MediaItem = {
    id: number;
    fileName: string;
    addedAt: string;

    tmdbId: number | null;
    overview: string | null;
    title: string | null;
    posterPath: string | null;
    backdropPath: string | null;
    releaseDate: string | null;
    runtimeMinutes: number | null;
}