(ns status-im.contexts.keycard.sheet.view
  (:require [quo.foundations.colors :as colors]
            quo.theme
            [react-native.core :as rn]
            [status-im.common.resources :as resources]
            [utils.re-frame :as rf]))

(defn connect-keycard
  []
  (let [connected?         (rf/sub [:keycard/connected?])
        {:keys [on-close]} (rf/sub [:keycard/connection-sheet-opts])
        theme              (quo.theme/use-theme)]
    [rn/view {:flex 1}
     [rn/view {:flex 1}]
     [rn/view
      {:style {:align-items        :center
               :padding-horizontal 36
               :padding-vertical   30
               :background-color   (colors/theme-colors colors/white colors/neutral-95 theme)}}
      [rn/text {:style {:font-size 26 :color "#9F9FA5" :margin-bottom 36}}
       "Ready to Scan"]
      [rn/image
       {:source (resources/get-image :nfc-prompt)}]
      [rn/text {:style {:font-size 16 :color :white :margin-vertical 36}}
       (if connected?
         "Connected. Don’t move your card."
         "Hold your phone near a Status Keycard")]
      [rn/pressable
       {:on-press (fn []
                    (when on-close (on-close))
                    (rf/dispatch [:keycard/hide-connection-sheet]))
        :style    {:flex-direction :row}}
       [rn/view
        {:style {:background-color "#8E8E93" :flex 1 :align-items :center :padding 18 :border-radius 10}}
        [rn/text {:style {:color :white :font-size 16}}
         "Cancel"]]]]]))
