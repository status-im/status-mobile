(ns status-im2.contexts.activity-center.notification.common.view
  (:require [quo2.core :as quo]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im2.contexts.activity-center.notification.common.style :as style]
            [status-im2.contexts.activity-center.utils :as activity-center.utils]
            [utils.re-frame :as rf]))

(defn user-avatar-tag
  [user-id]
  (let [contact (rf/sub [:contacts/contact-by-identity user-id])]
    [quo/user-avatar-tag
     {:color          :purple
      :override-theme :dark
      :size           :small
      :style          style/user-avatar-tag
      :text-style     style/user-avatar-tag-text}
     (activity-center.utils/contact-name contact)
     (multiaccounts/displayed-photo contact)]))
