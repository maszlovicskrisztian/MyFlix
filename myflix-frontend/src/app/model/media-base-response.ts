export type MediaBaseResponse = {
    showId: number | null;
    fileInfoId: number | null;
    tmdbId: number | null;
    title: string;
    imagePath: string;
    genres: Array<string>;
}