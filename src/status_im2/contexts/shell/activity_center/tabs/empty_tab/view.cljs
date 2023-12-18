(ns status-im2.contexts.shell.activity-center.tabs.empty-tab.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.core :as rn]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.shell.activity-center.notification-types :as types]
    [status-im2.contexts.shell.activity-center.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def ^:private empty-tab-description
  {types/no-type              (i18n/label :t/empty-notifications-all-tab)
   types/admin                (i18n/label :t/empty-notifications-admin-tab)
   types/mention              (i18n/label :t/empty-notifications-mentions-tab)
   types/reply                (i18n/label :t/empty-notifications-replies-tab)
   types/contact-request      (i18n/label :t/empty-notifications-contact-requests-tab)
   types/contact-verification (i18n/label :t/empty-notifications-identity-verification-tab)
   types/tx                   (i18n/label :t/empty-notifications-transactions-tab)
   types/membership           (i18n/label :t/empty-notifications-membership-tab)
   types/system               (i18n/label :t/empty-notifications-system-tab)})

(defn empty-tab-internal
  [{:keys [theme]}]
  (let [filter-type (rf/sub [:activity-center/filter-type])
        description (get empty-tab-description filter-type nil)]
    [rn/view {:style style/empty-container}
     [quo/empty-state
      {:blur?       true
       :image       (resources/get-themed-image :no-notifications theme)
       :title       (i18n/label :t/empty-notifications-title-unread)
       :description description}]]))

(def empty-tab (quo.theme/with-theme empty-tab-internal))
