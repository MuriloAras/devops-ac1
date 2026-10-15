// A plain ESM config also works with the CLI bundled in cypress/included,
// without installing node_modules again inside the test container.
export default {
  viewportWidth: 1600,
  viewportHeight: 900,
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
    setupNodeEvents(on) {
      on('before:browser:launch', (browser, launchOptions) => {
        // The AUT viewport and the headless screen are separate sizes. Both
        // must be wide enough to avoid clipping screenshots and video.
        if (browser.name === 'electron' && browser.isHeadless) {
          launchOptions.preferences.width = 1600
          launchOptions.preferences.height = 900
        }
        if (browser.family === 'chromium' && browser.name !== 'electron' && browser.isHeadless) {
          launchOptions.args.push('--window-size=1600,900')
          launchOptions.args.push('--force-device-scale-factor=1')
        }
        return launchOptions
      })
    },
  },
}
