(ns status-im2.contexts.chat.home.contact-request.view
  (:require [clojure.string :as string]
            [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent] ;; TODO move to status-im2
            [status-im.ui2.screens.chat.components.received-cr-item :as received-cr-item]
            [status-im2.contexts.chat.home.contact-request.style :as style]
            [utils.re-frame :as rf]))

(defn contact-requests-sheet
  [received-requests]
  (let [selected-requests-tab (reagent/atom :received)]
    (fn []
      (let [sent-requests []]
        [rn/view {:style {:margin-left 20}}
         [rn/touchable-opacity
          {:on-press #(rf/dispatch [:bottom-sheet/hide])
           :style    (style/contact-requests-sheet)}
          [quo/icon :i/close]]
         [rn/text {:size :heading-1 :weight :semi-bold}
          (i18n/label :t/pending-requests)]
         [quo/tabs
          {:style          {:margin-top 12 :margin-bottom 20}
           :size           32
           :on-change      #(reset! selected-requests-tab %)
           :default-active :received
           :data           [{:id    :received
                             :label (i18n/label :t/received)}
                            {:id    :sent
                             :label (i18n/label :t/sent)}]}]
         [rn/flat-list
          {:key-fn    :chat-id
           :data      (if (= @selected-requests-tab :received) received-requests sent-requests)
           :render-fn received-cr-item/received-cr-item}]]))))

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
   {:active-opacity 1
    :on-press       (fn []
                      (rf/dispatch [:bottom-sheet/show-sheet
                                    {:content (fn [] [contact-requests-sheet requests])}]))
    :style          style/contact-requests}
   [rn/view {:style (style/contact-requests-icon)}
    [quo/icon :i/pending-user {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]
   [rn/view {:style {:margin-left 8}}
    [rn/text {:weight :semi-bold} (i18n/label :t/pending-requests)]
    [rn/text
     {:size  :paragraph-2
      :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
     (requests-summary requests)]]
   [quo/info-count (count requests)]])
