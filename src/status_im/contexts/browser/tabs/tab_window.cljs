(ns status-im.contexts.browser.tabs.tab-window
  (:require [quo.context :as quo.context]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.freeze :as freeze]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [react-native.view-shot :as view-shot]
            [reagent.core :as reagent]
            [status-im.contexts.browser.constants :as browser.constants]
            [status-im.contexts.browser.core :as browser]
            [status-im.contexts.browser.hooks :as hooks]
            [status-im.contexts.browser.native-dapps :as native-dapps]
            [status-im.contexts.browser.tabs.webview :as webview]
            [utils.re-frame :as rf]))

(defn interpolate-x-position
  [x-value tab-idx output-values]
  (let [x-tab      (browser/tab-position tab-idx)
        x-tab-prev (browser/tab-position (dec tab-idx))
        x-tab-next (browser/tab-position (inc tab-idx))]
    (reanimated/interpolate x-value
                            [x-tab-prev x-tab x-tab-next]
                            output-values
                            {:extrapolateLeft  "clamp"
                             :extrapolateRight "clamp"})))

(defn tab-container
  [{:keys [idx x-translation-value]} & children]
  (let [theme             (quo.context/use-theme)
        scale-down-amount 0.95]
    (into [reanimated/view
           {:style
            [{:transform [{:scale (interpolate-x-position x-translation-value
                                                          idx
                                                          [scale-down-amount 1 scale-down-amount])}]}
             {:width            browser.constants/browser-width
              :height           browser.constants/browser-height
              :border-radius    20
              :transform-origin :bottom
              :overflow         :hidden
              :background-color (colors/theme-colors colors/white colors/neutral-95 theme)}]}]
          children)))

(defn freeze-placeholder
  [tab-id]
  (let [screenshot-url (rf/sub [:browser/tab-screenshot tab-id])]
    [rn/view
     {:style
      {:width         browser.constants/browser-width
       :height        browser.constants/browser-height
       :border-radius 20}}
     [rn/image
      {:source screenshot-url
       :style  {:flex 1}}]]))

(defn view
  [tab-id idx x-translation-value]
  (let [url                           (rf/sub [:browser/tab-url tab-id])
        tab-type                      (rf/sub [:browser/tab-type tab-id])
        focused-tab-idx               (rf/sub [:browser/focused-tab-idx])
        ;;freeze-tab?                              (browser/freeze-tab? idx focused-tab-idx)
        unfocused?                    (not= idx focused-tab-idx)
        {:keys [ref options capture]} (hooks/use-screenshot-tab tab-id)
        screenshot-interval           (rn/use-ref-atom nil)
        [freeze? set-freeze]          (rn/use-state unfocused?)
        placeholder-opacity-value     (reanimated/use-shared-value (if unfocused? 1 0))]

    (rn/use-effect (fn []
                     (reanimated/animate-shared-value-with-delay placeholder-opacity-value
                                                                 (if unfocused? 1 0)
                                                                 200
                                                                 :easing2
                                                                 200)
                     (js/setTimeout (fn [] (set-freeze unfocused?)) 400))
                   [unfocused?])

    (rn/use-effect (fn []
                     (when-not freeze?
                       (capture)))
                   [freeze?])

    ;; (rn/use-unmount (fn [] (js/clearInterval @screenshot-interval)))
    ;; (rn/use-effect (fn []
    ;;                  (if freeze?
    ;;                    (when @screenshot-interval
    ;;                      (js/clearInterval @screenshot-interval))
    ;;                    (let [interval-id (js/setInterval capture 5000)]
    ;;                      (capture)
    ;;                      (reset! screenshot-interval interval-id))))
    ;;                [freeze?])
    [view-shot/view
     {:ref     ref
      :style   {:flex 1}
      :options options}
     [tab-container
      {:idx                 idx
       :x-translation-value x-translation-value}
      [freeze/view
       {:freeze      unfocused?
        :placeholder (reagent/as-element
                      [freeze-placeholder tab-id])}
       (if (= :tab/native tab-type)
         [rn/view
          {:style {:margin-top (- 10 safe-area/top)
                   :z-index    5
                   :flex       1}}
          (get native-dapps/views url)]
         [webview/view
          {:tab-id tab-id
           :url    url}])]
      (when freeze?
        [reanimated/view
         {:pointer-events :none
          :style          [{:opacity placeholder-opacity-value}
                           {:position :absolute :left 0 :right 0 :bottom 0 :top 0}]}
         [freeze-placeholder tab-id]])]]))
