(ns status-im2.contexts.chat.home.contact-request.view
  (:require [clojure.string :as string]
            [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.activity-center.notification-types :as notification-types]
            [status-im2.contexts.chat.home.contact-request.style :as style]
            [utils.re-frame :as rf]))

(defn get-display-name
  [{:keys [chat-id message]}]
  (let [name        (first (rf/sub [:contacts/contact-two-names-by-identity chat-id]))
        no-ens-name (string/blank? (get-in message [:content :ens-name]))]
    (if no-ens-name
      (first (string/split name " "))
      name)))

(defn requests-summary
  [requests]
  (case (count requests)
    1
    (get-display-name (first requests))
    2
    (str (get-display-name (first requests))
         " " (i18n/label :t/and)
         " " (get-display-name (second requests)))
    (str (get-display-name (first requests))
         ", " (get-display-name (second requests))
         " "
         (i18n/label :t/and)
         " "  (- (count requests) 2)
         " "  (i18n/label :t/more))))

(defn contact-requests
  [requests]
  [rn/touchable-opacity
   {:active-opacity      1
    :accessibility-label :open-activity-center-contact-requests
    :on-press            (fn []
                           (rf/dispatch [:activity-center/open
                                         {:filter-status :unread
                                          :filter-type   notification-types/contact-request}]))
    :style               style/contact-requests}
   [rn/view {:style (style/contact-requests-icon)}
    [quo/icon :i/pending-user {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]
   [rn/view {:style {:margin-left 8}}
    [rn/text {:weight :semi-bold} (i18n/label :t/pending-requests)]
    [rn/text
     {:size  :paragraph-2
      :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
     (requests-summary requests)]]
   [quo/info-count {:accessibility-label :pending-contact-requests-count}
    (count requests)]])
