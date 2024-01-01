(ns status-im.contexts.chat.composer.gradients.view
  (:require
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.composer.gradients.style :as style]
    [utils.re-frame :as rf]))

(defn f-view
  [{:keys [input-ref]}
   {:keys [gradient-z-index]}
   {:keys [gradient-opacity]}
   show-bottom-gradient?]
  (let [showing-extra-space? (boolean (or (rf/sub [:chats/edit-message])
                                          (rf/sub [:chats/reply-message])))]
    [:<>
     [reanimated/linear-gradient
      (style/top-gradient gradient-opacity @gradient-z-index showing-extra-space?)]
     (when show-bottom-gradient?
       [rn/pressable
        {:on-press            #(when @input-ref (.focus ^js @input-ref))
         :accessibility-label :bottom-gradient}
        [linear-gradient/linear-gradient (style/bottom-gradient)]])]))

(defn view
  [props state animations show-bottom-gradient?]
  [:f> f-view props state animations show-bottom-gradient?])
