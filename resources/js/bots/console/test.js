// Run this in a node REPL
// .load test.js

// Load dependencies
.load web3_metadata.js
.load bot.js

// A map from current input text to suggestion titles
var suggestionTests = {
    ",": [],
    ")": [],
    "(": [],
    "a)": [],
// Expected?
//    "a,": [],
//    "a(": [],
    "c": ["console"],
    "console.": ["log(text)"]
};

// Mock localStorage, necessary for suggestions functions in bot.js
var STORE = {};
var localStorage = function() {};
localStorage.getItem = function(k) { return STORE[k]; };
localStorage.setItem = function(k, v) { STORE[k] = v; };

var checkSuggestion = function(input) {
    var suggestions = getJsSuggestions(input, {});
    var titles = suggestions.map(function(suggestion) {
        return suggestion.title;
    });
    var expectedTitles = suggestionTests[input];
    var iseq = JSON.stringify(titles) == JSON.stringify(expectedTitles);
    console.log("CHECK", input, "   ", iseq);
    if (!iseq) {
        console.log("EXPECTED", expectedTitles);
        console.log("ACTUAL", titles);
    }
};

// Run tests
Object.keys(suggestionTests).forEach(checkSuggestion);
