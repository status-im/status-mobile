(ns status-im.contexts.profile.contact.block-contact.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.block-contact.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn on-close [] (rf/dispatch [:hide-bottom-sheet]))

(defn view
  []
  (let [{:keys [customization-color contact-request-state public-key]
         :as   contact}                       (rf/sub [:contacts/current-contact])
        ;; TODO(@mohsen): remove :blue, https://github.com/status-im/status-mobile/issues/18733
        customization-color                   (or customization-color :blue)
        full-name                             (profile.utils/displayed-name contact)
        profile-picture                       (profile.utils/photo contact)
        [remove-contact? set-remove-contact?] (rn/use-state false)
        on-block-press                        (rn/use-callback
                                               (fn []
                                                 (rf/dispatch [:toasts/upsert
                                                               {:id   :user-blocked
                                                                :type :positive
                                                                :text (i18n/label :t/user-blocked
                                                                                  {:username
                                                                                   full-name})}])
                                                 (rf/dispatch [:contact.ui/block-contact-confirmed
                                                               public-key])
                                                 (when remove-contact?
                                                   (rf/dispatch [:contact.ui/remove-contact-pressed
                                                                 contact]))
                                                 (on-close))
                                               [remove-contact? contact full-name])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :title               (i18n/label :t/block)
       :full-name           full-name
       :profile-picture     profile-picture
       :customization-color customization-color}]
     [rn/view {:style style/content-wrapper}
      [quo/text
       {:weight :medium
        :size   :paragraph-1}
       (i18n/label :t/block-user-title-message {:username full-name})]
      [quo/information-box
       {:icon :i/info
        :type :default}
       (i18n/label :t/blocking-a-user-message {:username full-name})]
      (when (= constants/contact-request-state-mutual contact-request-state)
        [rn/pressable
         {:style    style/checkbox-wrapper
          :on-press #(set-remove-contact? (not remove-contact?))}
         [quo/selectors
          {:type      :checkbox
           :checked?  remove-contact?
           :on-change #(set-remove-contact? %)}]
         [quo/text (i18n/label :t/remove-contact)]])]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/block)
       :button-one-props {:type     :danger
                          :on-press on-block-press}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press on-close}}]]))
