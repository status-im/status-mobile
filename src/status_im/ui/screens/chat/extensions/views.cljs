(ns status-im.ui.screens.chat.extensions.views
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.topbar :as topbar]
            [status-im.extensions.list :as extensions]
            [status-im.ui.components.list.views :as list]
            [quo.core :as quo]
            [reagent.core :as reagent]))

(defn extensions-view []
  (let [extensions @(re-frame/subscribe [:extensions-hooks-command])
        one-to-one-chat? @(re-frame/subscribe [:current-chat/one-to-one-chat?])]
    [react/scroll-view {:horizontal true :style {:flex 1} :showsHorizontalScrollIndicator false}
     [react/view {:style {:flex-direction :row}}
      (when one-to-one-chat?
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch [:wallet/prepare-transaction-from-chat])}
         [react/view {:width              128 :height 128 :justify-content :space-between
                      :padding-horizontal 10 :padding-vertical 12
                      :background-color   (colors/alpha colors/purple 0.2) :border-radius 16 :margin-left 8}
          [react/view {:background-color colors/purple :width 40 :height 40 :border-radius 20 :align-items :center
                       :justify-content  :center}
           [icons/icon :main-icons/send {:color colors/white}]]
          [react/text {:typography :medium} (i18n/label :t/send-transaction)]]])
      (when one-to-one-chat?
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch [:wallet/prepare-request-transaction-from-chat])}
         [react/view {:width              128 :height 128 :justify-content :space-between
                      :padding-horizontal 10 :padding-vertical 12
                      :background-color   (colors/alpha colors/orange 0.2) :border-radius 16 :margin-left 8}
          [react/view {:background-color colors/orange :width 40 :height 40 :border-radius 20 :align-items :center
                       :justify-content  :center}
           [icons/icon :main-icons/receive {:color colors/white}]]
          [react/text {:typography :medium} (i18n/label :t/request-transaction)]]])
      (for [ext extensions]
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch [:bottom-sheet/show-sheet
                                         {:content (fn [] [(reagent/adapt-react-class (get-in ext [:hook :view]))])}])}
         [react/view {:width              128 :height 128 :justify-content :space-between
                      :padding-horizontal 10 :padding-vertical 12
                      :border-radius      16 :margin-left 8 :background-color (colors/alpha (:color ext) 0.2)}
          [react/image {:source (:icon ext)
                        :resize-mode :center
                        :style       {:width  40
                                      :height 40}}]
          [react/text {:typography :medium} (:name ext)]]])
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:navigate-to :extensions])}
       [react/view {:width              128 :height 128 :justify-content :space-between
                    :padding-horizontal 10 :padding-vertical 12
                    :align-items        :center
                    :border-radius      16 :margin-left 8 :border-color colors/gray-lighter :border-width 1 :margin-right 8}
        [react/image {:source (resources/get-image :extensions)}]
        [react/text {:typography :medium :style {:color colors/blue}} "Add extensions"]]]]]))

(defn render-item [{:keys [name author version id icon] :as ext}]
  [quo/list-item
   {:icon      [react/image {:source icon
                             :resize-mode :center
                             :style       {:width  40
                                           :height 40}}]
    :title     (str name " " version)
    :subtitle  (str "Author: " author)
    :active    (boolean @(re-frame/subscribe [:extension-by-id id]))
    :accessory :switch
    :on-press  #(re-frame/dispatch [:switch-extension ext])}])

(defn extensions-screen []
  [react/view {:flex 1}
   [topbar/topbar
    {:modal?        true
     :border-bottom true
     :title         "Extensions"}]
   [list/flat-list
    {:key-fn    :id
     :data      extensions/extensions
     :render-fn (fn [ext] [render-item ext])}]])
