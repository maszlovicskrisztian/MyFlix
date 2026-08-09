export type PlaybackInfo = {
    mode: 'DIRECT' | 'HLS';
    url: string;
    progressSeconds: number;
    durationSeconds: number;
}