(ns status-im.contexts.chat.messenger.composer.gradients.view
  (:require
    [quo.theme]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.gradients.style :as style]
    [utils.re-frame :as rf]))

(defn f-view
  [{:keys [input-ref]}
   {:keys [gradient-z-index]}
   {:keys [gradient-opacity]}
   show-bottom-gradient?]
  (let [theme                (quo.theme/use-theme)
        showing-extra-space? (boolean (or (rf/sub [:chats/edit-message])
                                          (rf/sub [:chats/reply-message])))]
    [:<>
     [reanimated/linear-gradient
      (style/top-gradient gradient-opacity @gradient-z-index showing-extra-space? theme)]
     (when show-bottom-gradient?
       [rn/pressable
        {:on-press            #(when @input-ref (.focus ^js @input-ref))
         :style               {:z-index 1}
         :accessibility-label :bottom-gradient}
        [linear-gradient/linear-gradient (style/bottom-gradient theme)]])]))

(defn view
  [props state animations show-bottom-gradient?]
  [:f> f-view props state animations show-bottom-gradient?])
