(ns status-im2.contexts.add-new-contact.scan.scan-profile-qr-page
  (:require [status-im2.common.scan-qr-code.view :as scan-qr-code]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]))

(defn view
  []
  [scan-qr-code/view
   {:title           (i18n/label :t/scan-qr)
    :on-success-scan #(debounce/debounce-and-dispatch [:contacts/set-new-identity %] 300)}])
