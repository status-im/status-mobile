(ns status-im2.contexts.chat.composer.gradients.view
  (:require
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.composer.gradients.style :as style]))


(defn view
  [{:keys [input-ref]}
   {:keys [gradient-z-index]}
   {:keys [gradient-opacity]}
   show-bottom-gradient?]
  [:f>
   (fn []
     [:<>
      [reanimated/linear-gradient (style/top-gradient gradient-opacity @gradient-z-index)]
      (when show-bottom-gradient?
        [rn/touchable-without-feedback
         {:on-press            #(when @input-ref (.focus ^js @input-ref))
          :accessibility-label :bottom-gradient}
         [linear-gradient/linear-gradient (style/bottom-gradient)]])])])

