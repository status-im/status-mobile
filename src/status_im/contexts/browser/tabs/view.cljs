(ns status-im.contexts.browser.tabs.view
  (:require [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im.contexts.browser.tabs.tab-window :as tab-window]
            [utils.re-frame :as rf]))

(defn use-transition-animation
  [show?]
  (let [opacity-value (reanimated/use-shared-value (if show? 1 0))
        scale-value   (reanimated/use-shared-value (if show? 1 0.8))]
    (rn/use-effect
     (fn []
       (if show?
         (do (reanimated/animate-shared-value-with-timing opacity-value 1 200 :easing2)
             (reanimated/animate-shared-value-with-timing scale-value 1 200 :easing2))
         (do (reanimated/animate-shared-value-with-timing opacity-value 0 600 :easing2)
             (reanimated/animate-shared-value-with-timing scale-value 0.6 200 :easing2))))
     [show?])
    {:opacity          (reanimated/interpolate scale-value [0.6 0.8 1] [0 0.7 1])
     :transform-origin :top
     :transform        [{:scale scale-value}]}))

(defn view
  [{:keys [scroll-ref x-translation-value]}]
  (let [browser-mode  (rf/sub [:browser/mode])
        tab-ids       (rf/sub [:browser/tab-ids])
        browser-style (use-transition-animation (= browser-mode :browser-mode/browser))]
    [reanimated/view
     {:style [browser-style {:flex 1}]}
     [reanimated/scroll-view
      {:horizontal                        true
       :ref                               scroll-ref
       :shows-horizontal-scroll-indicator false
       :shows-vertical-scroll-indicator   false
       :scroll-enabled                    false}
      (map-indexed (fn [idx tab-id]
                     ^{:key (str idx "-" tab-id)}
                     [tab-window/view tab-id idx x-translation-value])
                   tab-ids)]]))
