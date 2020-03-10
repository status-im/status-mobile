(ns status-im.ui.screens.chat.utils
  (:require [status-im.ethereum.stateofus :as stateofus]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

(defn format-author [alias style name]
  (let [additional-styles (style false)]
    (if name
      (let [name (subs name 0 80)]
        [react/text {:number-of-lines 2
                     :style (merge {:color       colors/blue
                                    :font-size   13
                                    :line-height 18
                                    :font-weight "500"} additional-styles)}
         (str "@" (or (stateofus/username name) name))])
      [react/text {:style (merge {:color       colors/gray
                                  :font-size   12
                                  :line-height 18
                                  :font-weight "400"} additional-styles)}
       alias])))

(def ^:private reply-symbol "↪ ")

(defn format-reply-author [from alias username current-public-key style]
  (let [reply-name (or (some->> username
                                (str "@")
                                (str reply-symbol))
                       (str reply-symbol alias))]
    (or (and (= from current-public-key)
             [react/text {:style (style true)}
              (str reply-symbol (i18n/label :t/You))])
        (format-author (subs reply-name 0 80) style false))))