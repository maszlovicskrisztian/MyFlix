import { SeasonDetails } from "./season-details";

export type ShowDetailsResponse = {
    id: number;
    title: string;
    overview: string;
    backdropPath: string | null;
    seasonCount: number;
    episodeCount: number;
    seasons: Array<SeasonDetails>;
    genres: Array<string>;
}