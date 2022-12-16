(ns status-im.ui.screens.activity-center.notification.common.view
  (:require [quo2.core :as quo2]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.activity-center.notification.common.style :as style]
            [status-im.ui.screens.activity-center.utils :as activity-center.utils]
            [utils.re-frame :as rf]))

(defn user-avatar-tag
  [user]
  (let [contact (rf/sub [:contacts/contact-by-identity user])]
    [quo2/user-avatar-tag
     {:color          :purple
      :override-theme :dark
      :size           :small
      :style          style/user-avatar-tag
      :text-style     style/user-avatar-tag-text}
     (activity-center.utils/contact-name contact)
     (multiaccounts/displayed-photo contact)]))