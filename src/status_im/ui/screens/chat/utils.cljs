(ns status-im.ui.screens.chat.utils
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.utils.http :as http]
            [status-im.utils.platform :as platform]))

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
