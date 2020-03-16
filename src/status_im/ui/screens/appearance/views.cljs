(ns status-im.ui.screens.appearance.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n :as i18n]))

(defn button [label icon theme selected?]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:multiaccounts.ui/appearance-switched theme])}
   [react/view (merge {:align-items :center :padding 8 :border-radius 20}
                      (when selected?
                        {:background-color colors/blue-light}))
    [react/image {:source (get resources/ui icon)}]
    [react/text {:style {:margin-top 8}}
     (i18n/label label)]]])

(views/defview appearance []
  (views/letsubs [{:keys [appearance]} [:multiaccount]]
    [react/view {:flex 1}
     [topbar/topbar {:title :t/appearance :show-border? true}]
     [list-item/list-item {:type :section-header :title :t/preference :container-margin-top 8}]
     [react/view {:flex-direction  :row :flex 1 :padding-horizontal 8
                  :justify-content :space-between :margin-top 16}
      [button :t/light :theme-light 1 (= 1 appearance)]
      [button :t/dark :theme-dark 2 (= 2 appearance)]
      [button :t/system :theme-system 0 (= 0 appearance)]]]))
