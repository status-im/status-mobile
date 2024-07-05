(ns status-im.contexts.profile.settings.screens.messages.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.navigation :as navigation]
    [utils.re-frame :as rf]))

(defn- open-blocked-users
  []
  (rf/dispatch [:open-modal :screen/settings-blocked-users]))

(defn view
  []
  (let [allow-new-contact-requests?       (rf/sub [:profile/allow-new-contact-requests?])
        toggle-allow-new-contact-requests (rn/use-callback
                                           (fn []
                                             (rf/dispatch [:profile/update-messages-from-contacts-only]))
                                           [allow-new-contact-requests?])]
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   navigation/navigate-back}]
     [quo/page-top {:title (i18n/label :t/messages)}]
     [quo/category
      {:label     (i18n/label :t/contacts)
       :data      [{:title        (i18n/label :t/allow-new-contact-requests)
                    :blur?        true
                    :action       :selector
                    :action-props {:on-change toggle-allow-new-contact-requests
                                   :checked?  allow-new-contact-requests?}}
                   {:title    (i18n/label :t/blocked-users)
                    :on-press open-blocked-users
                    :blur?    true
                    :action   :arrow}]
       :blur?     true
       :list-type :settings}]]))
