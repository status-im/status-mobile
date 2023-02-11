(ns status-im.ui.screens.chat.utils
  (:require [quo.design-system.colors :as colors]
            [utils.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.react :as react]))

(defn format-author-old
  ([contact] (format-author-old contact nil))
  ([{:keys [names] :as contact} {:keys [modal profile? you?]}]
   (let [{:keys [nickname ens-name]} names
         [first-name second-name]    (multiaccounts/contact-two-names contact false)]
     (if (or nickname ens-name)
       [react/nested-text
        {:number-of-lines 2
         :style           {:color       (if modal colors/white-persist colors/blue)
                           :font-size   (if profile? 15 13)
                           :line-height (if profile? 22 18)
                           :font-weight "500"}}
        (subs first-name 0 81)
        (when you?
          [{:style {:color colors/gray :font-weight "400" :font-size 13}}
           (str " " (i18n/label :t/You))])
        (when nickname
          [{:style {:color colors/gray :font-weight "400"}}
           (str " " (subs second-name 0 81))])]
       [react/text
        {:style {:color       (if modal colors/white-persist colors/gray)
                 :font-size   (if profile? 15 12)
                 :line-height (if profile? 22 18)
                 :font-weight "400"}}
        first-name]))))

(defn format-author
  ([contact] (format-author contact nil nil))
  ([{:keys [names] :as contact} {:keys [modal profile? you?]} max-length]
   (let [{:keys [nickname ens-name]} names
         [first-name second-name]    (multiaccounts/contact-two-names contact false)]
     (if (or nickname ens-name)
       [react/nested-text
        {:number-of-lines 2
         :style           {:color          (if modal colors/white-persist colors/black)
                           :font-size      (if profile? 15 13)
                           :line-height    (if profile? 22 18)
                           :letter-spacing -0.2
                           :font-weight    "600"}}
        (subs first-name 0 81)
        (when you?
          [{:style {:color colors/black-light :font-weight "500" :font-size 13}}
           (str " " (i18n/label :t/You))])
        (when nickname
          [{:style {:color colors/black-light :font-weight "500"}}
           (str " " (subs second-name 0 81))])]
       [react/text
        {:style           {:color          (if modal colors/white-persist colors/black)
                           :font-size      (if profile? 15 13)
                           :line-height    (if profile? 22 18)
                           :font-weight    "600"
                           :letter-spacing -0.2}
         :number-of-lines 1}
        (if (and max-length (> (count first-name) max-length))
          (str (subs first-name 0 max-length) "...")
          first-name)]))))
