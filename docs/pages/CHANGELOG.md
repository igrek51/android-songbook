# Changelog - What's new in Songbook
All **user-facing**, notable changes will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.34.2] - 1894 - 2023-03-21
### Added
- Songbook keeps daily backups of *My Songs*. 
  It saves versions of *My Songs* from last 14 days in a local directory `Internal Storage/Android/data/igrek.songbook/files/backup`.
  There's a new option in *Settings*: "Keep daily backups of My Songs", which is enabled by default.
- You can restore Your Songs by choosing "Restore My Songs from a local backup" in *Settings*.
  It gives you a chance to recover Your Songs from a local backup from last 14 days.
  This is particularly useful in case of corrupted data or when your songs are gone for other reasons.

### Changed
- In case of detecting corrupted user data, user can choose whether to exit or to reset local data.
- "Favourite songs" are now called "Liked Songs".

### Fixed
- Fixed corrupting user data in case of a concurrent loading. 

## [1.34.1] - 1893 - 2023-03-09
### Added
- Whenever Songbook app is updated, it prompts you to view the Changelog to see the latest changes.

## [1.34.0] - 1892 - 2023-03-09
### Added
- You can Synchronize your songs with a Web Editor to modify them in any browser, for instance on your desktop computer.
  Then synchronize them back to see the changes on your local device.
  Click 3-dots icon on *My Songs* and pick *Synchronize with Web Editor*.
  This will start a temporary session for 24 hours, which you can access by opening link on any device.
  If changes were made in both places (locally and remotely), there might be a conflict to be resolved.
- *Songbook Web UI* is available at [songbook.igrek.dev](https://songbook.igrek.dev/ui).
  Right now it only handles *Synchronize Sessions*.
- Changelog can be opened from the *About* window
  and is available at [Manual pages](https://igrek51.github.io/android-songbook/CHANGELOG/).
- Runtime logs from the application can be browsed in the app by typing the secret command `logs`.
- Copyright notices of the third-party libraries are linked in the *About* window.

### Fixed
- Overlapping chords in the "Chords Above" style are now displayed correctly.
- Lists of songs are now properly updated when a change to a custom song is applied.

## [1.33.2] - 1891 - 2023-01-19
### Added
- Non-fatal errors can be reported by clicking *Send Report* on an error pop-up.

### Changed
- Improved startup performance by loading user data asynchronously.

### Fixed
- Inverted chords (eg. `C/G`) are no longer splitted when wrapping the lines.
- Fixed security error on some devices when opening the external links, like Privacy Policy.

## [1.33.1] - 1890 - 2022-12-30
### Added
- Debug logs are included in reports in case of a crash.