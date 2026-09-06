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
              mkdir -p $out/lib/oldstats-web
              cp -r dist node_modules package.json $out/lib/oldstats-web/

              makeWrapper ${nodejs}/bin/node $out/bin/oldstats-web \
                --add-flags "$out/lib/oldstats-web/node_modules/vite/bin/vite.js" \
                --add-flags "preview --host" \
                --chdir $out/lib/oldstats-web

              runHook postInstall
            '';

            meta.mainProgram = "oldstats-web";
          };
        }
      );

      apps = forAllSystems (system: {
        server = {
          type = "app";
          program = "${self.packages.${system}.server}/bin/oldstats-server";
        };
        web = {
          type = "app";
          program = "${self.packages.${system}.web}/bin/oldstats-web";
        };
        default = self.apps.${system}.server;
      });
    };
}
