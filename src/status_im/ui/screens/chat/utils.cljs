(ns status-im.ui.screens.chat.utils
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.utils.security :as security]
            [status-im.i18n :as i18n]
            [status-im.utils.core :as core-utils]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.http :as http]))

(defn format-author [alias style name]
  (let [additional-styles (style false)]
    (if (ens/is-valid-eth-name? name)
      [react/text {:style (merge {:color colors/blue :font-size 13 :font-weight "500"} additional-styles)}
       (str "@" (or (stateofus/username name) name))]
      [react/text {:style (merge {:color colors/gray :font-size 12 :font-weight "400"} additional-styles)}
       alias])))

(defn format-reply-author [from alias username current-public-key style]
  (or (and (= from current-public-key)
           [react/text {:style (style true)}
            (i18n/label :t/You)])
      (format-author alias style username)))

(def ^:private styling->prop
  {:bold      {:style {:font-weight "700"}}
   :italic    {:style {:font-style  :italic}}
   :backquote {:style {:background-color colors/black
                       :color            colors/green}}})

(def ^:private action->prop-fn
  {:link   (fn [text {:keys [outgoing] :as message}]
             {:style    {:color                (if outgoing colors/white colors/blue)
                         :text-decoration-line :underline}
              :on-press #(when (and (security/safe-link? text)
                                    (security/safe-link-text? (-> message :content :text)))
                           (if platform/desktop?
                             (.openURL react/linking (http/normalize-url text))
                             (re-frame/dispatch [:browser.ui/message-link-pressed text])))})
   :tag    (fn [text {:keys [outgoing]}]
             {:style    {:color                (if outgoing colors/white colors/blue)
                         :text-decoration-line :underline}
              :on-press #(re-frame/dispatch [:chat.ui/start-public-chat (subs text 1) {:navigation-reset? true}])})})

(defn- lookup-props [text-chunk message kind]
  (let [prop    (get styling->prop kind)
        prop-fn (get action->prop-fn kind)]
    (if prop-fn (prop-fn text-chunk message) prop)))

(defn render-chunks [render-recipe message]
  (vec (map-indexed (fn [idx [text-chunk kind]]
                      (if (= :text kind)
                        text-chunk
                        [(into {:key idx} (lookup-props text-chunk message kind))
                         text-chunk]))
                    render-recipe)))

(defn render-chunks-desktop [limit render-recipe message]
  "This fn is only needed as a temporary hack
  until rn-desktop supports text/number-of-lines property"
  (->> render-recipe
       (map vector (range))
       (reduce (fn [[total-length acc] [idx [text-chunk kind]]]
                 (if (<= limit total-length)
                   (reduced [total-length acc])
                   (let [chunk-len (count text-chunk)
                         cut-chunk-len (min chunk-len (- limit total-length))
                         cut-chunk (if (= chunk-len cut-chunk-len)
                                     text-chunk
                                     (core-utils/truncate-str text-chunk cut-chunk-len))]
                     [(+ total-length cut-chunk-len)
                      (conj acc
                            (if (= :text kind)
                              cut-chunk
                              [react/text (into {:key idx} (lookup-props text-chunk message kind))
                               cut-chunk]))])))
               [0 []])
       second
       seq))
