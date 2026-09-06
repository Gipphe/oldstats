{
  description = "OldStats dev environment (Node/TypeScript server + web, Kotlin RuneLite plugin)";

  inputs = {
    nixpkgs.url = "nixpkgs";
  };

  outputs =
    { self, nixpkgs }:
    let
      systems = [
        "x86_64-linux"
        "aarch64-linux"
        "x86_64-darwin"
        "aarch64-darwin"
      ];
      forAllSystems = f: nixpkgs.lib.genAttrs systems (system: f system);
    in
    {
      devShells = forAllSystems (
        system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
          isLinux = pkgs.lib.hasSuffix "linux" system;
        in
        {
          default = pkgs.mkShell {
            packages = builtins.attrValues {
              inherit (pkgs)
                nodejs_22
                jdk17
                gradle
                chromium
                ;
            };
            # NixOS's Playwright browser downloads are generic-glibc binaries
            # that don't run against the Nix store's non-FHS library layout.
            # playwright-driver.browsers ships pre-patched builds instead —
            # web/package.json pins @playwright/test to match its version.
            shellHook = pkgs.lib.optionalString isLinux ''
              export PLAYWRIGHT_BROWSERS_PATH=${pkgs.playwright-driver.browsers}
              export PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=true
            '';
          };
        }
      );

      packages = forAllSystems (
        system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
          # Also used at runtime by the wrapper below — better-sqlite3's native
          # addon is compiled against whichever Node's V8 headers are used to
          # build it, and fails to dlopen (undefined V8 symbols) if later run
          # under a different major version's Node. Must stay the same value.
          nodejs = pkgs.nodejs_22;
        in
        {
          server = pkgs.buildNpmPackage {
            pname = "oldstats-server";
            version = "0.1.0";
            src = ./server;
            inherit nodejs;

            # Regenerate with the hash Nix reports on a mismatch if package-lock.json changes.
            npmDepsHash = "sha256-rycuR6XEG/rNmWknj7o3qXBU2ixk9+W1mQ5TNBbXMwA=";

            # better-sqlite3's postinstall (prebuild-install) tries to download a
            # prebuilt binary over the network, which the sandboxed build phase
            # doesn't have; forcing a from-source build makes it fall back to
            # node-gyp instead, which just needs python3 + a C++ toolchain.
            npm_config_build_from_source = "true";
            nativeBuildInputs = [
              pkgs.python3
              pkgs.makeWrapper
            ];

            npmBuildScript = "build";

            installPhase = ''
              runHook preInstall

              mkdir -p $out/lib/oldstats-server
              cp -r dist node_modules package.json $out/lib/oldstats-server/

              makeWrapper ${nodejs}/bin/node $out/bin/oldstats-server \
                --add-flags "$out/lib/oldstats-server/dist/index.js" \
                --run 'export OLDSTATS_DB_PATH="''${OLDSTATS_DB_PATH:-''${XDG_DATA_HOME:-$HOME/.local/share}/oldstats/oldstats.db}"'

              runHook postInstall
            '';

            meta.mainProgram = "oldstats-server";
          };

          web = pkgs.buildNpmPackage {
            pname = "oldstats-web";
            version = "0.1.0";
            src = ./web;
            inherit nodejs;

            # Regenerate with the hash Nix reports on a mismatch if package-lock.json changes.
            npmDepsHash = "sha256-GnZK8CSpuG9j3yd8gmRJJKRPwfu5ILLbgIUg+V63O2o=";

            nativeBuildInputs = [ pkgs.makeWrapper ];

            # Vite bakes VITE_* vars into the built JS bundle at `vite build`
            # time (import.meta.env.VITE_API_URL is statically replaced) —
            # unlike the server, this can't be overridden at runtime, so the
            # backend URL is fixed here. Edit and rebuild to point elsewhere.
            env.VITE_API_URL = "http://localhost:4000";

            npmBuildScript = "build";

            installPhase = ''
              runHook preInstall

              # vite.config.ts is intentionally not copied: `vite preview` only
              # needs root + build.outDir, which already match the defaults,
              # and loading a config file would make Vite try to write a
              # bundled copy next to node_modules, which is read-only here.
              # Its port setting is passed explicitly below instead.
              mkdir -p $out/lib/oldstats-web
              cp -r dist node_modules package.json $out/lib/oldstats-web/

              makeWrapper ${nodejs}/bin/node $out/bin/oldstats-web \
                --add-flags "$out/lib/oldstats-web/node_modules/vite/bin/vite.js" \
                --add-flags "preview --host --port 3000" \
                --chdir $out/lib/oldstats-web

              runHook postInstall
            '';

            meta.mainProgram = "oldstats-web";
          };

          plugin = pkgs.stdenv.mkDerivation (finalAttrs: {
            pname = "oldstats-plugin";
            version = "1.0.0";
            src = ./plugin;

            # Deprecated Gradle features used by the Kotlin/shadow plugins
            # make this build incompatible with Gradle 9 — must stay 8.x,
            # matching the version pinned by plugin/gradle-wrapper.properties.
            nativeBuildInputs = [ pkgs.gradle_8 ];

            # Gradle has no built-in reproducible dependency fetching, so
            # nixpkgs proxies + records every Maven request into deps.json
            # (a MITM cache), replayed here instead of hitting the network.
            # Regenerate via:
            #   nix build .#plugin.mitmCache.updateScript -o /tmp/update-oldstats-plugin-deps
            #   /tmp/update-oldstats-plugin-deps
            mitmCache = pkgs.gradle_8.fetchDeps {
              pkg = finalAttrs.finalPackage;
              data = ./plugin/deps.json;
            };
            __darwinAllowLocalNetworking = true;

            gradleBuildTask = "shadowJar";

            installPhase = ''
              runHook preInstall
              mkdir -p $out
              cp build/libs/*-all.jar $out/oldstats-plugin.jar
              runHook postInstall
            '';
          });
        }
      );

      apps = forAllSystems (
        system:
        let
          pkgs = nixpkgs.legacyPackages.${system};
        in
        {
          server = {
            type = "app";
            program = "${self.packages.${system}.server}/bin/oldstats-server";
          };
          web = {
            type = "app";
            program = "${self.packages.${system}.web}/bin/oldstats-web";
          };
          # Launches a real RuneLite client with the plugin preloaded (see
          # OldStatsPluginTest.kt / build.gradle.kts's runClient task). Unlike
          # `packages.plugin`, this isn't built hermetically: it opens a real
          # GUI window on the host's display and needs a writable Gradle
          # project directory (build/ cache, ~/.gradle), so it just shells
          # out to `gradle runClient` against your actual checkout rather
          # than the read-only Nix store copy of the flake's source.
          runelite = {
            type = "app";
            program = "${pkgs.writeShellApplication {
              name = "oldstats-runelite";
              runtimeInputs = [ pkgs.gradle_8 ];
              text = ''
                repo_root="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
                cd "$repo_root/plugin"

                # RuneLite's UI is plain AWT/Swing/LWJGL with no native Wayland
                # backend, so it needs an X server — on a Wayland desktop that
                # means XWayland. Many wlroots-based compositors (Hyprland
                # included) don't export DISPLAY into every shell, so fall
                # back to whatever live X11 socket we can find rather than
                # failing outright with an opaque HeadlessException.
                if [ -z "''${DISPLAY:-}" ]; then
                  for sock in /tmp/.X11-unix/X*; do
                    [ -S "$sock" ] || continue
                    export DISPLAY=":''${sock##*/X}"
                    echo "DISPLAY was unset; using detected X11 socket $DISPLAY (XWayland?)" >&2
                    break
                  done
                fi
                if [ -z "''${DISPLAY:-}" ]; then
                  echo "warning: DISPLAY is unset and no X11 socket was found in /tmp/.X11-unix." >&2
                  echo "RuneLite needs a running X server (or XWayland, on Wayland desktops) to open a window." >&2
                fi

                # --no-daemon: a persistent Gradle daemon snapshots its
                # environment (DISPLAY, XAUTHORITY, ...) once at startup and
                # reuses it for every later invocation, including from
                # unrelated shells/sessions. For a GUI-launching task that's
                # actively wrong — a stale daemon started without a display
                # (e.g. over SSH, or by a headless CI/tool run) would make
                # every subsequent `nix run .#runelite` fail with a
                # HeadlessException even when run from a real desktop
                # session. A fresh JVM per run always has the current
                # invocation's actual environment.
                exec gradle --no-daemon runClient
              '';
            }}/bin/oldstats-runelite";
          };
          default = self.apps.${system}.server;
        }
      );
    };
}
