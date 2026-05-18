# YouTubeSpy

A Java application that fetches recent videos from YouTube channels and displays them either in a web browser or terminal.

## Features

- Search multiple YouTube channels for recent videos
- Output results to a web browser with filtering/sorting
- Output results to terminal
- Support for reading channel IDs from a file
- Local caching to reduce API calls

## Prerequisites

- Java 17 or higher
- Google API Key with YouTube Data API v3 enabled

## Build

```bash
./gradlew fatJar
```

The runnable JAR will be created at `build/libs/YouTubeSpy.jar`.

## Usage

```bash
java -jar build/libs/YouTubeSpy.jar -k <API_KEY> -c <CHANNEL_ID> -w
java -jar build/libs/YouTubeSpy.jar -k <API_KEY> -f /path/to/channelids.txt -t
```

### Options

| Flag | Long Form | Required | Description |
|------|-----------|----------|-------------|
| `-k` | `--api-key` | Yes | Google API key |
| `-c` | `--channel-id` | No* | Single channel ID to search |
| `-f` | `--file` | No* | Path to file with channel IDs (one per line) |
| `-w` | `--website` | No | Generate HTML output (default) |
| `-wl` | `--website-location` | No | Output path for website (default: ~/.youtubespy/index.html) |
| `-m` | `--max-videos` | No | Maximum videos per channel (default: 5) |
| `-r` | `--refresh` | No | Force refresh (ignore cache) |
| `-cl` | `--cache-location` | No | Custom cache file path (default: ~/.youtubespy/cache.json) |
| `-ct` | `--cache-ttl` | No | Cache lifetime in minutes (default: 30) |
| `-cc` | `--clear-cache` | No | Delete cache and exit |
| `-t` | `--terminal` | No | Output to terminal instead of website |
| `-h` | `--help` | No | Show help message |

\* Either `-c` or `-f` must be provided.

### Examples

Search a single channel and display in browser:
```bash
java -jar YouTubeSpy.jar -k YOUR_API_KEY -c UC_x5XG1OV2P6uZZ5FSM9Ttw -w
```

Search with custom video limit:
```bash
java -jar YouTubeSpy.jar -k YOUR_API_KEY -c CHANNEL_ID -w -m 10
```

Search multiple channels from a file and output to terminal:
```bash
java -jar YouTubeSpy.jar -k YOUR_API_KEY -f channelids.txt -t
```

Search with custom output location:
```bash
java -jar YouTubeSpy.jar -k YOUR_API_KEY -c CHANNEL_ID -w -wl /path/to/output.html
```

Force refresh (ignore cache):
```bash
java -jar YouTubeSpy.jar -k YOUR_API_KEY -c CHANNEL_ID -r
```

Clear cache:
```bash
java -jar YouTubeSpy.jar --clear-cache -k YOUR_API_KEY
```

## Getting a Google API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project
3. Enable "YouTube Data API v3"
4. Go to "Credentials" and create an API key
5. (Optional) Set up quota to avoid running out of API credits

## Output

- **Website mode**: Opens `index.html` in your default browser with filterable video cards
- **Terminal mode**: Prints video details in a simple list format