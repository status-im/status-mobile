(ns status-im.ui.screens.chat.utils
  (:require [re-frame.core :as re-frame]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.platform :as platform]
            [status-im.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.http :as http]))

(defn format-author [from username]
  (str (when username (str username " • "))
       (gfycat/generate-gfy from))) ; TODO: We defensively generate the name for now, to be revisited when new protocol is defined

(defn format-reply-author [from username current-public-key]
  (or (and (= from current-public-key) (i18n/label :t/You))
      (format-author from username)))

(def ^:private styling->prop
  {:bold      {:style {:font-weight :bold}}
   :italic    {:style {:font-style  :italic}}
   :backquote {:style {:background-color colors/black
                       :color            colors/green}}})

(def ^:private action->prop-fn
  {:link   (fn [text {:keys [outgoing]}]
             {:style    {:color                (if platform/desktop?
                                                 colors/blue
                                                 (if outgoing colors/white colors/blue))
                         :text-decoration-line :underline}
              :on-press (if platform/desktop?
                          #(.openURL react/linking (http/normalize-url text))
                          #(re-frame/dispatch [:browser.ui/message-link-pressed text]))})
   :tag    (fn [text {:keys [outgoing]}]
             {:style    {:color                (if platform/desktop?
                                                 colors/blue
                                                 (if outgoing colors/white colors/blue))
                         :text-decoration-line :underline}
              :on-press #(re-frame/dispatch [:chat.ui/start-public-chat (subs text 1)])})})

(defn- lookup-props [text-chunk message kind]
  (let [prop    (get styling->prop kind)
        prop-fn (get action->prop-fn kind)]
    (if prop-fn (prop-fn text-chunk message) prop)))

(defn render-chunks [render-recipe message]
  (map-indexed (fn [idx [text-chunk kind]]
                 (if (= :text kind)
                   text-chunk
                   [react/text (into {:key idx} (lookup-props text-chunk message kind))
                    text-chunk]))
               render-recipe))

(defn render-chunks-desktop [render-recipe message]
  "This fn is only need as a temporary hack
   until rn-desktop supports text/number-of-lines property"
  (seq (second
        (reduce (fn [[total-length acc] [idx [text-chunk kind]]]
                  (if (< constants/chars-collapse-threshold total-length)
                    (reduced [total-length acc])
                    [(+ total-length (count text-chunk))
                     (conj acc
                           (if (= :text kind)
                             text-chunk
                             [react/text (into {:key idx} (lookup-props text-chunk message kind))
                              text-chunk]))]))
                [0 []]
                (map vector (range) render-recipe)))))
