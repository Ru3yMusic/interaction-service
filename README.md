# interaction-service

Microservicio de interacciones del usuario con el contenido musical de **RUBY MUSIC**. Gestiona likes, historial de reproducción, preferencias del onboarding, canciones ocultas y biblioteca personal. Es el principal **productor Kafka** del sistema — emite eventos que mantienen los contadores del `catalog-service` actualizados.

---

## Responsabilidad

- Likes y unlikes de canciones (con eventos Kafka para `catalog-service`)
- Historial de reproducción por usuario
- Preferencias de géneros y artistas del onboarding (reemplazables)
- Ocultar canciones por álbum (`hidden_songs` scoped a album)
- Biblioteca personal: álbumes y artistas guardados (`ALBUM` | `ARTIST`)

---

## Stack

| Componente | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.2.5 |
| Spring Cloud | 2023.0.1 |
| Spring Data JPA | — |
| Spring Kafka | — (productor) |
| MapStruct | 1.5.5.Final |
| Lombok | — |
| SpringDoc OpenAPI | 2.5.0 |
| OpenAPI Generator (Maven plugin) | 7.4.0 |

---

## Puerto

| Servicio | Puerto |
|---|---|
| interaction-service | **8083** |
| Acceso vía gateway | `http://localhost:8080/api/v1/interactions` |

---

## Base de datos

| Parámetro | Valor |
|---|---|
| Engine | PostgreSQL |
| Database | `interaction_db` |
| Host | `localhost:5432` |
| DDL | `update` (Hibernate auto-schema) |

### Entidades

| Tabla | Clave primaria | Descripción |
|---|---|---|
| `song_likes` | Composite `(user_id, song_id)` | Likes de canciones |
| `hidden_songs` | UUID | Canciones ocultas, scope `(user_id, song_id, album_id)` |
| `user_library` | UUID | Álbumes/artistas en biblioteca |
| `user_genre_preferences` | Composite `(user_id, genre_id)` | Géneros del onboarding |
| `user_artist_preferences` | Composite `(user_id, artist_id)` | Artistas del onboarding |
| `play_history` | UUID | Historial de reproducción con duración escuchada |

---

## Endpoints

Las interfaces de controller se generan desde `src/main/resources/openapi.yml`. Todos los endpoints requieren el header `X-User-Id` propagado por el api-gateway.

### Preferencias

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/preferences/genres` | Obtener géneros del onboarding del usuario |
| `POST` | `/preferences/genres` | Guardar géneros (reemplaza todos los anteriores) |
| `GET` | `/preferences/artists` | Obtener artistas favoritos del usuario |
| `POST` | `/preferences/artists` | Guardar artistas (reemplaza todos los anteriores) |

### Interacciones con canciones

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/songs/{songId}/like` | Dar like a una canción |
| `DELETE` | `/songs/{songId}/like` | Quitar like |
| `GET` | `/songs/{songId}/like/status` | Verificar si el usuario le dio like |
| `GET` | `/songs/liked` | Canciones con like del usuario (paginado) |
| `POST` | `/albums/{albumId}/songs/{songId}/hide` | Ocultar canción en un álbum |
| `DELETE` | `/albums/{albumId}/songs/{songId}/hide` | Desocultar canción |
| `GET` | `/albums/{albumId}/hidden-songs` | Canciones ocultas en un álbum |

### Biblioteca

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/library?type=ALBUM\|ARTIST` | Obtener ítems de la biblioteca (paginado) |
| `POST` | `/library` | Agregar álbum o artista a la biblioteca |
| `DELETE` | `/library/{type}/{itemId}` | Eliminar ítem de la biblioteca |

### Historial de reproducción

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/play-history` | Historial del usuario (paginado, orden desc) |
| `POST` | `/play-history` | Registrar reproducción con duración escuchada |

---

## Kafka — Productor

Este servicio emite eventos que el `catalog-service` consume para actualizar sus contadores cacheados:

| Topic | Cuándo se emite | Consumidor | Efecto |
|---|---|---|---|
| `song.played` | Al registrar una reproducción | catalog-service | `song.play_count + 1` |
| `song.liked` | Al dar like a una canción | catalog-service | `song.likes_count + 1` |
| `song.unliked` | Al quitar like | catalog-service | `song.likes_count - 1` |

> **Formato del mensaje:** UUID en texto plano (ID de la canción afectada).

---

## Reglas de negocio

