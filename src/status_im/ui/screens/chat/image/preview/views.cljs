(ns status-im.ui.screens.chat.image.preview.views
  (:require ["react-native-image-viewing" :default image-viewing]
            [quo.design-system.colors :as colors]
            [quo.platform :as platform]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.chat.models.images :as images]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.utils.share :as share]
            [taoensso.timbre :as log]
            [react-native.safe-area :as safe-area]))

(defn share
  [path]
  (share/open {:url  (str (when platform/android? "file://") path)
               :type "image/jpeg"}
              #(log/debug "image shared successfully")
              #(log/error "could not share image")))

(defn header-options
  []
  (fn [{:keys [message on-close]}]
    ;; FIXME(Ferossgp): Bottom sheet doesn't work on Android because of
    ;; https://github.com/software-mansion/react-native-gesture-handler/issues/139
    [react/view
     {:style {:flex-direction     :row
              :background-color   colors/black-transparent-86
              :border-radius      44
              :padding-vertical   8
              :padding-horizontal 12
              :position           :absolute
              :right              0}}
     [react/touchable-opacity
      {:on-press            (fn []
                              (on-close)
                              (re-frame/dispatch [:chat.ui/save-image-to-gallery
                                                  (get-in message [:content :image])]))
       :style               {:margin-right 10}
       :accessibility-label :save-button}
      [icons/icon :main-icons/download
       {:container-style {:width  24
                          :height 24}
        :color           colors/white-persist}]]
     [react/touchable-opacity
      {:on-press            #(images/download-image-http (get-in message [:content :image]) share)
       :style               {:margin-left 10}
       :accessibility-label :share-button}
      [icons/icon :main-icons/share-default
       {:container-style {:width  24
                          :height 24}
        :color           colors/white-persist}]]]))

(defn header
  [{:keys [on-close] :as props}]
  [react/view
   {:style {:padding-horizontal 15
            :padding-top        (+ (safe-area/get-bottom) 50)}}
   [react/view {:style {:justify-content :center}}
    [react/touchable-opacity
     {:on-press            on-close
      :style               {:padding-vertical 11
                            :border-radius    44}
      :accessibility-label :close-button}
     [react/view
      {:style {:background-color colors/black-transparent-86
               :border-radius    20
               :width            40
               :height           40
               :justify-content  :center
               :align-items      :center}}
      [icons/icon :main-icons/close
       {:container-style {:width  24
                          :height 24}
        :color           colors/white-persist}]]]
    [header-options props]]])

(defn preview-image
  [{{:keys [content] :as message} :message
    visible                       :visible
    on-close                      :on-close}]
  [:> image-viewing
   {:images                 #js [#js {:uri (:image content)}]
    :on-request-close       on-close
    :hide-header-on-zoom    false
    :hide-footer-on-zoom    false
    :swipe-to-close-enabled platform/ios?
    :presentation-style     "overFullScreen"
    :HeaderComponent        #(reagent/as-element [header
                                                  {:on-close on-close
                                                   :message  message}])
    :FooterComponent        #(reagent/as-element [:<>])
    :visible                visible}])
