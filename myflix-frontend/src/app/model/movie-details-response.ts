export type MovieDetailsResponse = {
    id: number;
    addedAt: string;
    overview: string | null;
    title: string | null;
    posterPath: string | null;
    backdropPath: string | null;
    releaseDate: string | null;
    runtimeMinutes: number | null;
}