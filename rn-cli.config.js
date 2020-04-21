const blacklist = require('metro').createBlacklist;

module.exports = {
    getBlacklistRE: function() {
        return blacklist([/(desktop|mobile)\/js_files\/.*/]);
    }
};
