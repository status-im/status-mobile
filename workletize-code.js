const process = require('process');
const babel = require('@babel/core');
const plugin = require('./node_modules/react-native-reanimated/plugin');

function transformString(inputString) {
  return babel.transformSync(inputString, {
    filename: '/dev/null',
    compact: false,
    plugins: [plugin],
  }).code;
}

process.stdin.setEncoding('utf8');
let input = '';
process.stdin.on('data', (chunk) => {
  input += chunk;
});

process.stdin.on('end', () => {
  const result = transformString(input);
  process.stdout.write(result);
});
