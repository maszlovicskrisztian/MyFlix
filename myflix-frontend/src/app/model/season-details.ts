import { EpisodeDetails } from "./episode-details";

export type SeasonDetails = {
    title: string;
    overview: string;
    posterPath: string | null;
    seasonNumber: number;
    episodes: Array<EpisodeDetails>;
}