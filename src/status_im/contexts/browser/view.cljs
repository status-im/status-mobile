(ns status-im.contexts.browser.view
  (:require [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.browser.footer.view :as footer]
            [status-im.contexts.browser.hooks :as hooks]
            [status-im.contexts.browser.tabs.tabs-preview :as tabs-preview]
            [status-im.contexts.browser.tabs.view :as tabs]))

(defn view
  []
  (let [scroll-ref                                 (reanimated/use-animated-ref)
        {:keys [gesture-prop x-translation-value]} (hooks/use-swipe-gesture scroll-ref)]
    [rn/view
     {:style {:background-color colors/neutral-100
              :padding-top      safe-area/top
              :flex             1}}
     ;; NOTE: the browser tab windows live in a scroll-view (with disabled gestures) inside
     ;; `tabs/view`. The scrolling is handled by the `gesture-detector`, which wraps the
     ;; `footer/view`.
     [tabs/view
      {:scroll-ref          scroll-ref
       :x-translation-value x-translation-value}]
     ;; NOTE: tabs preview is absolutely positioned inside `tabs-preview/view` and is conditionally
     ;; rendered depending on the `:browser/mode` state
     [tabs-preview/view]
     [gesture/gesture-detector {:gesture gesture-prop}
      [footer/view]]]))
