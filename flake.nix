{
  description = "OldStats dev environment (Node/TypeScript server + web, Kotlin RuneLite plugin)";

  inputs = {
    nixpkgs.url = "nixpkgs";
  };

  outputs = { self, nixpkgs }:
    let
      systems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
      forAllSystems = f: nixpkgs.lib.genAttrs systems (system: f system);
    in
    {
      devShells = forAllSystems (system:
        let
          pkgs = import nixpkgs { inherit system; };
          isLinux = pkgs.lib.hasSuffix "linux" system;
        in {
          default = pkgs.mkShell {
            packages = [
              pkgs.nodejs_22
              pkgs.jdk17
              pkgs.gradle
              pkgs.chromium
            ];
            # NixOS's Playwright browser downloads are generic-glibc binaries
            # that don't run against the Nix store's non-FHS library layout.
            # playwright-driver.browsers ships pre-patched builds instead —
            # web/package.json pins @playwright/test to match its version.
            shellHook = pkgs.lib.optionalString isLinux ''
              export PLAYWRIGHT_BROWSERS_PATH=${pkgs.playwright-driver.browsers}
              export PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS=true
            '';
          };
        });
    };
}
