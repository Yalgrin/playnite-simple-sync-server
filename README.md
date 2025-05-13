# Playnite Simple Sync Server

Backend server for the [Playnite Simple Sync Plugin](https://github.com/Yalgrin/playnite-simple-sync-plugin). Stores
library data in Postgres database and image files in the file system.

**Intended to be used in local networks. Currently not ready for use when exposed to the internet.**

## Installation

### Inside Docker container

#### Prerequisites

- [Docker](https://www.docker.com/)

#### Instructions

1. Download the [`example-compose.yml`](https://github.com/Yalgrin/playnite-simple-sync-server/blob/master/example-compose.yml) file.
2. In the `volumes` sections, specify the paths which will be used to persist data on your machine.
3. Run `docker compose -f example-compose.yml up -d`.
