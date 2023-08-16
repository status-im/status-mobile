(ns status-im2.contexts.shell.activity-center.tabs.view
  (:require [utils.re-frame :as rf]
            [quo2.core :as quo]
            [status-im2.contexts.shell.activity-center.style :as style]
            [status-im2.contexts.shell.activity-center.notification-types :as types]
            [clojure.set :as set]
            [utils.i18n :as i18n]))

(defn tabs
  []
  (let [customization-color           (rf/sub [:profile/customization-color])
        filter-type                   (rf/sub [:activity-center/filter-type])
        types-with-unread             (rf/sub [:activity-center/notification-types-with-unread])
        is-mark-all-as-read-undoable? (boolean (rf/sub
                                                [:activity-center/mark-all-as-read-undoable-till]))]
    [quo/tabs
     {:size                32
      :scrollable?         true
      :customization-color customization-color
      :blur?               true
      :style               style/tabs
      :fade-end-percentage 0.79
      :scroll-on-press?    true
      :fade-end?           true
      :on-change           #(rf/dispatch [:activity-center.notifications/fetch-first-page
                                          {:filter-type %}])
      :default-active      filter-type
      :data                [{:id    types/no-type
                             :label (i18n/label :t/all)}
                            {:id                  types/admin
                             :label               (i18n/label :t/admin)
                             :accessibility-label :tab-admin
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/admin))}
                            {:id                  types/mention
                             :label               (i18n/label :t/mentions)
                             :accessibility-label :tab-mention
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/mention))}
                            {:id                  types/reply
                             :label               (i18n/label :t/replies)
                             :accessibility-label :tab-reply
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/reply))}
                            {:id                  types/contact-request
                             :label               (i18n/label :t/contact-requests)
                             :accessibility-label :tab-contact-request
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/contact-request))}
                            {:id                  types/contact-verification
                             :label               (i18n/label :t/identity-verification)
                             :accessibility-label :tab-contact-verification
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread
                                                               types/contact-verification))}
                            {:id                  types/tx
                             :label               (i18n/label :t/transactions)
                             :accessibility-label :tab-tx
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/tx))}
                            {:id                  types/membership
                             :label               (i18n/label :t/membership)
                             :accessibility-label :tab-membership
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (set/subset? types/membership types-with-unread))}
                            {:id                  types/system
                             :label               (i18n/label :t/system)
                             :accessibility-label :tab-system
                             :notification-dot?   (when-not is-mark-all-as-read-undoable?
                                                    (contains? types-with-unread types/system))}]}]))
