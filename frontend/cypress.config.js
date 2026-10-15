// A plain ESM config also works with the CLI bundled in cypress/included,
// without installing node_modules again inside the test container.
export default {
  viewportWidth: 1440,
  viewportHeight: 1000,
  video: true,
  screenshotOnRunFailure: true,
  screenshotsFolder: 'cypress/screenshots',
  videosFolder: 'cypress/videos',
  downloadsFolder: 'cypress/downloads',
  trashAssetsBeforeRuns: true,
  reporter: 'junit',
  reporterOptions: {
    mochaFile: 'cypress/results/junit-[hash].xml',
    toConsole: true,
  },
  e2e: {
    // Override with CYPRESS_BASE_URL=http://frontend:80 in Docker Compose.
    baseUrl: 'http://localhost:3002',
    specPattern: 'cypress/e2e/**/*.cy.js',
    supportFile: false,
    testIsolation: true,
  },
}
