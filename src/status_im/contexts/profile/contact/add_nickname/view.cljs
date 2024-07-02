(ns status-im.contexts.profile.contact.add-nickname.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.validation.profile :as profile-validator]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.add-nickname.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [public-key primary-name nickname customization-color]
         :as   profile}                         (rf/sub [:contacts/current-contact])
        customization-color                     customization-color
        full-name                               (profile.utils/displayed-name profile)
        profile-picture                         (profile.utils/photo profile)
        [unsaved-nickname set-unsaved-nickname] (rn/use-state nickname)
        [error-msg set-error-msg]               (rn/use-state nil)
        [typing? set-typing?]                   (rn/use-state false)
        has-nickname?                           (rn/use-memo (fn [] (not (string/blank? nickname)))
                                                             [nickname])
        validate-nickname                       (rn/use-callback
                                                 (debounce/debounce
                                                  (fn [name]
                                                    (set-error-msg
                                                     (profile-validator/validation-nickname name))
                                                    (set-typing? false))
                                                  300))
        on-cancel                               (rn/use-callback #(rf/dispatch [:hide-bottom-sheet]))
        on-nickname-change                      (rn/use-callback (fn [text]
                                                                   (set-typing? true)
                                                                   (set-unsaved-nickname text)
                                                                   (validate-nickname text)))
        on-nickname-submit                      (rn/use-callback
                                                 (fn []
                                                   (rf/dispatch [:hide-bottom-sheet])
                                                   (rf/dispatch [:toasts/upsert
                                                                 {:id   :add-nickname
                                                                  :type :positive
                                                                  :text (i18n/label
                                                                         (if (string/blank? nickname)
                                                                           :t/nickname-added
                                                                           :t/nickname-updated)
                                                                         {:primary-name primary-name})}])
                                                   (rf/dispatch [:contacts/update-nickname public-key
                                                                 (string/trim unsaved-nickname)]))
                                                 [public-key unsaved-nickname])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :title               (i18n/label :t/add-nickname-title)
       :full-name           full-name
       :profile-picture     profile-picture
       :customization-color customization-color}]
     [rn/view {:style style/input-wrapper}
      [quo/input
       {:type              :text
        :char-limit        constants/profile-name-max-length
        :max-length        constants/profile-name-max-length
        :auto-focus        true
        :default-value     unsaved-nickname
        :error?            (not (string/blank? error-msg))
        :label             (i18n/label :t/nickname)
        :on-change-text    on-nickname-change
        :on-submit-editing on-nickname-submit}]
      [quo/info-message
       {:icon   :i/info
        :size   :default
        :status (if-not (string/blank? error-msg) :error :default)}
       (if-not (string/blank? error-msg)
         error-msg
         (i18n/label :t/nickname-visible-to-you))]]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label (if has-nickname? :t/update-nickname-title :t/add-nickname-title))
       :button-one-props {:disabled?           (or typing?
                                                   (string/blank? unsaved-nickname)
                                                   (not (string/blank? error-msg)))
                          :customization-color customization-color
                          :on-press            on-nickname-submit}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press on-cancel}}]]))
