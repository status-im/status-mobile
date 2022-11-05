const data = require('./visual-tests.json')

const waitToNavigate = duration => new Promise(resolve => setTimeout(() => resolve(), duration));

describe('Example (hello)', () => {
    beforeAll(async () => {
        if (device.getPlatform() === 'ios') {
            await device.setStatusBar({ time: '12:34', dataNetwork: 'wifi', wifiBars: '3', batteryState: 'charging', batteryLevel: '100' });
        }
        await device.reloadReactNative();

        await element(by.text('I accept Status Terms of Use')).tap();
        // close alert
        await element(by.text('Get started')).tap();
        await waitToNavigate(200);

        await element(by.text('Generate keys')).tap();
        await waitToNavigate(200);

        await element(by.text('Next')).tap();
        await waitToNavigate(700);
        await element(by.text('Next')).tap();
        await waitToNavigate(700);
        // await element(by.text('Password...')).typeText('infinitbility@gmail.com');
        // await element(by.text('Confirm your password...')).typeText('infinitbility@gmail.com');
        await element(by.text('Next')).tap();
        await waitToNavigate(700);
        await element(by.text('Maybe later')).tap();
        await waitToNavigate(700);
        await element(by.text("Let's go")).tap();
        await waitToNavigate(700);

        await element(by.text("1")).tap();
        await waitToNavigate(400);
        await element(by.text("Quo2.0 Preview")).tap();
        await waitToNavigate(400);
    })
    beforeEach(async () => {
    });
    afterEach(async () => {
        await element(by.text("1")).tap();
        await waitToNavigate(200);
        await element(by.text("Quo2.0 Preview")).tap();
        await waitToNavigate(200);
    });

    const pages = Object.keys(data)

    it(`button page should match snapshot`, async () => {
        await element(by.text(`:button`)).tap();
        await waitToNavigate(200);
        const res = await jestExpect(`default-render-button`).toMatchImageSnapshot();
    })

});