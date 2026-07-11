# Upipe

Upipe is an Android streaming app with its own package identity, launcher icons, and About/Licenses screens. The current Android application ID is `com.upipe.app`.

## Download Builds

Build hashes and the local APK build list are tracked at:

https://raw.githubusercontent.com/hsuxy8uxi/Upipe/main/releases/upipe-apk-builds.txt

The latest build is `v1.0.0`.

## Extractor Source

Upipe includes a local source copy of NewPipeExtractor in `NewPipeExtractor/`. Upipe builds use that local extractor through Gradle included-build dependency substitution, so extractor patches are part of the public source and are licensed under GPL-3.0-or-later with the upstream NewPipeExtractor notices preserved.

## Contribute

Upipe grows through testing, translations, design feedback, and code contributions. Open issues, share fixes, or suggest improvements on GitHub. Every useful report or pull request helps make the app cleaner and easier to use.

Repository:

https://github.com/hsuxy8uxi/Upipe

## License

Upipe is distributed under the GNU General Public License, version 3.0. See `LICENSE` for the full license text and `NOTICE` for the copyright notice.

## Attribution

Upipe includes work from NewPipe, a free and open source streaming frontend for Android. Upstream NewPipe code and documentation remain copyright their original authors and contributors.
