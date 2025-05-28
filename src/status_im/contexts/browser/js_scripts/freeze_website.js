(function () {
  // pause media elements
  var mediaElements = document.querySelectorAll('video:not([paused]), audio:not([paused])');
  mediaElements.forEach(function (element) {
    element.setAttribute('data-frozen-playback-state', element.paused ? 'paused' : 'playing');
    element.setAttribute('data-frozen', 'true');
    element.pause();
  });

  // Suspend expensive animations and transitions
  var animatedElements = document.querySelectorAll('*[style*="animation"], *[style*="transition"]');
  animatedElements.forEach(function (element) {
    element.setAttribute('data-frozen-animation-play-state', element.style.animationPlayState);
    element.setAttribute('data-frozen-transition-property', element.style.transitionProperty);
    element.style.animationPlayState = 'paused';
    element.style.transitionProperty = 'none';
  });

  // Suspend keyframe animations
  var keyframeAnimatedElements = document.querySelectorAll('*[style*="animation-name"]');
  keyframeAnimatedElements.forEach(function (element) {
    element.setAttribute('data-frozen-animation-name', element.style.animationName);
    element.style.animationName = 'none';
  });
})();
