const waitToNavigate = (duration) => new Promise((resolve) => setTimeout(() => resolve(), duration));

const SUPER_SECRET_PASSWORD = 'password';

const loginToHomePage = async () => {
  if (device.getPlatform() === 'ios') {
    await device.setStatusBar({
      time: '12:34',
      dataNetwork: 'wifi',
      wifiBars: '3',
      batteryState: 'charging',
      batteryLevel: '100',
    });
  }
  await device.reloadReactNative();
  await element(by.id('terms-of-service')).tap();
  await waitToNavigate(400);
  await element(by.id('get-started')).tap();
  await waitToNavigate(300);
  await element(by.id('generate-keys')).tap();
  await waitToNavigate(200);
  await element(by.text('Next')).tap();
  await waitToNavigate(700);
  await element(by.text('Next')).tap();
  await waitToNavigate(700);
  await element(by.id('password-placeholder')).typeText(SUPER_SECRET_PASSWORD);
  await element(by.id('confirm-password-placeholder')).typeText(SUPER_SECRET_PASSWORD);
  await element(by.text('Next')).tap();
  await waitToNavigate(200);
  await element(by.id('browser-stack')).tap();
  await waitToNavigate(400);
  await element(by.text('Quo2.0 Preview')).tap();
  await waitToNavigate(400);
};

describe('Default Renders', () => {
  beforeAll(async () => loginToHomePage());
  beforeEach(async () => {});
  afterEach(async () => {
    await element(by.id('back-button')).tap();
    await waitToNavigate(200);
  });

  it(`button page should match image render`, async () => {
    await element(by.id(`quo2-:button`)).tap();
    await waitToNavigate(200);
    const res = await jestExpect(`button`).toMatchImageSnapshot();
  });
});