- **`X-User-Id` header:** Todos los endpoints lo leen del request — lo propaga el api-gateway desde el JWT. El servicio nunca valida el token directamente.
- **Likes idempotentes:** `likeSong` verifica existencia antes de insertar; no lanza error si ya existe.
- **Hidden songs con scope de álbum:** Ocultar una canción en el álbum A no la oculta en el álbum B — el scope es `(user_id, song_id, album_id)`.
- **Preferencias por reemplazo:** `saveGenrePreferences` y `saveArtistPreferences` hacen DELETE ALL + INSERT — no son aditivas.
- **Biblioteca idempotente:** `addToLibrary` verifica existencia por `(user_id, item_type, item_id)` antes de insertar.
- **Referencias cross-service:** IDs de canciones, artistas y géneros se almacenan como UUID planos — sin FK hacia `catalog-service`.

---

## Estructura del proyecto

```
interaction-service/
├── src/
│   ├── main/
│   │   ├── java/com/rubymusic/interaction/
│   │   │   ├── InteractionServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── LibraryController.java           ← implements LibraryApi
│   │   │   │   ├── PlayHistoryController.java        ← implements PlayHistoryApi
│   │   │   │   ├── PreferencesController.java        ← implements PreferencesApi
│   │   │   │   └── SongInteractionsController.java   ← implements SongInteractionsApi
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── mapper/
│   │   │   │   └── PlayHistoryMapper.java            ← MapStruct: PlayHistory → PlayHistoryResponse
│   │   │   ├── model/
│   │   │   │   ├── enums/
│   │   │   │   │   └── LibraryItemType.java           ← ALBUM | ARTIST
│   │   │   │   ├── id/
│   │   │   │   │   ├── SongLikeId.java                ← @EmbeddedId (userId, songId)
│   │   │   │   │   ├── UserArtistPreferenceId.java    ← @EmbeddedId (userId, artistId)
│   │   │   │   │   └── UserGenrePreferenceId.java     ← @EmbeddedId (userId, genreId)
│   │   │   │   ├── HiddenSong.java
│   │   │   │   ├── PlayHistory.java
│   │   │   │   ├── SongLike.java
│   │   │   │   ├── UserArtistPreference.java
│   │   │   │   ├── UserGenrePreference.java
│   │   │   │   └── UserLibrary.java
│   │   │   ├── repository/
│   │   │   │   ├── HiddenSongRepository.java
│   │   │   │   ├── PlayHistoryRepository.java
│   │   │   │   ├── SongLikeRepository.java
│   │   │   │   ├── UserArtistPreferenceRepository.java
│   │   │   │   ├── UserGenrePreferenceRepository.java
│   │   │   │   └── UserLibraryRepository.java
│   │   │   └── service/
│   │   │       ├── PlayHistoryService.java
│   │   │       ├── SongInteractionService.java
│   │   │       ├── UserLibraryService.java
│   │   │       ├── UserPreferenceService.java
│   │   │       └── impl/
│   │   │           ├── PlayHistoryServiceImpl.java
│   │   │           ├── SongInteractionServiceImpl.java
│   │   │           ├── UserLibraryServiceImpl.java
│   │   │           └── UserPreferenceServiceImpl.java
│   │   └── resources/
│   │       ├── application.yml       ← nombre + import config-server
│   │       └── openapi.yml           ← contrato OpenAPI 3.0.3 completo
│   └── test/
│       └── java/com/rubymusic/interaction/
│           └── InteractionServiceApplicationTests.java
└── pom.xml
```

---

## Manejo de errores

| Excepción | HTTP |
|---|---|
| `NoSuchElementException` | `404 Not Found` |
| `IllegalArgumentException` | `400 Bad Request` |
| `DataIntegrityViolationException` | `409 Conflict` |
| `Exception` (genérico) | `500 Internal Server Error` |

---

## Variables de entorno

Inyectadas desde `config-server` (`config/interaction-service.yml`):

| Variable | Descripción | Default |
|---|---|---|
| `DB_USERNAME` | Usuario PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña PostgreSQL | `password` |

---

## Build & Run

```bash
# Build (genera interfaces y DTOs desde openapi.yml)
mvn clean package -DskipTests

# Run
mvn spring-boot:run

# Test
mvn test -Dtest=InteractionServiceApplicationTests
```

> Requiere `discovery-service`, `config-server`, PostgreSQL en `localhost:5432` con `interaction_db` creada, y Kafka en `localhost:9092`.
