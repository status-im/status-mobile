(ns legacy.status-im.ui.screens.appearance.views
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.react-native.resources :as resources]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn button
  [label icon theme selected?]
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:profile.settings/change-appearance theme])}
   [react/view
    (merge {:align-items :center :padding 8 :border-radius 20}
           (when selected?
             {:background-color colors/blue-light}))
    [react/image {:source (get resources/ui icon)}]
    [react/text {:style {:margin-top 8}}
     (i18n/label label)]]])

(views/defview appearance-view
  []
  (views/letsubs [{:keys [appearance]} [:profile/profile]]
    [:<>
     [quo/list-header (i18n/label :t/preference)]
     [react/view
      {:flex-direction     :row
       :padding-horizontal 8
       :justify-content    :space-between
       :margin-vertical    16}
      [button :t/light :theme-light 1 (= 1 appearance)]
      [button :t/dark :theme-dark 2 (= 2 appearance)]
      [button :t/system :theme-system 0 (= 0 appearance)]]]))
