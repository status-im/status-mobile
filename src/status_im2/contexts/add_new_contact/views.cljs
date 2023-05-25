(ns status-im2.contexts.add-new-contact.views
  (:require
    [clojure.string :as string]
    [quo2.core :as quo]
    [react-native.core :as rn]
    [react-native.clipboard :as clipboard]
    [reagent.core :as reagent]
    [status-im.multiaccounts.core :as multiaccounts]
    [status-im.qr-scanner.core :as qr-scanner]
    [status-im.utils.utils :as utils]
    [status-im2.contexts.add-new-contact.style :as style]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn found-contact
  [public-key]
  (let [{:keys [primary-name compressed-key]
         :as   contact} (rf/sub [:contacts/contact-by-identity public-key])]
    (when primary-name
      [rn/view style/found-user
       [quo/text (style/text-description)
        (i18n/label :t/user-found)]
       [rn/view (style/found-user-container)
        [quo/user-avatar
         {:full-name         primary-name
          :profile-picture   (multiaccounts/displayed-photo contact)
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
          (utils/get-shortened-address compressed-key)]]]])))

(defn new-contact
  []
  (let [clipboard     (reagent/atom nil)
        default-value (reagent/atom nil)]
    (fn []
      (clipboard/get-string #(reset! clipboard %))
      (let [{:keys [input scanned public-key ens state msg]}
            (rf/sub [:contacts/new-identity])
            invalid? (= state :invalid)
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
             (merge (style/button-close)
                    {:on-press
                     (fn []
                       (reset! clipboard nil)
                       (reset! default-value nil)
                       (rf/dispatch [:contacts/clear-new-identity])
                       (rf/dispatch [:navigate-back]))}) :i/close]
            [quo/text (style/text-title)
             (i18n/label :t/add-a-contact)]
            [quo/text (style/text-subtitle)
             (i18n/label :t/find-your-friends)]
            [quo/text (style/text-description)
             (i18n/label :t/ens-or-chat-key)]
            [rn/view style/container-text-input
             [rn/view (style/text-input-container invalid?)
              [rn/text-input
               (merge (style/text-input)
                      {:default-value   (or scanned @default-value input)
                       :placeholder     (i18n/label :t/type-some-chat-key)
                       :on-change-text  (fn [v]
                                          (reset! default-value v)
                                          (debounce/debounce-and-dispatch
                                           [:contacts/set-new-identity v nil]
                                           600))
                       :blur-on-submit  true
                       :return-key-type :done})]
              (when show-paste-button?
                [quo/button
                 (merge style/button-paste
                        {:on-press
                         (fn []
                           (reset! default-value @clipboard)
                           (rf/dispatch
                            [:contacts/set-new-identity @clipboard nil]))})
                 (i18n/label :t/paste)])]
             [quo/button
              (merge style/button-qr
                     {:on-press #(rf/dispatch
                                  [::qr-scanner/scan-code
                                   {:handler :contacts/qr-code-scanned}])})
              :i/scan]]
            (when invalid?
              [rn/view style/container-invalid
               [quo/icon :i/alert style/icon-invalid]
               [quo/text style/text-invalid
                (i18n/label (or msg :t/invalid-ens-or-key))]])
            (when (= state :valid)
              [found-contact public-key])]
           [quo/button
            (merge (style/button-view-profile state)
                   {:on-press
                    (fn []
                      (reset! clipboard nil)
                      (reset! default-value nil)
                      (rf/dispatch [:contacts/clear-new-identity])
                      (rf/dispatch [:navigate-back])
                      (rf/dispatch [:chat.ui/show-profile public-key ens]))})
            (i18n/label :t/view-profile)]]]]))))
