(ns status-im.contexts.browser.tabs.tabs-preview
  (:require [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.browser.components.dapp-icon :as dapp-icon]
            [status-im.contexts.browser.constants :as browser.constants]
            [utils.re-frame :as rf]))

(def preview-width
  (/ (- browser.constants/browser-width (* 3 20)) 2))

(def preview-height (* preview-width 1.2))

(defn on-press-preview
  [tab-id]
  (rf/dispatch [:browser/show-browser])
  (rf/dispatch [:browser/focus-tab tab-id]))

(defn tab-preview
  [tab-id]
  (let [dapp        (rf/sub [:browser/dapp-for-tab tab-id])
        preview-url (rf/sub [:browser/tab-screenshot tab-id])]
    [rn/pressable
     {:on-press #(on-press-preview tab-id)
      :style    {:align-items      :center
                 :justify-content  :center
                 :background-color colors/neutral-80
                 :border-radius    12
                 :width            preview-width
                 :height           preview-height}}
     [rn/image
      {:source      {:uri preview-url}
       :resize-mode :cover
       :style       {:border-radius 12
                     :width         preview-width
                     :height        preview-height}}]
     [rn/view
      {:style {:position        :absolute
               :bottom          -8
               :left            0
               :right           0
               :justify-content :center
               :align-items     :center}}
      [rn/view
       {:style {:width            32
                :height           32
                :background-color colors/neutral-20
                :border-radius    20
                :justify-content  :center
                :align-items      :center}}
       [dapp-icon/view
        {:dapp             dapp
         :size             24
         :background-color colors/neutral-80}]]]]))

(defn use-hide
  [show?]
  (let [opacity-value (reanimated/use-shared-value (if show? 1 0))]
    (rn/use-effect
     (fn []
       (if show?
         (reanimated/animate-shared-value-with-timing opacity-value 1 200 :easing2)
         (reanimated/animate-shared-value-with-timing opacity-value 0 200 :easing2)))
     [show?])
    {:opacity opacity-value}))

(defn view
  []
  (let [tab-ids      (rf/sub [:browser/tab-ids])
        browser-mode (rf/sub [:browser/mode])
        tabs-style   (use-hide (= browser-mode :browser-mode/tabs))]
    (when (= :browser-mode/tabs browser-mode)
      [reanimated/view
       {:style [tabs-style
                {:position :absolute
                 :top      safe-area/top
                 :left     0
                 :right    0
                 :bottom   (+ browser.constants/footer-height safe-area/bottom)}]}
       [rn/scroll-view
        {:shows-horizontal-scroll-indicator false
         :shows-vertical-scroll-indicator   false
         :content-container-style           {:padding-horizontal 20
                                             :padding-vertical   20
                                             :flex-direction     :row
                                             :flex-wrap          :wrap
                                             :gap                20}}
        (map-indexed (fn [idx tab-id]
                       ^{:key (str idx "-" tab-id)}
                       [tab-preview tab-id])
                     tab-ids)]])))
