(ns status-im.contexts.chat.home.add-new-contact.scan.scan-profile-qr-page
  (:require [react-native.core :as rn]
            [react-native.hooks :as hooks]
            [status-im.common.scan-qr-code.view :as scan-qr-code]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]))

(defn- f-internal-view
  []
  (let [{:keys [keyboard-shown]} (hooks/use-keyboard)]
    [:<>
     (when keyboard-shown
       (rn/dismiss-keyboard!))
     [scan-qr-code/view
      {:title           (i18n/label :t/scan-qr)
       :on-success-scan #(debounce/debounce-and-dispatch [:contacts/set-new-identity % %] 300)}]]))

(defn view
  []
  [:f> f-internal-view])
