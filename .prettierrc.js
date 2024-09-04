module.exports = {
  arrowParens: 'always',
  printWidth: 120,
  semi: true,
  singleQuote: true,
  tabWidth: 2,
  trailingComma: 'all',
  useTabs: false,

  // JSON sorting
  jsonSortOrder: '{ "/.*/": "caseInsensitiveLexical" } ',
  plugins: ['prettier-plugin-sort-json'],
};
