Inkball — Java + Processing 🎨⚙️
<p align="center"> <img alt="Inkball banner" src="docs/banner.png" width="820"> </p> <p align="center"> <a href="https://gradle.org/"><img alt="Gradle" src="https://img.shields.io/badge/build-Gradle-02303A"></a> <a href="#"><img alt="Java" src="https://img.shields.io/badge/Java-8+-red"></a> <a href="#"><img alt="Processing" src="https://img.shields.io/badge/Processing-3.x-blue"></a> <a href="#"><img alt="License" src="https://img.shields.io/badge/license-Academic-green"></a> </p>

A minimalist, testable re-implementation of the classic Inkball puzzle: guide colored balls into matching holes using player-drawn lines. Built for Java 8 with the Processing library and Gradle.

✨ Features
- 18×18 tile levels rendered on a 576×640 canvas
- Balls spawn at intervals, bounce, change color on special walls, and get attracted into holes
- Player-drawn lines reflect balls and vanish on impact
- Persistent scoring, level timer, pause/restart, and level chaining
- Extension: Breakable colored bricks that disappear after three valid hits (grey bricks accept any color)

🕹️ Gameplay & Controls
- Draw line: Left mouse drag
- Erase line: Right click (or Ctrl + Left click)
- Pause / Unpause: Space (shows *** PAUSED *** in top bar)
- Restart level / game: R
- Spawn interval and time-left are visible in the top bar. When time hits 0, show === TIME’S UP ===.
  On level completion, remaining time converts to score while yellow tiles orbit the border.

🧩 Design Choices (OOP)
This project emphasizes clarity, testability, and extensibility:
- App (extends PApplet): central loop, orchestration, drawing, timers, score, level flow.
- GameObject: shared position & collision skeleton for all entities (balls, walls, holes, lines, spawners).
- Ball: movement, color changes on special wall collision, attraction into holes, spawn logic.
- Line: hitbox from polyline segments; handles reflection and self-removal on collision.
- Wall / Brick: wall collision; brick specializes wall with a 3-hit removal rule (color-gated unless grey).
- Spawner: random/predefined locations for ball emergence at configured intervals.
Fields are private/protected with accessors for controlled mutation and future extension. (Summary drawn from the project report.)

⚙️ Tech Stack
- Language: Java 8 (no newer language features)
- Runtime/Graphics: Processing (processing.core, processing.data)
- Build/Test: Gradle, JUnit, JaCoCo (≥90% combined branches + instructions coverage encouraged)
