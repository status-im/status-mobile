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
            [reagent.core :as reagent]
            [status-im.ui.components.bottom-panel.views :as bottom-panel]))

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
         {:on-press #(re-frame/dispatch [:set :current-extension-sheet (fn [] [(reagent/adapt-react-class (get-in ext [:hook :view]))])])}
         [react/view {:width              128 :height 128 :justify-content :space-between
                      :padding-horizontal 10 :padding-vertical 12
                      :border-radius      16 :margin-left 8 :background-color (colors/alpha (:color ext) 0.2)}
          [react/image {:source (:icon ext)
                        :resize-mode :center
                        :style       {:border-radius 20
                                      :width  40
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
                             :style       {:border-radius 20
                                           :width  40
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

(defn extension-screen []
  (let [{:keys [ext view]} @(re-frame/subscribe [:get-screen-params])]
    [react/view (merge {:flex 1}
                       (when (= (:theme ext) :dark)
                         {:background-color colors/black}))
     [topbar/topbar
      (merge {:modal?        true
              :border-bottom false
              :title         (:name ext)})]
     [react/scroll-view
      (when view
        [view])]]))

(defn extension-sheet []
  (let [sheet @(re-frame/subscribe [:current-extension-sheet])
        {window-height :height} @(re-frame/subscribe [:dimensions/window])]
    [bottom-panel/bottom-panel
     sheet
     (fn [sheet]
       [react/view {:style
                    {:background-color        colors/white
                     :border-top-right-radius 16
                     :border-top-left-radius  16
                     :padding-bottom          40
                     :height (/ window-height 2)
                     :flex 1}}
         [sheet]])
     window-height]))
