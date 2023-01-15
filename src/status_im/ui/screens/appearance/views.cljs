(ns status-im.ui.screens.appearance.views
  (:require-macros [status-im.utils.views :as views])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.utils.config :as config]))

(defn button
  [label icon theme selected?]
  [react/touchable-highlight
   {:on-press (fn []
                (re-frame/dispatch [:multiaccounts.ui/appearance-switched theme])
                (re-frame/dispatch (if config/new-ui-enabled? [:reload-new-ui] [:init-root :chat-stack]))
                (re-frame/dispatch [:navigate-change-tab :profile])
                (js/setTimeout #(re-frame/dispatch [:navigate-to :appearance]) 1000))}
   [react/view
    (merge {:align-items :center :padding 8 :border-radius 20}
           (when selected?
             {:background-color colors/blue-light}))
    [react/image {:source (get resources/ui icon)}]
    [react/text {:style {:margin-top 8}}
     (i18n/label label)]]])

(views/defview appearance
  []
  (views/letsubs [{:keys [appearance]} [:multiaccount]]
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
