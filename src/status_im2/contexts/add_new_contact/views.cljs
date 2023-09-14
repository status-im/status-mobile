(ns status-im2.contexts.add-new-contact.views
  (:require
    [clojure.string :as string]
    [quo2.core :as quo]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.qr-scanner.core :as qr-scanner]
    [status-im2.contexts.add-new-contact.style :as style]
    [utils.debounce :as debounce]
    [utils.address :as address]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn found-contact
  [public-key]
  (let [{:keys [primary-name compressed-key]} (rf/sub [:contacts/contact-by-identity public-key])
        photo-path                            (rf/sub [:chats/photo-path public-key])]
    (when primary-name
      [rn/view style/found-user
       [quo/text (style/text-description)
        (i18n/label :t/user-found)]
       [rn/view (style/found-user-container)
        [quo/user-avatar
         {:full-name         primary-name
          :profile-picture   photo-path
          :size              :small
          :status-indicator? false}]
        [rn/view style/found-user-text
         [quo/text
          {:weight :semi-bold
           :size   :paragraph-1
           :style  (style/found-user-display-name)}
          primary-name]
         [quo/text
          {:weight :regular
           :size   :paragraph-2
           :style  (style/found-user-key)}
          (address/get-shortened-compressed-key compressed-key)]]]])))

(defn new-contact
  []
  (let [clipboard     (reagent/atom nil)
        default-value (reagent/atom nil)
        invalid-state (reagent/atom nil)]
    (fn []
      (clipboard/get-string #(reset! clipboard %))
      (let [{:keys [input scanned public-key ens state msg]}
            (rf/sub [:contacts/new-identity])
            customization-color (rf/sub [:profile/customization-color])
            invalid? (reset! invalid-state (= state :invalid))
            show-paste-button? (and (not (string/blank? @clipboard))
                                    (string/blank? @default-value)
                                    (string/blank? input))]
        [rn/keyboard-avoiding-view
         {:style {:flex 1}}
         [rn/touchable-without-feedback
          {:on-press rn/dismiss-keyboard!}
          [rn/view (style/container-outer)
           [rn/view style/container-inner
            [quo/button
             {:type :grey
              :icon-only? true
              :accessibility-label :new-contact-close-button
              :size 32
              :on-press
              (fn []
                (reset! clipboard nil)
                (reset! default-value nil)
                (rf/dispatch [:contacts/clear-new-identity])
                (rf/dispatch [:navigate-back]))} :i/close]
            [quo/text (style/text-title)
             (i18n/label :t/add-a-contact)]
            [quo/text (style/text-subtitle)
             (i18n/label :t/find-your-friends)]

            [quo/private-key-input
             {:input-placeholder (i18n/label :t/type-some-chat-key)
              :disabled?         false
              :title-text        (i18n/label :t/ens-or-chat-key)
              :scan-variant?     true
              :on-scan-press     #(rf/dispatch [::qr-scanner/scan-code
                                                {:handler :contacts/qr-code-scanned}])
              :invalid?          invalid-state
              :on-change-text    (fn [v]
                                   (debounce/debounce-and-dispatch [:contacts/set-new-identity v nil]
                                                                   600))
              :after-paste-press #(rf/dispatch [:contacts/set-new-identity @clipboard nil])
              :blur-on-submit    true
              :return-key-type   :done}]
            (when @invalid-state
              [rn/view style/container-invalid
               [quo/icon :i/alert style/icon-invalid]
               [quo/text style/text-invalid
                (i18n/label (or msg :t/invalid-ens-or-key))]])
            (when (= state :valid)
              [found-contact public-key])]
           [quo/button
            {:type :primary
             :customization-color customization-color
             :size 40
             :container-style style/button-view-profile
             :accessibility-label :new-contact-button
             :icon-left :i/profile
             :disabled? (not= state :valid)
             :on-press
             (fn []
               (reset! clipboard nil)
               (reset! default-value nil)
               (rf/dispatch [:contacts/clear-new-identity])
               (rf/dispatch [:navigate-back])
               (rf/dispatch [:chat.ui/show-profile public-key ens]))}
            (i18n/label :t/view-profile)]]]]))))
