# Playnite Simple Sync Server

Backend server for the [Playnite Simple Sync Plugin](https://github.com/Yalgrin/playnite-simple-sync-plugin). Stores
library data in Postgres database and image files in the file system.

**Intended to be used in local networks. Currently not ready for use when exposed to the internet.**

## Installation

### Inside Docker container

#### Prerequisites

- [Docker](https://www.docker.com/)

#### Instructions

1. Download the [`example-compose.yml`](https://raw.githubusercontent.com/Yalgrin/playnite-simple-sync-server/refs/heads/master/example-compose.yml) file.
2. The only required piece of configuration are locations of the server data and logs. You can change them in the following lines:

        volumes:
          - 'D:/Playnite-Sync-Server/metadata/:/app/metadata'
          - 'D:/Playnite-Sync-Server/logs/:/app/logs'

        volumes:
          - 'D:/Playnite-Sync-Server/postgres/data/:/var/lib/postgresql/data'

Replace `D:/Playnite-Sync-Server` with the path to the directory of your choosing.

3. Navigate to the directory where `example-compose.yml` is located and execute the following command in the terminal/command line:


    docker compose -f example-compose.yml up -d

The server should now be running on port 8093 (unless changed). You can now specify the URL of the app in the plugin settings as follows:

    http://<SERVER_IP>:8093

Replace `<SERVER_IP>` with the IP address of the machine that's running the application. You can manually check if it's running by opening the following link:

    http://<SERVER_IP>:8093/api/health