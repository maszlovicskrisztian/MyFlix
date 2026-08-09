/** Avatar files served from `public/avatar/`. The file name is the stored avatarKey. */
export const AVATARS = ['dear-128.ico', 'duck-128.png', 'fox-128.ico'];

export function avatarUrl(avatarKey: string): string {
  return `/avatar/${avatarKey}`;
}
