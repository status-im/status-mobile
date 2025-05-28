(function () {
  // resume media elements
  var pausedMediaElements = document.querySelectorAll('video[data-frozen="true"], audio[data-frozen="true"]');
  pausedMediaElements.forEach(function (element) {
    if (element.getAttribute('data-frozen-playback-state') === 'playing') {
      element.play();
    }
    element.removeAttribute('data-frozen');
    element.removeAttribute('data-frozen-playback-state');
  });

  // Resume animations and transitions
  var animatedElements = document.querySelectorAll('*[style*="animation"], *[style*="transition"]');
  animatedElements.forEach(function (element) {
    element.style.animationPlayState = element.getAttribute('data-frozen-animation-play-state') || 'running';
    element.style.transitionProperty = element.getAttribute('data-frozen-transition-property') || '';
    element.removeAttribute('data-frozen-animation-play-state');
    element.removeAttribute('data-frozen-transition-property');
  });

  // Resume keyframe animations
  var keyframeAnimatedElements = document.querySelectorAll('*[style*="animation-name"]');
  keyframeAnimatedElements.forEach(function (element) {
    element.style.animationName = element.getAttribute('data-frozen-animation-name') || '';
    element.removeAttribute('data-frozen-animation-name');
  });
})();
