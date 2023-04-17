(ns status-im2.contexts.chat.bottom-sheet-composer.gradients.view
  (:require
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.bottom-sheet-composer.gradients.style :as style]))


(defn view
  [{:keys [input-ref]}
   {:keys [gradient-z-index text-value focused?]}
   {:keys [gradient-opacity]}
   {:keys [lines]}]
  [:f>
   (fn []
     [:<>
      [reanimated/linear-gradient (style/top-gradient gradient-opacity @gradient-z-index)]
      (when (and (not-empty @text-value) (not @focused?) (> lines 2))
        [rn/touchable-without-feedback
         {:on-press            #(when @input-ref (.focus ^js @input-ref))
          :accessibility-label :bottom-gradient}
         [linear-gradient/linear-gradient (style/bottom-gradient)]])])])

