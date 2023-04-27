(ns utils.worklets.onboarding-carousel)

(def worklets (js/require "../src/js/worklets/onboarding_carousel.js"))

(defn dynamic-progress-bar-width
  [static-progress-bar-width progress]
  (.dynamicProgressBarWidth ^js worklets static-progress-bar-width progress))

(defn carousel-left-position
  [window-width progress]
  (.carouselLeftPosition ^js worklets window-width progress))
