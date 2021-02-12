(ns status-im.ui.screens.chat.image.preview.views
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [quo.core :as quo]
            [quo.platform :as platform]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.chat.sheets :as sheets]
            [quo.components.safe-area :as safe-area]
            ["react-native-image-viewing" :default image-viewing]))

(defn footer-options []
  (let [show-sheet (reagent/atom false)]
    (fn [{:keys [message on-close]}]
      ;; FIXME(Ferossgp): Bottom sheet doesn't work on Android because of https://github.com/software-mansion/react-native-gesture-handler/issues/139
      (if platform/android?
        [:<>
         [react/touchable-opacity
          {:on-press (fn []
                       (on-close)
                       (re-frame/dispatch [:chat.ui/save-image-to-gallery (get-in message [:content :image])]))
           :style    {:background-color colors/black-transparent-86
                      :border-radius    44
                      :padding          8
                      :position         :absolute
                      :bottom           0
                      :right            0}}
          [icons/icon :main-icons/download {:container-style {:width  24
                                                              :height 24}
                                            :color           colors/white-persist}]]]
        [:<>
         [react/touchable-opacity
          {:on-press #(reset! show-sheet true)
           :style    {:background-color colors/black-transparent-86
                      :border-radius    44
                      :padding          8
                      :position         :absolute
                      :bottom           0
                      :right            0}}
          [icons/icon :main-icons/more {:container-style {:width  24
                                                          :height 24}
                                        :color           colors/white-persist}]]
         ;; NOTE(Ferossgp): If we use global bottom sheet, then it is rendered under the preview
         [quo/bottom-sheet {:visible?  @show-sheet
                            :on-cancel #(reset! show-sheet false)}
          [sheets/image-long-press message #(do (reset! show-sheet false)
                                                (on-close))]]]))))

(defn footer [{:keys [on-close] :as props}]
  [safe-area/consumer
   (fn [insets]
     [react/view {:style {:padding-horizontal 24
                          :padding-bottom     (+ (:bottom insets) 8)}}
      [react/view {:style {:justify-content :center
                           :align-items     :center}}
       [react/touchable-opacity {:on-press on-close
                                 :style    {:background-color   colors/black-transparent-86
                                            :padding-horizontal 24
                                            :padding-vertical   11
                                            :border-radius      44}}
        [quo/text {:style {:color colors/white-persist}}
         (i18n/label :t/close)]]
       [footer-options props]]])])

(defn preview-image [{{:keys [content] :as message} :message
                      visible                       :visible
                      on-close                      :on-close}]
  [:> image-viewing {:images                 #js [#js {:uri (:image content)}]
                     :on-request-close       on-close
                     :hide-header-on-zoom    false
                     :hide-footer-on-zoom    false
                     :swipe-to-close-enabled platform/ios?
                     :presentation-style     "overFullScreen"
                     :HeaderComponent        #(reagent/as-element [:<>]) ; NOTE: Keep it to remove default header
                     :FooterComponent        #(reagent/as-element [footer {:on-close on-close
                                                                           :message  message}])
                     :visible                visible}])
