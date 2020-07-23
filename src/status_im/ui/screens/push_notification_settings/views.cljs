(ns status-im.ui.screens.notification-settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar])
  (:require-macros [status-im.utils.views :as views]))

(defn notification-toggle [notifications-enabled?]

  [react/view {:flex 1}
   (if notifications-enabled?
     [react/text {} "Enabled"]
     [react/text {} "Disabled"])])

(views/defview notification-settings []
  (views/letsubs [notifications-enabled?  [:multiaccount/notifications-enabled?]]
    [react/view {:flex 1}
     [topbar/topbar {:title :t/notification-settings}]
     [notification-toggle notifications-enabled?]]))
