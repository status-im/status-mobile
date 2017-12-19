const m = require('module');
const originalLoader = m._load;

/*
 Hook `require` so that RN abuse of require does not break when running tests in nodejs.
*/

m._load = function hookedLoader(request, parent, isMain) {
  if (request.match(/.jpeg|.jpg|.png$/)) {
    return { uri: request };
  }

  return originalLoader(request, parent, isMain);
};
