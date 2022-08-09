(ns status-im.ui.screens.chat.utils
  (:require [status-im.ethereum.stateofus :as stateofus]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [quo.design-system.colors :as colors]
            [status-im.multiaccounts.core :as multiaccounts]
            [quo2.components.text :as quo2.text]
            [status-im.utils.utils :as utils]
            [quo2.foundations.colors :as quo2.colors :refer [theme-colors]]
            [re-frame.core :as re-frame]))

(def ^:private reply-symbol "↪ ")

(defn format-author-old
  ([contact] (format-author-old contact nil))
  ([{:keys [names] :as contact} {:keys [modal profile? you?]}]
   (let [{:keys [nickname ens-name]} names
         [first-name second-name] (multiaccounts/contact-two-names contact false)]
     (if (or nickname ens-name)
       [react/nested-text {:number-of-lines 2
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
       [react/text {:style {:color       (if modal colors/white-persist colors/gray)
                            :font-size   (if profile? 15 12)
                            :line-height (if profile? 22 18)
                            :font-weight "400"}}
        first-name]))))

(defn format-author
  ([contact] (format-author contact nil))
  ([{:keys [names public-key] :as contact} {:keys [modal timestamp-str? profile? you?]}]
   (let [{:keys [nickname ens-name]} names
         [first-name second-name] (multiaccounts/contact-two-names contact false)
         short-public-key (utils/get-shortened-address public-key)
         window-width @(re-frame/subscribe [:dimensions/window-width])]
     (println window-width "DSFDFSFSDFS")
     (if (or nickname ens-name)
       [react/view {:style {:number-of-lines 1
                            :flex-direction :row
                            :align-items :flex-end
                            :flex 1}}
        [quo2.text/text {:number-of-lines 1
                         :style           {:color       (if modal colors/white-persist colors/black)
                                           :font-size   (if profile? 15 13)
                                           :line-height (if profile? 22 18)
                                           :letter-spacing -0.2
                                           :max-width "30%"
                                           :font-weight "600"
                                           :number-of-lines 1}}
         (str first-name)]
        (when you?
          [quo2.text/text {:weight :regular
                           :size :label
                           :style {:color (theme-colors quo2.colors/neutral-60 quo2.colors/neutral-40)
                                   :text-transform :none}}
           (str " · " (i18n/label :t/You))])
        (when nickname
          [quo2.text/text {:weight :regular
                           :size :label
                           :number-of-lines 1
                           :style {:text-transform :none
                                   :max-width "30%"
                                   :number-of-lines 1
                                   :color (theme-colors quo2.colors/neutral-60 quo2.colors/neutral-40)}}
           (str " · " second-name "  ")])
        (when nickname
          [quo2.text/text
           {:size :label
            :weight :regular
            :monospace true
            :number-of-lines 1
            :style {:text-transform :none
                    :margin-left 8
                    :max-width "30%"
                    :number-of-lines 1
                    :color quo2.colors/neutral-50}}
           short-public-key])
        (when timestamp-str?
          [quo2.text/text
           {:size :label
            :weight :regular
            :number-of-lines 1
            :style {:text-transform :none
                    :color quo2.colors/neutral-50}
            :accessibility-label :message-timestamp}
           (str " · " timestamp-str?)])]
       [react/text-class {:style {:flex-direction :row
                                  :align-items :flex-end
                                  :max-width 342}}
        [quo2.text/text {:number-of-lines 1
                         :size (if profile? :paragraph-1 :paragraph-2)
                         :weight :semi-bold
                         :style {:color (if modal colors/white-persist colors/black)}}
         (str (subs first-name 0 24) (when (> (count first-name) 24) "..."))]
        [quo2.text/text
         {:size :label
          :weight :regular
          :monospace true
          :style {:text-transform :none
                  :margin-left 8
                  :color quo2.colors/neutral-50}}
         (str "  " short-public-key)]
        (when timestamp-str?
          [quo2.text/text
           {:size :label
            :weight :regular
            :number-of-lines 1
            :style {:text-transform :none
                    :color quo2.colors/neutral-50}
            :accessibility-label :message-timestamp}
           (str " · " timestamp-str?)])]))))

(defn format-reply-author [from username current-public-key style outgoing]
  (let [contact-name (str reply-symbol username)]
    (or (and (= from current-public-key)
             [react/text {:style (style true)}
              (str reply-symbol (i18n/label :t/You))])
        (if (or (= (aget contact-name 0) "@")
               ;; in case of replies
                (= (aget contact-name 1) "@"))
          (let [trimmed-name (subs contact-name 0 81)]
            [react/text {:number-of-lines 2
                         :style           (merge {:color       colors/blue
                                                  :font-size   13
                                                  :line-height 18
                                                  :font-weight "500"})}
             (or (stateofus/username trimmed-name) trimmed-name)])
          [react/text {:style (merge {:color       (if outgoing
                                                     colors/white-transparent-70-persist
                                                     colors/gray)
                                      :font-size   12
                                      :line-height 18
                                      :font-weight "400"})}
           contact-name]))))
