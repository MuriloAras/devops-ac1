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
    baseUrl: 'http://localhost:3002',
    specPattern: 'cypress/e2e/**/*.cy.js',
    supportFile: false,
    testIsolation: true,
    setupNodeEvents(on) {
      on('before:browser:launch', (browser, launchOptions) => {
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
