(ns status-im2.contexts.add-new-contact.views
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im.react-native.resources :as resources]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im2.contexts.add-new-contact.style :as style]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn new-contact
  []
  (let [{:keys [input public-key state error]} (rf/sub
                                                [:contacts/new-identity])
        error?                                 (and (= state :error)
                                                    (= error :uncompressed-key))]
    [rn/keyboard-avoiding-view (style/container-kbd)
     [rn/view style/container-image
      [rn/image
       {:source (resources/get-image :add-new-contact)
        :style  style/image}]
      [quo/button
       (merge (style/button-close)
              {:on-press
               (fn []
                 (rf/dispatch [:contacts/clear-new-identity])
                 (rf/dispatch [:navigate-back]))}) :i/close]]
     [rn/view (style/container-outer)
      [rn/view style/container-inner
       [quo/text (style/text-title)
        (i18n/label :t/add-a-contact)]
       [quo/text (style/text-subtitle)
        (i18n/label :t/find-your-friends)]
       [quo/text (style/text-description)
        (i18n/label :t/ens-or-chat-key)]
       [rn/view style/container-text-input
        [rn/text-input
         (merge (style/text-input error?)
                {:default-value  input
                 :placeholder    (i18n/label :t/type-some-chat-key)
                 :on-change-text #(debounce/debounce-and-dispatch
                                   [:contacts/set-new-identity %]
                                   600)})]
        [quo/button
         (merge style/button-qr
                {:on-press #(rf/dispatch [::qr-scanner/scan-code
                                          {:handler :contacts/qr-code-scanned}])})
         :i/scan]]
       (when error?
         [rn/view style/container-error
          [quo/icon :i/alert style/icon-error]
          [quo/text style/text-error (i18n/label :t/not-a-chatkey)]])]
      [rn/view
       [quo/button
        (merge (style/button-view-profile state)
               {:on-press
                (fn []
                  (rf/dispatch [:contacts/clear-new-identity])
                  (rf/dispatch [:navigate-back])
                  (rf/dispatch [:chat.ui/show-profile public-key]))})
        (i18n/label :t/view-profile)]]]]))
