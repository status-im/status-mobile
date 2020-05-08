(ns status-im.ui.screens.desktop.main.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.screens.desktop.main.styles :as styles]
            [status-im.ui.screens.bootnodes-settings.edit-bootnode.views :as edit-bootnode]
            [status-im.ui.screens.about-app.views :as about-app.views]
            [status-im.ui.screens.help-center.views :as help-center.views]
            [status-im.ui.screens.bootnodes-settings.views :as bootnodes]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.offline-messaging-settings.edit-mailserver.views :as edit-mailserver]
            [re-frame.core :as re-frame]))

(views/defview status-view []
  [react/view {:style {:flex 1 :background-color "#eef2f5" :align-items :center :justify-content :center}}
   [react/text {:style {:font-size 18 :color "#939ba1"}}
    "Status.im"]])

(views/defview tab-views []
  [react/view {:style {:flex 1}}
   [react/view]])

(views/defview popup-view []
  (views/letsubs [{:keys [popup]} [:desktop]]
    (when popup
      [react/view {:style styles/absolute}
       [react/touchable-highlight {:on-press #(re-frame/dispatch [:set-in [:desktop :popup] nil])
                                   :style    {:flex 1}}
        [react/view]]
       [react/view {:style styles/absolute}
        [popup]]])))

(views/defview main-view []
  (views/letsubs [view-id [:view-id]]
    (let [component (case view-id
                      :edit-mailserver edit-mailserver/edit-mailserver
                      :bootnodes-settings bootnodes/bootnodes-settings
                      :edit-bootnode edit-bootnode/edit-bootnode
                      :about-app about-app.views/about-app
                      :help-center help-center.views/help-center
                      status-view)]
      [react/view {:style {:flex 1}}
       [component]])))

(views/defview main-views []
  [react/view {:style styles/main-views}
   [react/view {:style styles/left-sidebar}
    [react/view {:style {:flex 1}}
     [tab-views]]]
   [react/view {:style styles/pane-separator}]
   [main-view]])
