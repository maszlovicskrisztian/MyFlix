export type EpisodeDetails = {
    title: string;
    overview: string;
    stillPath: string | null;
    releaseDate: string | null;
    runtimeMinutes: number | null;
    episodeNumber: number;
    fileInfoId: number;
}