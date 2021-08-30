(ns status-im.ui.screens.chat.image.preview.views
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [quo.platform :as platform]
            [status-im.ui.components.icons.icons :as icons]
            [quo.components.safe-area :as safe-area]
            ["react-native-image-viewing" :default image-viewing]
            [status-im.utils.share :as share]
            [taoensso.timbre :as log]
            [status-im.utils.fs :as fs]
            [clojure.string :as string]))

(def temp-image-url (str (fs/cache-dir) "/image.jpeg"))

(defn share []
  (share/open {:url (str (when platform/android? "file://") temp-image-url)
               :type "image/jpeg"}
              #(log/debug "image shared successfully")
              #(log/error "could not share image")))

(defn header-options []
  (fn [{:keys [message on-close]}]
      ;; FIXME(Ferossgp): Bottom sheet doesn't work on Android because of https://github.com/software-mansion/react-native-gesture-handler/issues/139
    [react/view {:style {:flex-direction     :row
                         :background-color   colors/black-transparent-86
                         :border-radius      44
                         :padding-vertical   8
                         :padding-horizontal 12
                         :position           :absolute
                         :right              0}}
     [react/touchable-opacity
      {:on-press (fn []
                   (on-close)
                   (re-frame/dispatch [:chat.ui/save-image-to-gallery (get-in message [:content :image])]))
       :style    {:margin-right 10}
       :accessibility-label :save-button}
      [icons/icon :main-icons/download {:container-style {:width  24
                                                          :height 24}
                                        :color           colors/white-persist}]]
     [react/touchable-opacity
      {:on-press (fn []
                   (fs/write-file
                    temp-image-url
                    (last (string/split (get-in message [:content :image]) ",")) "base64"
                    #(share)
                    #(log/error "error writing image to cache dir")))
       :style    {:margin-left 10}
       :accessibility-label :share-button}
      [icons/icon :main-icons/share-default {:container-style {:width  24
                                                               :height 24}
                                             :color           colors/white-persist}]]]))

(defn header [{:keys [on-close] :as props}]
  [safe-area/consumer
   (fn [insets]
     [react/view {:style {:padding-horizontal 15
                          :padding-top     (+ (:bottom insets) 50)}}
      [react/view {:style {:justify-content :center}}
       [react/touchable-opacity {:on-press on-close
                                 :style    {:padding-vertical   11
                                            :border-radius      44}
                                 :accessibility-label :close-button}
        [react/view {:style {:background-color colors/black-transparent-86
                             :border-radius    20
                             :width            40
                             :height           40
                             :justify-content  :center
                             :align-items      :center}}
         [icons/icon :main-icons/close {:container-style {:width            24
                                                          :height           24}
                                        :color           colors/white-persist}]]]
       [header-options props]]])])

(defn preview-image [{{:keys [content] :as message} :message
                      visible                       :visible
                      on-close                      :on-close}]
  [:> image-viewing {:images                 #js [#js {:uri (:image content)}]
                     :on-request-close       on-close
                     :hide-header-on-zoom    false
                     :hide-footer-on-zoom    false
                     :swipe-to-close-enabled platform/ios?
                     :presentation-style     "overFullScreen"
                     :HeaderComponent        #(reagent/as-element [header {:on-close on-close
                                                                           :message  message}])
                     :FooterComponent        #(reagent/as-element [:<>])
                     :visible                visible}])
